package com.flipclassroom.dto.assignment;

public class UpdateAssignmentResponse {

    private Long id;
    private Long courseId;
    private String title;
    private String description;
    private String deadline;
    private Integer published;

    public UpdateAssignmentResponse(Long id, Long courseId, String title, String description,
                                    String deadline, Integer published) {
        this.id = id;
        this.courseId = courseId;
        this.title = title;
        this.description = description;
        this.deadline = deadline;
        this.published = published;
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

    public String getDescription() {
        return description;
    }

    public String getDeadline() {
        return deadline;
    }

    public Integer getPublished() {
        return published;
    }
}
