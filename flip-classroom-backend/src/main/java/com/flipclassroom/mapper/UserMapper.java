package com.flipclassroom.mapper;

import com.flipclassroom.entity.SysUser;

import java.util.List;

public interface UserMapper {

    SysUser findByUsername(String username); // 按用户名查询用户

    SysUser findById(Long id); // 按 id 查询用户

    List<SysUser> findAll(String role); // 查询全部用户，必要时按角色筛选

    Long insert(SysUser user); // 插入新用户并返回主键 id

    int updateStatus(Long id, Integer status); // 更新用户状态

    int updateBasicInfo(SysUser user); // 更新用户基础信息
}
