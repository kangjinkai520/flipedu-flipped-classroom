package com.flipclassroom.dto.quiz;

public class QuizRecordItemResponse {

    private Long id;
    private Long quizId;
    private Long studentId;
    private String studentUsername;
    private String studentName;
    private Integer score;
    private String status;
    private String answersJson;
    private String submittedAt;

    public QuizRecordItemResponse(Long id,
                                  Long quizId,
                                  Long studentId,
                                  String studentUsername,
                                  String studentName,
                                  Integer score,
                                  String status,
                                  String answersJson,
                                  String submittedAt) {
        this.id = id;
        this.quizId = quizId;
        this.studentId = studentId;
        this.studentUsername = studentUsername;
        this.studentName = studentName;
        this.score = score;
        this.status = status;
        this.answersJson = answersJson;
        this.submittedAt = submittedAt;
    }

    public Long getId() {
        return id;
    }

    public Long getQuizId() {
        return quizId;
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

    public Integer getScore() {
        return score;
    }

    public String getStatus() {
        return status;
    }

    public String getAnswersJson() {
        return answersJson;
    }

    public String getSubmittedAt() {
        return submittedAt;
    }
}
