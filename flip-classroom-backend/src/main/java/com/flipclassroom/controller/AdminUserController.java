package com.flipclassroom.controller;

import com.flipclassroom.common.Result;
import com.flipclassroom.dto.user.CreateUserRequest;
import com.flipclassroom.dto.user.CreateUserResponse;
import com.flipclassroom.dto.user.UserDetailResponse;
import com.flipclassroom.dto.user.UpdateUserRequest;
import com.flipclassroom.dto.user.UpdateUserResponse;
import com.flipclassroom.dto.user.UpdateUserStatusRequest;
import com.flipclassroom.dto.user.UserListItemResponse;
import com.flipclassroom.service.UserService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/admin/users")
public class AdminUserController {

    private final UserService userService;

    public AdminUserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public Result<List<UserListItemResponse>> listUsers(@RequestParam(required = false) String role) { // 查询用户列表，支持按角色筛选
        return Result.success(userService.listUsers(role));
    }

    @GetMapping("/{id}")
    public Result<UserDetailResponse> getUser(@PathVariable Long id) { // 查询用户详情
        try {
            return Result.success(userService.getUser(id));
        } catch (RuntimeException exception) {
            return Result.fail(exception.getMessage());
        }
    }

    @PostMapping
    public Result<CreateUserResponse> createUser(@RequestBody CreateUserRequest request) { // 新增用户
        try {
            return Result.success(userService.createUser(request));
        } catch (RuntimeException exception) {
            return Result.fail(exception.getMessage());
        }
    }

    @PatchMapping("/{id}/status")
    public Result<String> updateUserStatus(@PathVariable Long id, @RequestBody UpdateUserStatusRequest request) { // 修改用户启用/停用状态
        try {
            userService.updateUserStatus(id, request == null ? null : request.getStatus());
            return Result.success("status updated");
        } catch (RuntimeException exception) {
            return Result.fail(exception.getMessage());
        }
    }

    @PutMapping("/{id}")
    public Result<UpdateUserResponse> updateUser(@PathVariable Long id, @RequestBody UpdateUserRequest request) { // 修改用户基础信息
        try {
            return Result.success(userService.updateUser(id, request));
        } catch (RuntimeException exception) {
            return Result.fail(exception.getMessage());
        }
    }
}
