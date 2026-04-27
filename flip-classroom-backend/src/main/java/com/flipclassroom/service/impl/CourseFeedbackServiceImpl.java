package com.flipclassroom.service.impl;

import com.flipclassroom.dto.feedback.CourseFeedbackItemResponse;
import com.flipclassroom.dto.feedback.CourseFeedbackSummaryResponse;
import com.flipclassroom.dto.feedback.CreateCourseFeedbackRequest;
import com.flipclassroom.entity.Course;
import com.flipclassroom.entity.CourseFeedback;
import com.flipclassroom.entity.CourseMember;
import com.flipclassroom.entity.SysUser;
import com.flipclassroom.mapper.CourseFeedbackMapper;
import com.flipclassroom.mapper.CourseMapper;
import com.flipclassroom.mapper.UserMapper;
import com.flipclassroom.service.CourseFeedbackService;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Set;

@Service
public class CourseFeedbackServiceImpl implements CourseFeedbackService {

    private static final Set<String> MASTERY_LEVELS = Set.of("MASTERED", "BASIC", "WEAK");

    private final CourseFeedbackMapper feedbackMapper;
    private final CourseMapper courseMapper;
    private final UserMapper userMapper;

    public CourseFeedbackServiceImpl(CourseFeedbackMapper feedbackMapper,
                                     CourseMapper courseMapper,
                                     UserMapper userMapper) {
        this.feedbackMapper = feedbackMapper;
        this.courseMapper = courseMapper;
        this.userMapper = userMapper;
    }

    @Override
    public CourseFeedbackItemResponse createFeedback(Long courseId, CreateCourseFeedbackRequest request) {
        ensureCourseExists(courseId);
        validateCreateRequest(request);

        SysUser student = ensureStudentExists(request.getStudentId());
        ensureCourseMember(courseId, student.getId());

        CourseFeedback feedback = new CourseFeedback();
        feedback.setCourseId(courseId);
        feedback.setStudentId(student.getId());
        feedback.setStudentUsername(student.getUsername());
        feedback.setStudentName(student.getRealName());
        feedback.setLessonTitle(request.getLessonTitle().trim());
        feedback.setRating(request.getRating());
        feedback.setMasteryLevel(request.getMasteryLevel().trim().toUpperCase());
        feedback.setContent(blankToNull(request.getContent()));
        feedback.setSuggestion(blankToNull(request.getSuggestion()));

        Long id = feedbackMapper.insert(feedback);
        if (id == null) {
            throw new IllegalStateException("Failed to create course feedback");
        }
        feedback.setId(id);

        return toItem(feedback);
    }

    @Override
    public List<CourseFeedbackItemResponse> listCourseFeedback(Long courseId) {
        ensureCourseExists(courseId);
        return feedbackMapper.findByCourseId(courseId)
                .stream()
                .map(this::toItem)
                .toList();
    }

    @Override
    public List<CourseFeedbackItemResponse> listMyFeedback(Long courseId, Long studentId) {
        ensureCourseExists(courseId);
        SysUser student = ensureStudentExists(studentId);
        ensureCourseMember(courseId, student.getId());
        return feedbackMapper.findByCourseAndStudent(courseId, studentId)
                .stream()
                .map(this::toItem)
                .toList();
    }

    @Override
    public CourseFeedbackSummaryResponse getSummary(Long courseId) {
        ensureCourseExists(courseId);
        List<CourseFeedback> feedbackList = feedbackMapper.findByCourseId(courseId);
        int total = feedbackList.size();
        if (total == 0) {
            return new CourseFeedbackSummaryResponse(0, 0.0, 0, 0, 0, null);
        }

        int ratingSum = 0;
        int masteredCount = 0;
        int basicCount = 0;
        int weakCount = 0;
        for (CourseFeedback feedback : feedbackList) {
            ratingSum += feedback.getRating() == null ? 0 : feedback.getRating();
            String mastery = feedback.getMasteryLevel();
            if ("MASTERED".equalsIgnoreCase(mastery)) {
                masteredCount++;
            } else if ("BASIC".equalsIgnoreCase(mastery)) {
                basicCount++;
            } else if ("WEAK".equalsIgnoreCase(mastery)) {
                weakCount++;
            }
        }

        double averageRating = BigDecimal.valueOf((double) ratingSum / total)
                .setScale(1, RoundingMode.HALF_UP)
                .doubleValue();

        return new CourseFeedbackSummaryResponse(
                total,
                averageRating,
                masteredCount,
                basicCount,
                weakCount,
                feedbackList.get(0).getCreatedAt()
        );
    }

    private CourseFeedbackItemResponse toItem(CourseFeedback feedback) {
        return new CourseFeedbackItemResponse(
                feedback.getId(),
                feedback.getCourseId(),
                feedback.getStudentId(),
                feedback.getStudentUsername(),
                feedback.getStudentName(),
                feedback.getLessonTitle(),
                feedback.getRating(),
                feedback.getMasteryLevel(),
                feedback.getContent(),
                feedback.getSuggestion(),
                feedback.getCreatedAt()
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

    private SysUser ensureStudentExists(Long studentId) {
        if (studentId == null) {
            throw new IllegalArgumentException("Student id cannot be empty");
        }
        SysUser student = userMapper.findById(studentId);
        if (student == null) {
            throw new IllegalArgumentException("Student does not exist");
        }
        if (student.getRole() == null || !"STUDENT".equalsIgnoreCase(student.getRole())) {
            throw new IllegalArgumentException("StudentId must point to a student user");
        }
        return student;
    }

    private CourseMember ensureCourseMember(Long courseId, Long studentId) {
        CourseMember courseMember = courseMapper.findMember(courseId, studentId);
        if (courseMember == null) {
            throw new IllegalArgumentException("Student is not a member of the course");
        }
        return courseMember;
    }

    private void validateCreateRequest(CreateCourseFeedbackRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Request body cannot be empty");
        }
        if (request.getStudentId() == null || isBlank(request.getLessonTitle())) {
            throw new IllegalArgumentException("StudentId and lessonTitle are required");
        }
        if (request.getRating() == null || request.getRating() < 1 || request.getRating() > 5) {
            throw new IllegalArgumentException("Rating must be between 1 and 5");
        }
        if (isBlank(request.getMasteryLevel())) {
            throw new IllegalArgumentException("Mastery level is required");
        }
        String normalizedMastery = request.getMasteryLevel().trim().toUpperCase();
        if (!MASTERY_LEVELS.contains(normalizedMastery)) {
            throw new IllegalArgumentException("Mastery level must be MASTERED, BASIC or WEAK");
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private String blankToNull(String value) {
        return isBlank(value) ? null : value.trim();
    }
}
