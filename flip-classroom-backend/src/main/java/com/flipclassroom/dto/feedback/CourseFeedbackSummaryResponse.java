package com.flipclassroom.dto.feedback;

public class CourseFeedbackSummaryResponse {

    private Integer totalCount;
    private Double averageRating;
    private Integer masteredCount;
    private Integer basicCount;
    private Integer weakCount;
    private String latestCreatedAt;

    public CourseFeedbackSummaryResponse(Integer totalCount, Double averageRating, Integer masteredCount,
                                         Integer basicCount, Integer weakCount, String latestCreatedAt) {
        this.totalCount = totalCount;
        this.averageRating = averageRating;
        this.masteredCount = masteredCount;
        this.basicCount = basicCount;
        this.weakCount = weakCount;
        this.latestCreatedAt = latestCreatedAt;
    }

    public Integer getTotalCount() {
        return totalCount;
    }

    public Double getAverageRating() {
        return averageRating;
    }

    public Integer getMasteredCount() {
        return masteredCount;
    }

    public Integer getBasicCount() {
        return basicCount;
    }

    public Integer getWeakCount() {
        return weakCount;
    }

    public String getLatestCreatedAt() {
        return latestCreatedAt;
    }
}
