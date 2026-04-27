package com.flipclassroom.dto.auth;

public class LoginResponse {

    private Long userId;
    private String username;
    private String realName;
    private String role;
    private String token;

    public LoginResponse() {
    }

    public LoginResponse(Long userId, String username, String realName, String role, String token) {
        this.userId = userId;
        this.username = username;
        this.realName = realName;
        this.role = role;
        this.token = token;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
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

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }
}
