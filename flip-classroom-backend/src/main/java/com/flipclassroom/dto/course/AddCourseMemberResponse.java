package com.flipclassroom.dto.course;

public class AddCourseMemberResponse {

    private Long id;
    private Long courseId;
    private Long userId;
    private String role;

    public AddCourseMemberResponse() {
    }

    public AddCourseMemberResponse(Long id, Long courseId, Long userId, String role) {
        this.id = id;
        this.courseId = courseId;
        this.userId = userId;
        this.role = role;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getCourseId() {
        return courseId;
    }

    public void setCourseId(Long courseId) {
        this.courseId = courseId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }
}
