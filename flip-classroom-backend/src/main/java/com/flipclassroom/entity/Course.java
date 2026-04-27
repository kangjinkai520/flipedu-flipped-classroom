package com.flipclassroom.entity;

public class Course {

    private Long id;
    private Long teacherId;
    private String teacherName;
    private String teacherUsername;
    private String courseName;
    private String courseCode;
    private String introduction;
    private String term;
    private Integer status;

    public Course() {
    }

    public Course(Long id, Long teacherId, String teacherName, String courseName, String courseCode,
                  String introduction, String term, Integer status) {
        this.id = id;
        this.teacherId = teacherId;
        this.teacherName = teacherName;
        this.courseName = courseName;
        this.courseCode = courseCode;
        this.introduction = introduction;
        this.term = term;
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

    public String getIntroduction() {
        return introduction;
    }

    public void setIntroduction(String introduction) {
        this.introduction = introduction;
    }

    public String getTerm() {
        return term;
    }

    public void setTerm(String term) {
        this.term = term;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }
}
