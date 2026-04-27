package com.flipclassroom.controller;

import com.flipclassroom.common.Result;
import com.flipclassroom.dto.auth.LoginRequest;
import com.flipclassroom.dto.auth.LoginResponse;
import com.flipclassroom.service.AuthService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public Result<LoginResponse> login(@RequestBody LoginRequest request) { // 处理登录请求
        try {
            return Result.success(authService.login(request));
        } catch (RuntimeException exception) {
            return Result.fail(exception.getMessage());
        }
    }
}
