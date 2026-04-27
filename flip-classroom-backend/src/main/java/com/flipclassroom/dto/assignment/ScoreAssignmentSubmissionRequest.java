package com.flipclassroom.dto.assignment;

public class ScoreAssignmentSubmissionRequest {

    private Integer score;
    private String feedback;

    public Integer getScore() {
        return score;
    }

    public void setScore(Integer score) {
        this.score = score;
    }

    public String getFeedback() {
        return feedback;
    }

    public void setFeedback(String feedback) {
        this.feedback = feedback;
    }
}
