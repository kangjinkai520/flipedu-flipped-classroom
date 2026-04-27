package com.flipclassroom.dto.user;

public class UserDetailResponse {

    private Long id;
    private String username;
    private String realName;
    private String role;
    private String studentNo;
    private String teacherNo;
    private String phone;
    private Integer status;

    public UserDetailResponse(Long id, String username, String realName, String role,
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

    public String getUsername() {
        return username;
    }

    public String getRealName() {
        return realName;
    }

    public String getRole() {
        return role;
    }

    public String getStudentNo() {
        return studentNo;
    }

    public String getTeacherNo() {
        return teacherNo;
    }

    public String getPhone() {
        return phone;
    }

    public Integer getStatus() {
        return status;
    }
}
