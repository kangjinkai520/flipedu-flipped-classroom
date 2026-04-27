package com.flipclassroom.controller;

import com.flipclassroom.common.Result;
import com.flipclassroom.dto.course.AddCourseMemberRequest;
import com.flipclassroom.dto.course.AddCourseMemberResponse;
import com.flipclassroom.dto.course.CourseDetailResponse;
import com.flipclassroom.dto.course.CourseListItemResponse;
import com.flipclassroom.dto.course.CourseMemberItemResponse;
import com.flipclassroom.dto.course.CreateCourseRequest;
import com.flipclassroom.dto.course.CreateCourseResponse;
import com.flipclassroom.dto.course.UpdateCourseRequest;
import com.flipclassroom.dto.course.UpdateCourseResponse;
import com.flipclassroom.service.CourseService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/courses")
public class CourseController {

    private final CourseService courseService;

    public CourseController(CourseService courseService) {
        this.courseService = courseService;
    }

    @GetMapping
    public Result<List<CourseListItemResponse>> listCourses(@RequestParam(required = false) Long teacherId,
                                                            @RequestParam(required = false) Long memberUserId) { // 查询课程列表
        try {
            return Result.success(courseService.listCourses(teacherId, memberUserId));
        } catch (RuntimeException exception) {
            return Result.fail(exception.getMessage());
        }
    }

    @PostMapping
    public Result<CreateCourseResponse> createCourse(@RequestBody CreateCourseRequest request) { // 新增课程
        try {
            return Result.success(courseService.createCourse(request));
        } catch (RuntimeException exception) {
            return Result.fail(exception.getMessage());
        }
    }

    @GetMapping("/{id}")
    public Result<CourseDetailResponse> getCourse(@PathVariable Long id) { // 查询课程详情
        try {
            return Result.success(courseService.getCourse(id));
        } catch (RuntimeException exception) {
            return Result.fail(exception.getMessage());
        }
    }

    @PutMapping("/{id}")
    public Result<UpdateCourseResponse> updateCourse(@PathVariable Long id,
                                                     @RequestBody UpdateCourseRequest request) { // 修改课程基础信息
        try {
            return Result.success(courseService.updateCourse(id, request));
        } catch (RuntimeException exception) {
            return Result.fail(exception.getMessage());
        }
    }

    @GetMapping("/{id}/members")
    public Result<List<CourseMemberItemResponse>> listCourseMembers(@PathVariable Long id) { // 查询课程成员列表
        try {
            return Result.success(courseService.listCourseMembers(id));
        } catch (RuntimeException exception) {
            return Result.fail(exception.getMessage());
        }
    }

    @PostMapping("/{id}/members")
    public Result<AddCourseMemberResponse> addCourseMember(@PathVariable Long id,
                                                           @RequestBody AddCourseMemberRequest request) { // 添加课程成员
        try {
            return Result.success(courseService.addCourseMember(id, request));
        } catch (RuntimeException exception) {
            return Result.fail(exception.getMessage());
        }
    }
}
