package com.flipclassroom.mapper;

import com.flipclassroom.entity.TeachingMaterial;

import java.util.List;

public interface MaterialMapper {

    List<TeachingMaterial> findByCourseId(Long courseId); // 按课程查询教学资料列表

    TeachingMaterial findById(Long id); // 按 id 查询教学资料

    Long insert(TeachingMaterial material); // 插入教学资料并返回新记录 id

    int updateBasicInfo(TeachingMaterial material); // 更新教学资料基础信息

    int updateStatus(Long id, Integer status); // 更新教学资料状态
}
