package com.flipclassroom.dto.assignment;

public class SubmitAssignmentResponse {

    private Long id;
    private Long assignmentId;
    private Long studentId;
    private String content;
    private String attachmentUrl;
    private Integer score;
    private String feedback;
    private String submittedAt;

    public SubmitAssignmentResponse(Long id, Long assignmentId, Long studentId, String content,
                                    String attachmentUrl, Integer score, String feedback, String submittedAt) {
        this.id = id;
        this.assignmentId = assignmentId;
        this.studentId = studentId;
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
