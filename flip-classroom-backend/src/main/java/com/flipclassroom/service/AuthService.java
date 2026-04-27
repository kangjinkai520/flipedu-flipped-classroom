package com.flipclassroom.service;

import com.flipclassroom.dto.auth.LoginRequest;
import com.flipclassroom.dto.auth.LoginResponse;

public interface AuthService {

    LoginResponse login(LoginRequest request); // 登录并返回登录结果
}
