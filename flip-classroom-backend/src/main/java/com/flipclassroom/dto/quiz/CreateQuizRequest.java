package com.flipclassroom.dto.quiz;

import java.util.List;

public class CreateQuizRequest {

    private String title;
    private String description;
    private Integer totalScore;
    private Integer status;
    private String publishTime;
    private List<CreateQuizQuestionRequest> questions;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Integer getTotalScore() {
        return totalScore;
    }

    public void setTotalScore(Integer totalScore) {
        this.totalScore = totalScore;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public String getPublishTime() {
        return publishTime;
    }

    public void setPublishTime(String publishTime) {
        this.publishTime = publishTime;
    }

    public List<CreateQuizQuestionRequest> getQuestions() {
        return questions;
    }

    public void setQuestions(List<CreateQuizQuestionRequest> questions) {
        this.questions = questions;
    }
}
