package com.flipclassroom.service.impl;

import com.flipclassroom.dto.auth.LoginRequest;
import com.flipclassroom.dto.auth.LoginResponse;
import com.flipclassroom.entity.SysUser;
import com.flipclassroom.mapper.UserMapper;
import com.flipclassroom.service.AuthService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Locale;

@Service
public class AuthServiceImpl implements AuthService {

    private final UserMapper userMapper;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public AuthServiceImpl(UserMapper userMapper) {
        this.userMapper = userMapper;
    }

    @Override
    public LoginResponse login(LoginRequest request) { // 处理登录校验并返回登录结果
        if (request == null || isBlank(request.getUsername()) || isBlank(request.getPassword())) {
            throw new IllegalArgumentException("Username and password cannot be empty");
        }

        // 登录时先确认账号存在，再校验明文密码和数据库里的 BCrypt 哈希是否匹配。
        SysUser user = userMapper.findByUsername(request.getUsername());
        if (user == null || !passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new IllegalArgumentException("Username or password is incorrect");
        }

        if (user.getStatus() == null || user.getStatus() != 1) {
            throw new IllegalStateException("账号已被停用，请联系管理员");
        }

        // 返回给前端时统一使用小写角色，并生成一个简单演示 token。
        String normalizedRole = user.getRole().toLowerCase(Locale.ROOT);
        String token = "db-token-" + normalizedRole + "-" + user.getId();
        return new LoginResponse(
                user.getId(),
                user.getUsername(),
                user.getRealName(),
                normalizedRole,
                token
        );
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
