package com.flipclassroom.dto.user;

public class UpdateUserResponse {

    private Long id;
    private String username;
    private String realName;
    private String role;
    private String studentNo;
    private String teacherNo;
    private String phone;
    private Integer status;

    public UpdateUserResponse() {
    }

    public UpdateUserResponse(Long id, String username, String realName, String role,
                              String studentNo, String teacherNo, String phone, Integer status) {
        this.id = id;
        this.username = username;
        this.realName = realName;
        this.role = role;
        this.studentNo = studentNo;
        this.teacherNo = teacherNo;
        this.phone = phone;
        this.status = status;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getRealName() {
        return realName;
    }

    public void setRealName(String realName) {
        this.realName = realName;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getStudentNo() {
        return studentNo;
    }

    public void setStudentNo(String studentNo) {
        this.studentNo = studentNo;
    }

    public String getTeacherNo() {
        return teacherNo;
    }

    public void setTeacherNo(String teacherNo) {
        this.teacherNo = teacherNo;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }
}
