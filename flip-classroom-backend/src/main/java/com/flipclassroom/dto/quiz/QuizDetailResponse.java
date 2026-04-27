package com.flipclassroom.dto.quiz;

public class QuizDetailResponse {

    private Long id;
    private Long courseId;
    private String title;
    private String description;
    private Integer totalScore;
    private Integer status;
    private String publishTime;

    public QuizDetailResponse(Long id,
                              Long courseId,
                              String title,
                              String description,
                              Integer totalScore,
                              Integer status,
                              String publishTime) {
        this.id = id;
        this.courseId = courseId;
        this.title = title;
        this.description = description;
        this.totalScore = totalScore;
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

    public Integer getStatus() {
        return status;
    }

    public String getPublishTime() {
        return publishTime;
    }
}
