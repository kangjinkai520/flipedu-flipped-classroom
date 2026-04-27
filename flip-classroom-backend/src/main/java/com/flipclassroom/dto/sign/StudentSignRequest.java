package com.flipclassroom.dto.sign;

public class StudentSignRequest {

    private Long studentId;
    private String signCode;
    private Double latitude;
    private Double longitude;
    private Boolean exceptionRequest;
    private String exceptionReason;

    public Long getStudentId() {
        return studentId;
    }

    public void setStudentId(Long studentId) {
        this.studentId = studentId;
    }

    public String getSignCode() {
        return signCode;
    }

    public void setSignCode(String signCode) {
        this.signCode = signCode;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public Boolean getExceptionRequest() {
        return exceptionRequest;
    }

    public void setExceptionRequest(Boolean exceptionRequest) {
        this.exceptionRequest = exceptionRequest;
    }

    public String getExceptionReason() {
        return exceptionReason;
    }

    public void setExceptionReason(String exceptionReason) {
        this.exceptionReason = exceptionReason;
    }
}
