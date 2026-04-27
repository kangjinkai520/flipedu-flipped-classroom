package com.flipclassroom.service.impl;

import com.flipclassroom.dto.user.CreateUserRequest;
import com.flipclassroom.dto.user.CreateUserResponse;
import com.flipclassroom.dto.user.UserDetailResponse;
import com.flipclassroom.dto.user.UpdateUserRequest;
import com.flipclassroom.dto.user.UpdateUserResponse;
import com.flipclassroom.dto.user.UserListItemResponse;
import com.flipclassroom.entity.SysUser;
import com.flipclassroom.mapper.UserMapper;
import com.flipclassroom.service.UserService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Locale;

@Service
public class UserServiceImpl implements UserService {

    private final UserMapper userMapper;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public UserServiceImpl(UserMapper userMapper) {
        this.userMapper = userMapper;
    }

    @Override
    public List<UserListItemResponse> listUsers(String role) { // 查询用户列表并转换成前端返回格式
        return userMapper.findAll(role)
                .stream()
                .map(this::toListItem)
                .toList();
    }

    @Override
    public UserDetailResponse getUser(Long id) { // 查询用户详情
        if (id == null) {
            throw new IllegalArgumentException("User id cannot be empty");
        }

        SysUser user = userMapper.findById(id);
        if (user == null) {
            throw new IllegalArgumentException("User does not exist");
        }

        return toDetail(user);
    }

    @Override
    public CreateUserResponse createUser(CreateUserRequest request) { // 新增用户
        validateCreateRequest(request);

        // 用户名必须唯一，所以先查重，再决定是否继续写数据库。
        if (userMapper.findByUsername(request.getUsername()) != null) {
            throw new IllegalArgumentException("Username already exists");
        }

        // 先规范角色、加密密码，再把可选字段里的空字符串统一处理成 null。
        String normalizedRole = normalizeRole(request.getRole());
        SysUser user = new SysUser();
        user.setUsername(request.getUsername().trim());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRealName(request.getRealName().trim());
        user.setRole(normalizedRole.toUpperCase(Locale.ROOT));
        user.setStudentNo(blankToNull(request.getStudentNo()));
        user.setTeacherNo(blankToNull(request.getTeacherNo()));
        user.setPhone(blankToNull(request.getPhone()));
        user.setStatus(1);

        Long id = userMapper.insert(user);
        if (id == null) {
            throw new IllegalStateException("Failed to create user");
        }

        return new CreateUserResponse(
                id,
                user.getUsername(),
                user.getRealName(),
                normalizedRole,
                user.getStatus()
        );
    }

    @Override
    public void updateUserStatus(Long id, Integer status) {
        if (id == null) {
            throw new IllegalArgumentException("User id cannot be empty");
        }
        if (status == null || (status != 0 && status != 1)) {
            throw new IllegalArgumentException("Status must be 0 or 1");
        }

        SysUser existingUser = userMapper.findById(id);
        if (existingUser == null) {
            throw new IllegalArgumentException("User does not exist");
        }
        if ("ADMIN".equalsIgnoreCase(existingUser.getRole())) {
            throw new IllegalArgumentException("管理员账号不能被停用");
        }

        int affectedRows = userMapper.updateStatus(id, status);
        if (affectedRows == 0) {
            throw new IllegalArgumentException("User does not exist");
        }
    }

    @Override
    public UpdateUserResponse updateUser(Long id, UpdateUserRequest request) { // 修改用户基础信息
        if (id == null) {
            throw new IllegalArgumentException("User id cannot be empty");
        }
        validateUpdateRequest(request);

        SysUser existingUser = userMapper.findById(id);
        if (existingUser == null) {
            throw new IllegalArgumentException("User does not exist");
        }

        String normalizedUsername = request.getUsername().trim();
        // 用户可以保留自己的原用户名，但不能改成别人的用户名。
        SysUser duplicatedUser = userMapper.findByUsername(normalizedUsername);
        if (duplicatedUser != null && !duplicatedUser.getId().equals(id)) {
            throw new IllegalArgumentException("Username already exists");
        }

        // 复用原有实体，确保这个接口不会顺手改掉密码和状态。
        String normalizedRole = normalizeRole(request.getRole());
        existingUser.setUsername(normalizedUsername);
        existingUser.setRealName(request.getRealName().trim());
        existingUser.setRole(normalizedRole.toUpperCase(Locale.ROOT));
        existingUser.setStudentNo(blankToNull(request.getStudentNo()));
        existingUser.setTeacherNo(blankToNull(request.getTeacherNo()));
        existingUser.setPhone(blankToNull(request.getPhone()));

        int affectedRows = userMapper.updateBasicInfo(existingUser);
        if (affectedRows == 0) {
            throw new IllegalStateException("Failed to update user");
        }

        return new UpdateUserResponse(
                existingUser.getId(),
                existingUser.getUsername(),
                existingUser.getRealName(),
                normalizedRole,
                existingUser.getStudentNo(),
                existingUser.getTeacherNo(),
                existingUser.getPhone(),
                existingUser.getStatus()
        );
    }

    private UserListItemResponse toListItem(SysUser user) { // 把数据库实体转换成列表返回对象
        // 返回给前端时统一用小写角色，保证接口风格一致。
        String normalizedRole = user.getRole() == null
                ? null
                : user.getRole().toLowerCase(Locale.ROOT);
        return new UserListItemResponse(
                user.getId(),
                user.getUsername(),
                user.getRealName(),
                normalizedRole,
                user.getStatus()
        );
    }

    private UserDetailResponse toDetail(SysUser user) { // 把数据库实体转换成详情返回对象
        String normalizedRole = user.getRole() == null
                ? null
                : user.getRole().toLowerCase(Locale.ROOT);
        return new UserDetailResponse(
                user.getId(),
                user.getUsername(),
                user.getRealName(),
                normalizedRole,
                user.getStudentNo(),
                user.getTeacherNo(),
                user.getPhone(),
                user.getStatus()
        );
    }

    private void validateCreateRequest(CreateUserRequest request) { // 校验新增用户请求
        if (request == null) {
            throw new IllegalArgumentException("Request body cannot be empty");
        }
        if (isBlank(request.getUsername()) || isBlank(request.getPassword())
                || isBlank(request.getRealName()) || isBlank(request.getRole())) {
            throw new IllegalArgumentException("Username, password, realName and role are required");
        }
        normalizeRole(request.getRole());
    }

    private void validateUpdateRequest(UpdateUserRequest request) { // 校验修改用户请求
        if (request == null) {
            throw new IllegalArgumentException("Request body cannot be empty");
        }
        if (isBlank(request.getUsername()) || isBlank(request.getRealName()) || isBlank(request.getRole())) {
            throw new IllegalArgumentException("Username, realName and role are required");
        }
        normalizeRole(request.getRole());
    }

    private String normalizeRole(String role) { // 统一并校验角色值
        String normalizedRole = role.trim().toLowerCase(Locale.ROOT);
        if (!normalizedRole.equals("admin") && !normalizedRole.equals("teacher") && !normalizedRole.equals("student")) {
            throw new IllegalArgumentException("Role must be admin, teacher or student");
        }
        return normalizedRole;
    }

    private boolean isBlank(String value) { // 判断字符串是否为空或全是空格
        return value == null || value.trim().isEmpty();
    }

    private String blankToNull(String value) { // 把空字符串转换成 null，方便入库
        return isBlank(value) ? null : value.trim();
    }
}
