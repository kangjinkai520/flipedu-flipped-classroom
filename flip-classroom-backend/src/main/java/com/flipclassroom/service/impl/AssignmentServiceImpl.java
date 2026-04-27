package com.flipclassroom.service.impl;

import com.flipclassroom.dto.assignment.AssignmentDetailResponse;
import com.flipclassroom.dto.assignment.AssignmentListItemResponse;
import com.flipclassroom.dto.assignment.AssignmentSubmissionItemResponse;
import com.flipclassroom.dto.assignment.CreateAssignmentRequest;
import com.flipclassroom.dto.assignment.CreateAssignmentResponse;
import com.flipclassroom.dto.assignment.ScoreAssignmentSubmissionRequest;
import com.flipclassroom.dto.assignment.ScoreAssignmentSubmissionResponse;
import com.flipclassroom.dto.assignment.SubmitAssignmentRequest;
import com.flipclassroom.dto.assignment.SubmitAssignmentResponse;
import com.flipclassroom.dto.assignment.UpdateAssignmentRequest;
import com.flipclassroom.dto.assignment.UpdateAssignmentResponse;
import com.flipclassroom.entity.Assignment;
import com.flipclassroom.entity.AssignmentSubmit;
import com.flipclassroom.entity.Course;
import com.flipclassroom.entity.CourseMember;
import com.flipclassroom.entity.SysUser;
import com.flipclassroom.mapper.AssignmentMapper;
import com.flipclassroom.mapper.CourseMapper;
import com.flipclassroom.mapper.UserMapper;
import com.flipclassroom.service.AssignmentService;
import com.flipclassroom.service.ScoreService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class AssignmentServiceImpl implements AssignmentService {

    private static final DateTimeFormatter TIMESTAMP_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final AssignmentMapper assignmentMapper;
    private final CourseMapper courseMapper;
    private final UserMapper userMapper;
    private final ScoreService scoreService;

    public AssignmentServiceImpl(AssignmentMapper assignmentMapper,
                                 CourseMapper courseMapper,
                                 UserMapper userMapper,
                                 ScoreService scoreService) {
        this.assignmentMapper = assignmentMapper;
        this.courseMapper = courseMapper;
        this.userMapper = userMapper;
        this.scoreService = scoreService;
    }

    @Override
    public List<AssignmentListItemResponse> listAssignments(Long courseId) {
        ensureCourseExists(courseId);
        return assignmentMapper.findByCourseId(courseId)
                .stream()
                .map(this::toListItem)
                .toList();
    }

    @Override
    public CreateAssignmentResponse createAssignment(Long courseId, CreateAssignmentRequest request) {
        ensureCourseExists(courseId);
        validateCreateAssignmentRequest(request);

        Assignment assignment = new Assignment();
        assignment.setCourseId(courseId);
        assignment.setTitle(request.getTitle().trim());
        assignment.setDescription(request.getDescription().trim());
        assignment.setDeadline(blankToNull(request.getDeadline()));
        assignment.setPublished(normalizePublished(request.getPublished(), 1));

        Long id = assignmentMapper.insert(assignment);
        if (id == null) {
            throw new IllegalStateException("Failed to create assignment");
        }

        return new CreateAssignmentResponse(
                id,
                courseId,
                assignment.getTitle(),
                assignment.getDescription(),
                assignment.getDeadline(),
                assignment.getPublished()
        );
    }

    @Override
    public AssignmentDetailResponse getAssignment(Long id) {
        Assignment assignment = ensureAssignmentExists(id);
        return toDetail(assignment);
    }

    @Override
    public UpdateAssignmentResponse updateAssignment(Long id, UpdateAssignmentRequest request) {
        if (id == null) {
            throw new IllegalArgumentException("Assignment id cannot be empty");
        }
        validateUpdateAssignmentRequest(request);

        Assignment existingAssignment = ensureAssignmentExists(id);
        existingAssignment.setTitle(request.getTitle().trim());
        existingAssignment.setDescription(request.getDescription().trim());
        existingAssignment.setDeadline(blankToNull(request.getDeadline()));
        existingAssignment.setPublished(normalizePublished(request.getPublished(), existingAssignment.getPublished()));

        int affectedRows = assignmentMapper.updateBasicInfo(existingAssignment);
        if (affectedRows == 0) {
            throw new IllegalStateException("Failed to update assignment");
        }

        return new UpdateAssignmentResponse(
                existingAssignment.getId(),
                existingAssignment.getCourseId(),
                existingAssignment.getTitle(),
                existingAssignment.getDescription(),
                existingAssignment.getDeadline(),
                existingAssignment.getPublished()
        );
    }

    @Override
    public void updateAssignmentPublished(Long id, Integer published) {
        if (id == null) {
            throw new IllegalArgumentException("Assignment id cannot be empty");
        }
        ensureAssignmentExists(id);
        int affectedRows = assignmentMapper.updatePublished(id, normalizePublished(published, null));
        if (affectedRows == 0) {
            throw new IllegalStateException("Failed to update assignment published");
        }
    }

    @Override
    public List<AssignmentSubmissionItemResponse> listAssignmentSubmissions(Long assignmentId) {
        ensureAssignmentExists(assignmentId);
        return assignmentMapper.findSubmissionsByAssignmentId(assignmentId)
                .stream()
                .map(this::toSubmissionItem)
                .toList();
    }

    @Override
    public SubmitAssignmentResponse submitAssignment(Long assignmentId, SubmitAssignmentRequest request) {
        if (request == null || request.getStudentId() == null || isBlank(request.getContent())) {
            throw new IllegalArgumentException("StudentId and content are required");
        }

        Assignment assignment = ensureAssignmentExists(assignmentId);
        if (assignment.getPublished() == null || assignment.getPublished() != 1) {
            throw new IllegalArgumentException("Assignment is not available");
        }

        SysUser student = ensureStudentExists(request.getStudentId());
        CourseMember courseMember = courseMapper.findMember(assignment.getCourseId(), student.getId());
        if (courseMember == null) {
            throw new IllegalArgumentException("Student is not a member of the course");
        }

        String submittedAt = LocalDateTime.now().format(TIMESTAMP_FORMATTER);
        AssignmentSubmit existingSubmission = assignmentMapper.findSubmission(assignmentId, student.getId());

        if (existingSubmission == null) {
            AssignmentSubmit assignmentSubmit = new AssignmentSubmit();
            assignmentSubmit.setAssignmentId(assignmentId);
            assignmentSubmit.setStudentId(student.getId());
            assignmentSubmit.setContent(request.getContent().trim());
            assignmentSubmit.setAttachmentUrl(blankToNull(request.getAttachmentUrl()));
            assignmentSubmit.setScore(null);
            assignmentSubmit.setFeedback(null);
            assignmentSubmit.setSubmittedAt(submittedAt);

            Long id = assignmentMapper.insertSubmission(assignmentSubmit);
            if (id == null) {
                throw new IllegalStateException("Failed to submit assignment");
            }

            return new SubmitAssignmentResponse(
                    id,
                    assignmentId,
                    student.getId(),
                    assignmentSubmit.getContent(),
                    assignmentSubmit.getAttachmentUrl(),
                    null,
                    null,
                    submittedAt
            );
        }

        existingSubmission.setContent(request.getContent().trim());
        existingSubmission.setAttachmentUrl(blankToNull(request.getAttachmentUrl()));
        existingSubmission.setScore(null);
        existingSubmission.setFeedback(null);
        existingSubmission.setSubmittedAt(submittedAt);
        int affectedRows = assignmentMapper.updateSubmission(existingSubmission);
        if (affectedRows == 0) {
            throw new IllegalStateException("Failed to update assignment submission");
        }

        return new SubmitAssignmentResponse(
                existingSubmission.getId(),
                assignmentId,
                student.getId(),
                existingSubmission.getContent(),
                existingSubmission.getAttachmentUrl(),
                existingSubmission.getScore(),
                existingSubmission.getFeedback(),
                existingSubmission.getSubmittedAt()
        );
    }

    @Override
    public ScoreAssignmentSubmissionResponse scoreAssignmentSubmission(Long submissionId, ScoreAssignmentSubmissionRequest request) {
        if (submissionId == null) {
            throw new IllegalArgumentException("Submission id cannot be empty");
        }
        if (request == null || request.getScore() == null || request.getScore() < 0) {
            throw new IllegalArgumentException("Score must be greater than or equal to 0");
        }

        AssignmentSubmit submission = assignmentMapper.findSubmissionById(submissionId);
        if (submission == null) {
            throw new IllegalArgumentException("Assignment submission does not exist");
        }

        Assignment assignment = ensureAssignmentExists(submission.getAssignmentId());
        submission.setScore(request.getScore());
        submission.setFeedback(blankToNull(request.getFeedback()));
        int affectedRows = assignmentMapper.updateSubmission(submission);
        if (affectedRows == 0) {
            throw new IllegalStateException("Failed to score assignment submission");
        }

        scoreService.upsertScoreRecord(
                assignment.getCourseId(),
                submission.getStudentId(),
                "assignment",
                assignment.getTitle(),
                request.getScore(),
                "课后作业成绩"
        );

        return new ScoreAssignmentSubmissionResponse(
                submission.getId(),
                submission.getAssignmentId(),
                submission.getStudentId(),
                submission.getScore(),
                submission.getFeedback()
        );
    }

    private AssignmentListItemResponse toListItem(Assignment assignment) {
        return new AssignmentListItemResponse(
                assignment.getId(),
                assignment.getCourseId(),
                assignment.getTitle(),
                assignment.getDescription(),
                assignment.getDeadline(),
                assignment.getPublished()
        );
    }

    private AssignmentDetailResponse toDetail(Assignment assignment) {
        return new AssignmentDetailResponse(
                assignment.getId(),
                assignment.getCourseId(),
                assignment.getTitle(),
                assignment.getDescription(),
                assignment.getDeadline(),
                assignment.getPublished()
        );
    }

    private AssignmentSubmissionItemResponse toSubmissionItem(AssignmentSubmit assignmentSubmit) {
        return new AssignmentSubmissionItemResponse(
                assignmentSubmit.getId(),
                assignmentSubmit.getAssignmentId(),
                assignmentSubmit.getStudentId(),
                assignmentSubmit.getStudentUsername(),
                assignmentSubmit.getStudentName(),
                assignmentSubmit.getContent(),
                assignmentSubmit.getAttachmentUrl(),
                assignmentSubmit.getScore(),
                assignmentSubmit.getFeedback(),
                assignmentSubmit.getSubmittedAt()
        );
    }

    private void validateCreateAssignmentRequest(CreateAssignmentRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Request body cannot be empty");
        }
        if (isBlank(request.getTitle()) || isBlank(request.getDescription())) {
            throw new IllegalArgumentException("Title and description are required");
        }
        normalizePublished(request.getPublished(), 1);
    }

    private void validateUpdateAssignmentRequest(UpdateAssignmentRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Request body cannot be empty");
        }
        if (isBlank(request.getTitle()) || isBlank(request.getDescription())) {
            throw new IllegalArgumentException("Title and description are required");
        }
        if (request.getPublished() == null) {
            throw new IllegalArgumentException("Published is required");
        }
        normalizePublished(request.getPublished(), 1);
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

    private Assignment ensureAssignmentExists(Long assignmentId) {
        if (assignmentId == null) {
            throw new IllegalArgumentException("Assignment id cannot be empty");
        }
        Assignment assignment = assignmentMapper.findById(assignmentId);
        if (assignment == null) {
            throw new IllegalArgumentException("Assignment does not exist");
        }
        return assignment;
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

    private Integer normalizePublished(Integer published, Integer defaultPublished) {
        Integer finalPublished = published == null ? defaultPublished : published;
        if (finalPublished == null || (finalPublished != 0 && finalPublished != 1)) {
            throw new IllegalArgumentException("Published must be 0 or 1");
        }
        return finalPublished;
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private String blankToNull(String value) {
        return isBlank(value) ? null : value.trim();
    }
}
