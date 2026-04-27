package com.flipclassroom.dto.assignment;

public class ScoreAssignmentSubmissionResponse {

    private Long id;
    private Long assignmentId;
    private Long studentId;
    private Integer score;
    private String feedback;

    public ScoreAssignmentSubmissionResponse(Long id, Long assignmentId, Long studentId,
                                             Integer score, String feedback) {
        this.id = id;
        this.assignmentId = assignmentId;
        this.studentId = studentId;
        this.score = score;
        this.feedback = feedback;
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

    public Integer getScore() {
        return score;
    }

    public String getFeedback() {
        return feedback;
    }
}
