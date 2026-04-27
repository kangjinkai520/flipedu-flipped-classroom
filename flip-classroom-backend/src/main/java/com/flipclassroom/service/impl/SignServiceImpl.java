package com.flipclassroom.service.impl;

import com.flipclassroom.dto.sign.CreateSignTaskRequest;
import com.flipclassroom.dto.sign.CreateSignTaskResponse;
import com.flipclassroom.dto.sign.SignRecordItemResponse;
import com.flipclassroom.dto.sign.SignTaskDetailResponse;
import com.flipclassroom.dto.sign.SignTaskListItemResponse;
import com.flipclassroom.dto.sign.StudentSignRequest;
import com.flipclassroom.dto.sign.StudentSignResponse;
import com.flipclassroom.entity.Course;
import com.flipclassroom.entity.CourseMember;
import com.flipclassroom.entity.SignRecord;
import com.flipclassroom.entity.SignTask;
import com.flipclassroom.entity.SysUser;
import com.flipclassroom.mapper.CourseMapper;
import com.flipclassroom.mapper.SignMapper;
import com.flipclassroom.mapper.UserMapper;
import com.flipclassroom.service.SignService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

@Service
public class SignServiceImpl implements SignService {

    private static final DateTimeFormatter TIMESTAMP_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final double EARTH_RADIUS_METERS = 6371000d;

    private final SignMapper signMapper;
    private final CourseMapper courseMapper;
    private final UserMapper userMapper;

    public SignServiceImpl(SignMapper signMapper, CourseMapper courseMapper, UserMapper userMapper) {
        this.signMapper = signMapper;
        this.courseMapper = courseMapper;
        this.userMapper = userMapper;
    }

    @Override
    public List<SignTaskListItemResponse> listSignTasks(Long courseId) {
        ensureCourseExists(courseId);
        return signMapper.findTasksByCourseId(courseId)
                .stream()
                .map(this::toTaskListItem)
                .toList();
    }

    @Override
    public CreateSignTaskResponse createSignTask(Long courseId, CreateSignTaskRequest request) {
        ensureCourseExists(courseId);
        validateCreateTaskRequest(request);

        SignTask signTask = new SignTask();
        signTask.setCourseId(courseId);
        signTask.setTitle(request.getTitle().trim());
        signTask.setSignCode(request.getSignCode().trim().toUpperCase(Locale.ROOT));
        signTask.setStartsAt(blankToNull(request.getStartsAt()));
        signTask.setEndsAt(blankToNull(request.getEndsAt()));
        signTask.setLocationName(blankToNull(request.getLocationName()));
        signTask.setTargetLatitude(request.getTargetLatitude());
        signTask.setTargetLongitude(request.getTargetLongitude());
        signTask.setAllowedRadiusMeters(request.getAllowedRadiusMeters());
        signTask.setStatus(normalizeTaskStatus(request.getStatus(), "ONGOING"));

        Long id = signMapper.insertTask(signTask);
        if (id == null) {
            throw new IllegalStateException("Failed to create sign task");
        }

        return toCreateTaskResponse(id, signTask);
    }

    @Override
    public SignTaskDetailResponse getSignTask(Long id) {
        SignTask signTask = ensureSignTaskExists(id);
        return toTaskDetail(signTask);
    }

    @Override
    public void updateSignTaskStatus(Long id, String status) {
        String normalizedStatus = normalizeTaskStatus(status, null);
        ensureSignTaskExists(id);
        int affectedRows = signMapper.updateTaskStatus(id, normalizedStatus);
        if (affectedRows == 0) {
            throw new IllegalStateException("Failed to update sign task status");
        }
    }

    @Override
    public List<SignRecordItemResponse> listSignRecords(Long signTaskId) {
        SignTask signTask = ensureSignTaskExists(signTaskId);
        ensureCourseExists(signTask.getCourseId());
        return signMapper.findRecordsByTaskId(signTaskId)
                .stream()
                .map(this::toRecordItem)
                .toList();
    }

    @Override
    public StudentSignResponse submitSign(Long signTaskId, StudentSignRequest request) {
        if (request == null || request.getStudentId() == null || isBlank(request.getSignCode())) {
            throw new IllegalArgumentException("StudentId and signCode are required");
        }

        SignTask signTask = ensureSignTaskExists(signTaskId);
        if (!"ONGOING".equalsIgnoreCase(signTask.getStatus())) {
            throw new IllegalArgumentException("Sign task is not available");
        }
        if (!signTask.getSignCode().equalsIgnoreCase(request.getSignCode().trim())) {
            throw new IllegalArgumentException("Sign code is incorrect");
        }

        SysUser student = ensureStudentExists(request.getStudentId());
        ensureCourseMembership(signTask.getCourseId(), student.getId());

        if (signMapper.findRecord(signTaskId, student.getId()) != null) {
            throw new IllegalArgumentException("Student has already signed in");
        }

        SignRecord signRecord = new SignRecord();
        signRecord.setSignTaskId(signTaskId);
        signRecord.setStudentId(student.getId());
        signRecord.setSignedAt(LocalDateTime.now().format(TIMESTAMP_FORMATTER));

        boolean locationEnabled = isLocationEnabled(signTask);
        if (!locationEnabled) {
            signRecord.setSignType("NORMAL");
            signRecord.setStatus("SIGNED");
        } else {
            handleLocationSubmission(signTask, request, signRecord);
        }

        Long recordId = signMapper.insertRecord(signRecord);
        if (recordId == null) {
            throw new IllegalStateException("Failed to submit sign record");
        }

        SignRecord savedRecord = signMapper.findRecordById(recordId);
        if (savedRecord == null) {
            savedRecord = signRecord;
            savedRecord.setId(recordId);
        }

        return new StudentSignResponse(
                savedRecord.getId(),
                signTaskId,
                student.getId(),
                savedRecord.getSignedAt(),
                savedRecord.getSignType(),
                savedRecord.getStatus(),
                savedRecord.getDistanceMeters(),
                buildSignMessage(savedRecord)
        );
    }

    @Override
    public void reviewSignRecord(Long recordId, String status, String reviewComment) {
        if (recordId == null) {
            throw new IllegalArgumentException("Sign record id cannot be empty");
        }
        String normalizedStatus = normalizeReviewStatus(status);
        SignRecord record = signMapper.findRecordById(recordId);
        if (record == null) {
            throw new IllegalArgumentException("Sign record does not exist");
        }
        if (!"PENDING_REVIEW".equalsIgnoreCase(record.getStatus())) {
            throw new IllegalArgumentException("Only pending records can be reviewed");
        }
        int affectedRows = signMapper.updateRecordReview(recordId, normalizedStatus, blankToNull(reviewComment));
        if (affectedRows == 0) {
            throw new IllegalStateException("Failed to review sign record");
        }
    }

    private void handleLocationSubmission(SignTask signTask, StudentSignRequest request, SignRecord signRecord) {
        Double latitude = request.getLatitude();
        Double longitude = request.getLongitude();
        boolean exceptionRequest = Boolean.TRUE.equals(request.getExceptionRequest());
        String exceptionReason = blankToNull(request.getExceptionReason());

        signRecord.setLatitude(latitude);
        signRecord.setLongitude(longitude);

        if (latitude != null && longitude != null) {
            validateCoordinate(latitude, longitude);
            double distanceMeters = calculateDistanceMeters(
                    signTask.getTargetLatitude(),
                    signTask.getTargetLongitude(),
                    latitude,
                    longitude
            );
            signRecord.setDistanceMeters(roundDistance(distanceMeters));

            if (distanceMeters <= signTask.getAllowedRadiusMeters()) {
                signRecord.setSignType("LOCATION");
                signRecord.setStatus("SIGNED");
                return;
            }

            if (!exceptionRequest || isBlank(exceptionReason)) {
                throw new IllegalArgumentException("Current location is outside the allowed range, please submit an exception request");
            }

            signRecord.setSignType("EXCEPTION");
            signRecord.setStatus("PENDING_REVIEW");
            signRecord.setExceptionReason(exceptionReason);
            return;
        }

        if (!exceptionRequest || isBlank(exceptionReason)) {
            throw new IllegalArgumentException("Location is required, please submit an exception request if location is unavailable");
        }

        signRecord.setSignType("EXCEPTION");
        signRecord.setStatus("PENDING_REVIEW");
        signRecord.setExceptionReason(exceptionReason);
    }

    private CreateSignTaskResponse toCreateTaskResponse(Long id, SignTask signTask) {
        return new CreateSignTaskResponse(
                id,
                signTask.getCourseId(),
                signTask.getTitle(),
                signTask.getSignCode(),
                signTask.getStartsAt(),
                signTask.getEndsAt(),
                signTask.getLocationName(),
                signTask.getTargetLatitude(),
                signTask.getTargetLongitude(),
                signTask.getAllowedRadiusMeters(),
                isLocationEnabled(signTask),
                signTask.getStatus()
        );
    }

    private SignTaskListItemResponse toTaskListItem(SignTask signTask) {
        return new SignTaskListItemResponse(
                signTask.getId(),
                signTask.getCourseId(),
                signTask.getTitle(),
                signTask.getSignCode(),
                signTask.getStartsAt(),
                signTask.getEndsAt(),
                signTask.getLocationName(),
                signTask.getTargetLatitude(),
                signTask.getTargetLongitude(),
                signTask.getAllowedRadiusMeters(),
                isLocationEnabled(signTask),
                signTask.getStatus()
        );
    }

    private SignTaskDetailResponse toTaskDetail(SignTask signTask) {
        return new SignTaskDetailResponse(
                signTask.getId(),
                signTask.getCourseId(),
                signTask.getTitle(),
                signTask.getSignCode(),
                signTask.getStartsAt(),
                signTask.getEndsAt(),
                signTask.getLocationName(),
                signTask.getTargetLatitude(),
                signTask.getTargetLongitude(),
                signTask.getAllowedRadiusMeters(),
                isLocationEnabled(signTask),
                signTask.getStatus()
        );
    }

    private SignRecordItemResponse toRecordItem(SignRecord signRecord) {
        return new SignRecordItemResponse(
                signRecord.getId(),
                signRecord.getSignTaskId(),
                signRecord.getStudentId(),
                signRecord.getStudentUsername(),
                signRecord.getStudentName(),
                signRecord.getSignedAt(),
                signRecord.getLatitude(),
                signRecord.getLongitude(),
                signRecord.getDistanceMeters(),
                signRecord.getSignType(),
                signRecord.getStatus(),
                signRecord.getExceptionReason(),
                signRecord.getReviewComment()
        );
    }

    private Course ensureCourseExists(Long courseId) {
        if (courseId == null) {
            throw new IllegalArgumentException("Course id cannot be empty");
        }
        Course course = courseMapper.findById(courseId);
        if (course == null) {
            throw new IllegalArgumentException("Course does not exist");
        }
        return course;
    }

    private SignTask ensureSignTaskExists(Long signTaskId) {
        if (signTaskId == null) {
            throw new IllegalArgumentException("Sign task id cannot be empty");
        }
        SignTask signTask = signMapper.findTaskById(signTaskId);
        if (signTask == null) {
            throw new IllegalArgumentException("Sign task does not exist");
        }
        return signTask;
    }

    private SysUser ensureStudentExists(Long studentId) {
        SysUser student = userMapper.findById(studentId);
        if (student == null) {
            throw new IllegalArgumentException("Student does not exist");
        }
        if (student.getRole() == null || !"STUDENT".equalsIgnoreCase(student.getRole())) {
            throw new IllegalArgumentException("StudentId must point to a student user");
        }
        return student;
    }

    private void ensureCourseMembership(Long courseId, Long studentId) {
        CourseMember courseMember = courseMapper.findMember(courseId, studentId);
        if (courseMember == null) {
            throw new IllegalArgumentException("Student is not a member of the course");
        }
    }

    private void validateCreateTaskRequest(CreateSignTaskRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Request body cannot be empty");
        }
        if (isBlank(request.getTitle()) || isBlank(request.getSignCode())) {
            throw new IllegalArgumentException("Title and signCode are required");
        }
        normalizeTaskStatus(request.getStatus(), "ONGOING");

        boolean hasAnyLocationField = !isBlank(request.getLocationName())
                || request.getTargetLatitude() != null
                || request.getTargetLongitude() != null
                || request.getAllowedRadiusMeters() != null;

        if (!hasAnyLocationField) {
            return;
        }

        if (isBlank(request.getLocationName())
                || request.getTargetLatitude() == null
                || request.getTargetLongitude() == null
                || request.getAllowedRadiusMeters() == null) {
            throw new IllegalArgumentException("Location sign-in requires locationName, latitude, longitude and radius");
        }
        validateCoordinate(request.getTargetLatitude(), request.getTargetLongitude());
        if (request.getAllowedRadiusMeters() <= 0) {
            throw new IllegalArgumentException("Allowed radius must be greater than 0");
        }
    }

    private String normalizeTaskStatus(String status, String defaultStatus) {
        String finalStatus = isBlank(status) ? defaultStatus : status.trim().toUpperCase(Locale.ROOT);
        if (finalStatus == null || (!"ONGOING".equals(finalStatus) && !"FINISHED".equals(finalStatus))) {
            throw new IllegalArgumentException("Status must be ONGOING or FINISHED");
        }
        return finalStatus;
    }

    private String normalizeReviewStatus(String status) {
        String finalStatus = isBlank(status) ? null : status.trim().toUpperCase(Locale.ROOT);
        if (!"APPROVED".equals(finalStatus) && !"REJECTED".equals(finalStatus)) {
            throw new IllegalArgumentException("Review status must be APPROVED or REJECTED");
        }
        return finalStatus;
    }

    private boolean isLocationEnabled(SignTask signTask) {
        return signTask.getTargetLatitude() != null
                && signTask.getTargetLongitude() != null
                && signTask.getAllowedRadiusMeters() != null
                && signTask.getAllowedRadiusMeters() > 0;
    }

    private void validateCoordinate(Double latitude, Double longitude) {
        if (latitude < -90 || latitude > 90) {
            throw new IllegalArgumentException("Latitude is out of range");
        }
        if (longitude < -180 || longitude > 180) {
            throw new IllegalArgumentException("Longitude is out of range");
        }
    }

    private double calculateDistanceMeters(double lat1, double lon1, double lat2, double lon2) {
        double radLat1 = Math.toRadians(lat1);
        double radLat2 = Math.toRadians(lat2);
        double deltaLat = Math.toRadians(lat2 - lat1);
        double deltaLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(deltaLat / 2) * Math.sin(deltaLat / 2)
                + Math.cos(radLat1) * Math.cos(radLat2)
                * Math.sin(deltaLon / 2) * Math.sin(deltaLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return EARTH_RADIUS_METERS * c;
    }

    private Double roundDistance(double distanceMeters) {
        return Math.round(distanceMeters * 100.0) / 100.0;
    }

    private String buildSignMessage(SignRecord signRecord) {
        if ("PENDING_REVIEW".equalsIgnoreCase(signRecord.getStatus())) {
            return "已提交异常签到申请，等待教师审核";
        }
        if ("SIGNED".equalsIgnoreCase(signRecord.getStatus())) {
            return "签到成功";
        }
        if ("APPROVED".equalsIgnoreCase(signRecord.getStatus())) {
            return "异常签到申请已通过";
        }
        if ("REJECTED".equalsIgnoreCase(signRecord.getStatus())) {
            return "异常签到申请已驳回";
        }
        return "签到状态已更新";
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private String blankToNull(String value) {
        return isBlank(value) ? null : value.trim();
    }
}
