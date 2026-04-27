package com.flipclassroom.service;

import com.flipclassroom.dto.course.AddCourseMemberRequest;
import com.flipclassroom.dto.course.AddCourseMemberResponse;
import com.flipclassroom.dto.course.CourseDetailResponse;
import com.flipclassroom.dto.course.CourseListItemResponse;
import com.flipclassroom.dto.course.CourseMemberItemResponse;
import com.flipclassroom.dto.course.CreateCourseRequest;
import com.flipclassroom.dto.course.CreateCourseResponse;
import com.flipclassroom.dto.course.UpdateCourseRequest;
import com.flipclassroom.dto.course.UpdateCourseResponse;

import java.util.List;

public interface CourseService {

    List<CourseListItemResponse> listCourses(Long teacherId, Long memberUserId); // 查询课程列表

    CreateCourseResponse createCourse(CreateCourseRequest request); // 新增课程

    CourseDetailResponse getCourse(Long id); // 查询课程详情

    UpdateCourseResponse updateCourse(Long id, UpdateCourseRequest request); // 修改课程基础信息

    List<CourseMemberItemResponse> listCourseMembers(Long courseId); // 查询课程成员列表

    AddCourseMemberResponse addCourseMember(Long courseId, AddCourseMemberRequest request); // 添加课程成员
}
