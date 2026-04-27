package com.flipclassroom.dto.quiz;

public class SubmitQuizResponse {

    private Long recordId;
    private Long quizId;
    private Long studentId;
    private Integer score;
    private String status;
    private String submittedAt;

    public SubmitQuizResponse(Long recordId,
                              Long quizId,
                              Long studentId,
                              Integer score,
                              String status,
                              String submittedAt) {
        this.recordId = recordId;
        this.quizId = quizId;
        this.studentId = studentId;
        this.score = score;
        this.status = status;
        this.submittedAt = submittedAt;
    }

    public Long getRecordId() {
        return recordId;
    }

    public Long getQuizId() {
        return quizId;
    }

    public Long getStudentId() {
        return studentId;
    }

    public Integer getScore() {
        return score;
    }

    public String getStatus() {
        return status;
    }

    public String getSubmittedAt() {
        return submittedAt;
    }
}
