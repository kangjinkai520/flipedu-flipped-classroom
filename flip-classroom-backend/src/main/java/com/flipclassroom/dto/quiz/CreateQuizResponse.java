package com.flipclassroom.dto.quiz;

public class CreateQuizResponse {

    private Long id;
    private Long courseId;
    private String title;
    private String description;
    private Integer totalScore;
    private Integer questionCount;
    private Integer status;
    private String publishTime;

    public CreateQuizResponse(Long id,
                              Long courseId,
                              String title,
                              String description,
                              Integer totalScore,
                              Integer questionCount,
                              Integer status,
                              String publishTime) {
        this.id = id;
        this.courseId = courseId;
        this.title = title;
        this.description = description;
        this.totalScore = totalScore;
        this.questionCount = questionCount;
        this.status = status;
        this.publishTime = publishTime;
    }

    public Long getId() {
        return id;
    }

    public Long getCourseId() {
        return courseId;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public Integer getTotalScore() {
        return totalScore;
    }

    public Integer getQuestionCount() {
        return questionCount;
    }

    public Integer getStatus() {
        return status;
    }

    public String getPublishTime() {
        return publishTime;
    }
}
