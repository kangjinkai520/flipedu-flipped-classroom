package com.flipclassroom.service;

import com.flipclassroom.dto.user.CreateUserRequest;
import com.flipclassroom.dto.user.CreateUserResponse;
import com.flipclassroom.dto.user.UserDetailResponse;
import com.flipclassroom.dto.user.UpdateUserRequest;
import com.flipclassroom.dto.user.UpdateUserResponse;
import com.flipclassroom.dto.user.UserListItemResponse;

import java.util.List;

public interface UserService {

    List<UserListItemResponse> listUsers(String role); // 查询用户列表

    UserDetailResponse getUser(Long id); // 查询用户详情

    CreateUserResponse createUser(CreateUserRequest request); // 新增用户

    void updateUserStatus(Long id, Integer status); // 修改用户状态

    UpdateUserResponse updateUser(Long id, UpdateUserRequest request); // 修改用户基础信息
}
