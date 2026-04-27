package com.flipclassroom.dto.sign;

public class StudentSignResponse {

    private Long recordId;
    private Long signTaskId;
    private Long studentId;
    private String signedAt;
    private String signType;
    private String status;
    private Double distanceMeters;
    private String message;

    public StudentSignResponse(Long recordId,
                               Long signTaskId,
                               Long studentId,
                               String signedAt,
                               String signType,
                               String status,
                               Double distanceMeters,
                               String message) {
        this.recordId = recordId;
        this.signTaskId = signTaskId;
        this.studentId = studentId;
        this.signedAt = signedAt;
        this.signType = signType;
        this.status = status;
        this.distanceMeters = distanceMeters;
        this.message = message;
    }

    public Long getRecordId() {
        return recordId;
    }

    public Long getSignTaskId() {
        return signTaskId;
    }

    public Long getStudentId() {
        return studentId;
    }

    public String getSignedAt() {
        return signedAt;
    }

    public String getSignType() {
        return signType;
    }

    public String getStatus() {
        return status;
    }

    public Double getDistanceMeters() {
        return distanceMeters;
    }

    public String getMessage() {
        return message;
    }
}
