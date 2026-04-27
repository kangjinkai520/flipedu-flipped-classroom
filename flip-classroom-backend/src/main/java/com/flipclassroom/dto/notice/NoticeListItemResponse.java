package com.flipclassroom.dto.notice;

public class NoticeListItemResponse {

    private Long id;
    private Long courseId;
    private String title;
    private String content;
    private String publishTime;
    private Integer status;

    public NoticeListItemResponse(Long id,
                                  Long courseId,
                                  String title,
                                  String content,
                                  String publishTime,
                                  Integer status) {
        this.id = id;
        this.courseId = courseId;
        this.title = title;
        this.content = content;
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

    public String getContent() {
        return content;
    }

    public String getPublishTime() {
        return publishTime;
    }

    public Integer getStatus() {
        return status;
    }
}
