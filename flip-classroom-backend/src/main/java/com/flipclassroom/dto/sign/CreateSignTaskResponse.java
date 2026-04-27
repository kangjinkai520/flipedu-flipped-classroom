package com.flipclassroom.dto.sign;

public class CreateSignTaskResponse {

    private Long id;
    private Long courseId;
    private String title;
    private String signCode;
    private String startsAt;
    private String endsAt;
    private String locationName;
    private Double targetLatitude;
    private Double targetLongitude;
    private Integer allowedRadiusMeters;
    private boolean locationEnabled;
    private String status;

    public CreateSignTaskResponse(Long id,
                                  Long courseId,
                                  String title,
                                  String signCode,
                                  String startsAt,
                                  String endsAt,
                                  String locationName,
                                  Double targetLatitude,
                                  Double targetLongitude,
                                  Integer allowedRadiusMeters,
                                  boolean locationEnabled,
                                  String status) {
        this.id = id;
        this.courseId = courseId;
        this.title = title;
        this.signCode = signCode;
        this.startsAt = startsAt;
        this.endsAt = endsAt;
        this.locationName = locationName;
        this.targetLatitude = targetLatitude;
        this.targetLongitude = targetLongitude;
        this.allowedRadiusMeters = allowedRadiusMeters;
        this.locationEnabled = locationEnabled;
        this.status = status;
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

    public String getSignCode() {
        return signCode;
    }

    public String getStartsAt() {
        return startsAt;
    }

    public String getEndsAt() {
        return endsAt;
    }

    public String getLocationName() {
        return locationName;
    }

    public Double getTargetLatitude() {
        return targetLatitude;
    }

    public Double getTargetLongitude() {
        return targetLongitude;
    }

    public Integer getAllowedRadiusMeters() {
        return allowedRadiusMeters;
    }

    public boolean isLocationEnabled() {
        return locationEnabled;
    }

    public String getStatus() {
        return status;
    }
}
