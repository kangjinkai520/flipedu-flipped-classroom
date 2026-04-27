package com.flipclassroom.dto.course;

public class CourseDetailResponse {

    private Long id;
    private Long teacherId;
    private String teacherName;
    private String teacherUsername;
    private String courseName;
    private String courseCode;
    private String term;
    private String introduction;
    private Integer status;

    public CourseDetailResponse() {
    }

    public CourseDetailResponse(Long id, Long teacherId, String teacherName, String teacherUsername,
                                String courseName, String courseCode, String term,
                                String introduction, Integer status) {
        this.id = id;
        this.teacherId = teacherId;
        this.teacherName = teacherName;
        this.teacherUsername = teacherUsername;
        this.courseName = courseName;
        this.courseCode = courseCode;
        this.term = term;
        this.introduction = introduction;
        this.status = status;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getTeacherId() {
        return teacherId;
    }

    public void setTeacherId(Long teacherId) {
        this.teacherId = teacherId;
    }

    public String getTeacherName() {
        return teacherName;
    }

    public void setTeacherName(String teacherName) {
        this.teacherName = teacherName;
    }

    public String getTeacherUsername() {
        return teacherUsername;
    }

    public void setTeacherUsername(String teacherUsername) {
        this.teacherUsername = teacherUsername;
    }

    public String getCourseName() {
        return courseName;
    }

    public void setCourseName(String courseName) {
        this.courseName = courseName;
    }

    public String getCourseCode() {
        return courseCode;
    }

    public void setCourseCode(String courseCode) {
        this.courseCode = courseCode;
    }

    public String getTerm() {
        return term;
    }

    public void setTerm(String term) {
        this.term = term;
    }

    public String getIntroduction() {
        return introduction;
    }

    public void setIntroduction(String introduction) {
        this.introduction = introduction;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }
}
