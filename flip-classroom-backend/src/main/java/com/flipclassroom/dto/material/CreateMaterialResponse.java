package com.flipclassroom.dto.material;

public class CreateMaterialResponse {

    private Long id;
    private Long courseId;
    private String title;
    private String materialType;
    private String materialUrl;
    private String publishTime;
    private Integer status;

    public CreateMaterialResponse(Long id,
                                  Long courseId,
                                  String title,
                                  String materialType,
                                  String materialUrl,
                                  String publishTime,
                                  Integer status) {
        this.id = id;
        this.courseId = courseId;
        this.title = title;
        this.materialType = materialType;
        this.materialUrl = materialUrl;
        this.publishTime = publishTime;
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

    public String getMaterialType() {
        return materialType;
    }

    public String getMaterialUrl() {
        return materialUrl;
    }

    public String getPublishTime() {
        return publishTime;
    }

    public Integer getStatus() {
        return status;
    }
}
