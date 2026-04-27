package com.flipclassroom.dto.assignment;

public class AssignmentSubmissionItemResponse {

    private Long id;
    private Long assignmentId;
    private Long studentId;
    private String studentUsername;
    private String studentName;
    private String content;
    private String attachmentUrl;
    private Integer score;
    private String feedback;
    private String submittedAt;

    public AssignmentSubmissionItemResponse(Long id, Long assignmentId, Long studentId,
                                            String studentUsername, String studentName, String content,
                                            String attachmentUrl, Integer score, String feedback,
                                            String submittedAt) {
        this.id = id;
        this.assignmentId = assignmentId;
        this.studentId = studentId;
        this.studentUsername = studentUsername;
        this.studentName = studentName;
        this.content = content;
        this.attachmentUrl = attachmentUrl;
        this.score = score;
        this.feedback = feedback;
        this.submittedAt = submittedAt;
    }

    public Long getId() {
        return id;
    }

    public Long getAssignmentId() {
        return assignmentId;
    }

    public Long getStudentId() {
        return studentId;
    }

    public String getStudentUsername() {
        return studentUsername;
    }

    public String getStudentName() {
        return studentName;
    }

    public String getContent() {
        return content;
    }

    public String getAttachmentUrl() {
        return attachmentUrl;
    }

    public Integer getScore() {
        return score;
    }

    public String getFeedback() {
        return feedback;
    }

    public String getSubmittedAt() {
        return submittedAt;
    }
}
