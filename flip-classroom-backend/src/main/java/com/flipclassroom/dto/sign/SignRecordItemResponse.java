package com.flipclassroom.dto.sign;

public class SignRecordItemResponse {

    private Long id;
    private Long signTaskId;
    private Long studentId;
    private String studentUsername;
    private String studentName;
    private String signedAt;
    private Double latitude;
    private Double longitude;
    private Double distanceMeters;
    private String signType;
    private String status;
    private String exceptionReason;
    private String reviewComment;

    public SignRecordItemResponse(Long id,
                                  Long signTaskId,
                                  Long studentId,
                                  String studentUsername,
                                  String studentName,
                                  String signedAt,
                                  Double latitude,
                                  Double longitude,
                                  Double distanceMeters,
                                  String signType,
                                  String status,
                                  String exceptionReason,
                                  String reviewComment) {
        this.id = id;
        this.signTaskId = signTaskId;
        this.studentId = studentId;
        this.studentUsername = studentUsername;
        this.studentName = studentName;
        this.signedAt = signedAt;
        this.latitude = latitude;
        this.longitude = longitude;
        this.distanceMeters = distanceMeters;
        this.signType = signType;
        this.status = status;
        this.exceptionReason = exceptionReason;
        this.reviewComment = reviewComment;
    }

    public Long getId() {
        return id;
    }

    public Long getSignTaskId() {
        return signTaskId;
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

    public String getSignedAt() {
        return signedAt;
    }

    public Double getLatitude() {
        return latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public Double getDistanceMeters() {
        return distanceMeters;
    }

    public String getSignType() {
        return signType;
    }

    public String getStatus() {
        return status;
    }

    public String getExceptionReason() {
        return exceptionReason;
    }

    public String getReviewComment() {
        return reviewComment;
    }
}
