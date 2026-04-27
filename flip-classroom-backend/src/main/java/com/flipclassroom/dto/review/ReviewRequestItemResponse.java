package com.flipclassroom.dto.review;

public class ReviewRequestItemResponse {

    private Long id;
    private String targetType;
    private Long targetId;
    private String actionType;
    private Long requesterId;
    private String requesterName;
    private String title;
    private String summary;
    private String payload;
    private String status;
    private String reviewComment;
    private String createdAt;
    private String reviewedAt;

    public ReviewRequestItemResponse(Long id, String targetType, Long targetId, String actionType,
                                     Long requesterId, String requesterName, String title, String summary,
                                     String payload, String status, String reviewComment,
                                     String createdAt, String reviewedAt) {
        this.id = id;
        this.targetType = targetType;
        this.targetId = targetId;
        this.actionType = actionType;
        this.requesterId = requesterId;
        this.requesterName = requesterName;
        this.title = title;
        this.summary = summary;
        this.payload = payload;
        this.status = status;
        this.reviewComment = reviewComment;
        this.createdAt = createdAt;
        this.reviewedAt = reviewedAt;
    }

    public Long getId() { return id; }
    public String getTargetType() { return targetType; }
    public Long getTargetId() { return targetId; }
    public String getActionType() { return actionType; }
    public Long getRequesterId() { return requesterId; }
    public String getRequesterName() { return requesterName; }
    public String getTitle() { return title; }
    public String getSummary() { return summary; }
    public String getPayload() { return payload; }
    public String getStatus() { return status; }
    public String getReviewComment() { return reviewComment; }
    public String getCreatedAt() { return createdAt; }
    public String getReviewedAt() { return reviewedAt; }
}
