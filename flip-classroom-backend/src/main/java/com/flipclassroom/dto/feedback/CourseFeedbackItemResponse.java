package com.flipclassroom.dto.feedback;

public class CourseFeedbackItemResponse {

    private Long id;
    private Long courseId;
    private Long studentId;
    private String studentUsername;
    private String studentName;
    private String lessonTitle;
    private Integer rating;
    private String masteryLevel;
    private String content;
    private String suggestion;
    private String createdAt;

    public CourseFeedbackItemResponse(Long id, Long courseId, Long studentId, String studentUsername,
                                      String studentName, String lessonTitle, Integer rating,
                                      String masteryLevel, String content, String suggestion, String createdAt) {
        this.id = id;
        this.courseId = courseId;
        this.studentId = studentId;
        this.studentUsername = studentUsername;
        this.studentName = studentName;
        this.lessonTitle = lessonTitle;
        this.rating = rating;
        this.masteryLevel = masteryLevel;
        this.content = content;
        this.suggestion = suggestion;
        this.createdAt = createdAt;
    }

    public Long getId() {
        return id;
    }

    public Long getCourseId() {
        return courseId;
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

    public String getLessonTitle() {
        return lessonTitle;
    }

    public Integer getRating() {
        return rating;
    }

    public String getMasteryLevel() {
        return masteryLevel;
    }

    public String getContent() {
        return content;
    }

    public String getSuggestion() {
        return suggestion;
    }

    public String getCreatedAt() {
        return createdAt;
    }
}
