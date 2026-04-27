package com.flipclassroom.dto.user;

public class CreateUserResponse {

    private Long id;
    private String username;
    private String realName;
    private String role;
    private Integer status;

    public CreateUserResponse() {
    }

    public CreateUserResponse(Long id, String username, String realName, String role, Integer status) {
        this.id = id;
        this.username = username;
        this.realName = realName;
        this.role = role;
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

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }
}
