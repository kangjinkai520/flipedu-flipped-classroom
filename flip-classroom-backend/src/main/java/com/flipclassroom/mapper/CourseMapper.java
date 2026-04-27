package com.flipclassroom.mapper;

import com.flipclassroom.entity.Course;
import com.flipclassroom.entity.CourseMember;

import java.util.List;

public interface CourseMapper {

    List<Course> findAll(); // 查询课程列表

    List<Course> findByTeacherId(Long teacherId); // 按教师查询课程列表

    List<Course> findByMemberUserId(Long memberUserId); // 按课程成员查询课程列表

    Course findById(Long id); // 按 id 查询课程

    Course findByCode(String courseCode); // 按课程编码查询课程

    Long insert(Course course); // 插入课程并返回新记录 id

    int updateBasicInfo(Course course); // 更新课程基础信息

    List<CourseMember> findMembers(Long courseId); // 查询课程成员列表

    CourseMember findMember(Long courseId, Long userId); // 查询课程内是否已存在某成员

    Long insertMember(CourseMember courseMember); // 添加课程成员并返回新记录 id
}
