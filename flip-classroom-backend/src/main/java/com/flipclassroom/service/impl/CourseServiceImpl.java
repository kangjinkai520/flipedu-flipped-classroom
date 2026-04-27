package com.flipclassroom.service.impl;

import com.flipclassroom.dto.course.AddCourseMemberRequest;
import com.flipclassroom.dto.course.AddCourseMemberResponse;
import com.flipclassroom.dto.course.CourseDetailResponse;
import com.flipclassroom.dto.course.CourseListItemResponse;
import com.flipclassroom.dto.course.CourseMemberItemResponse;
import com.flipclassroom.dto.course.CreateCourseRequest;
import com.flipclassroom.dto.course.CreateCourseResponse;
import com.flipclassroom.dto.course.UpdateCourseRequest;
import com.flipclassroom.dto.course.UpdateCourseResponse;
import com.flipclassroom.entity.Course;
import com.flipclassroom.entity.CourseMember;
import com.flipclassroom.entity.SysUser;
import com.flipclassroom.mapper.CourseMapper;
import com.flipclassroom.mapper.UserMapper;
import com.flipclassroom.service.CourseService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Locale;

@Service
public class CourseServiceImpl implements CourseService {

    private final CourseMapper courseMapper;
    private final UserMapper userMapper;

    public CourseServiceImpl(CourseMapper courseMapper, UserMapper userMapper) {
        this.courseMapper = courseMapper;
        this.userMapper = userMapper;
    }

    @Override
    public List<CourseListItemResponse> listCourses(Long teacherId, Long memberUserId) { // 查询课程列表并转换成前端返回格式
        if (teacherId != null && memberUserId != null) {
            throw new IllegalArgumentException("teacherId and memberUserId cannot be used together");
        }

        List<Course> courses;
        if (teacherId != null) {
            courses = courseMapper.findByTeacherId(teacherId);
        } else if (memberUserId != null) {
            courses = courseMapper.findByMemberUserId(memberUserId);
        } else {
            courses = courseMapper.findAll();
        }

        return courses
                .stream()
                .map(this::toListItem)
                .toList();
    }

    @Override
    public CreateCourseResponse createCourse(CreateCourseRequest request) { // 新增课程
        validateCreateRequest(request);

        if (courseMapper.findByCode(request.getCourseCode().trim()) != null) {
            throw new IllegalArgumentException("Course code already exists");
        }

        SysUser teacher = validateTeacher(request.getTeacherId());

        Course course = new Course();
        course.setTeacherId(teacher.getId());
        course.setCourseName(request.getCourseName().trim());
        course.setCourseCode(request.getCourseCode().trim());
        course.setTerm(request.getTerm().trim());
        course.setIntroduction(blankToNull(request.getIntroduction()));
        course.setStatus(normalizeStatus(request.getStatus(), 1));

        Long id = courseMapper.insert(course);
        if (id == null) {
            throw new IllegalStateException("Failed to create course");
        }

        return new CreateCourseResponse(
                id,
                course.getTeacherId(),
                course.getCourseName(),
                course.getCourseCode(),
                course.getTerm(),
                course.getIntroduction(),
                course.getStatus()
        );
    }

    @Override
    public CourseDetailResponse getCourse(Long id) { // 查询课程详情
        if (id == null) {
            throw new IllegalArgumentException("Course id cannot be empty");
        }

        Course course = courseMapper.findById(id);
        if (course == null) {
            throw new IllegalArgumentException("Course does not exist");
        }

        return toDetail(course);
    }

    @Override
    public UpdateCourseResponse updateCourse(Long id, UpdateCourseRequest request) { // 修改课程基础信息
        if (id == null) {
            throw new IllegalArgumentException("Course id cannot be empty");
        }
        validateUpdateRequest(request);

        Course existingCourse = courseMapper.findById(id);
        if (existingCourse == null) {
            throw new IllegalArgumentException("Course does not exist");
        }

        String normalizedCode = request.getCourseCode().trim();
        Course duplicatedCourse = courseMapper.findByCode(normalizedCode);
        if (duplicatedCourse != null && !duplicatedCourse.getId().equals(id)) {
            throw new IllegalArgumentException("Course code already exists");
        }

        SysUser teacher = validateTeacher(request.getTeacherId());

        existingCourse.setTeacherId(teacher.getId());
        existingCourse.setCourseName(request.getCourseName().trim());
        existingCourse.setCourseCode(normalizedCode);
        existingCourse.setTerm(request.getTerm().trim());
        existingCourse.setIntroduction(blankToNull(request.getIntroduction()));
        existingCourse.setStatus(normalizeStatus(request.getStatus(), existingCourse.getStatus()));

        int affectedRows = courseMapper.updateBasicInfo(existingCourse);
        if (affectedRows == 0) {
            throw new IllegalStateException("Failed to update course");
        }

        return new UpdateCourseResponse(
                existingCourse.getId(),
                existingCourse.getTeacherId(),
                existingCourse.getCourseName(),
                existingCourse.getCourseCode(),
                existingCourse.getTerm(),
                existingCourse.getIntroduction(),
                existingCourse.getStatus()
        );
    }

    @Override
    public List<CourseMemberItemResponse> listCourseMembers(Long courseId) { // 查询课程成员列表
        if (courseId == null) {
            throw new IllegalArgumentException("Course id cannot be empty");
        }
        ensureCourseExists(courseId);

        return courseMapper.findMembers(courseId)
                .stream()
                .map(this::toCourseMemberItem)
                .toList();
    }

    @Override
    public AddCourseMemberResponse addCourseMember(Long courseId, AddCourseMemberRequest request) { // 添加课程成员
        if (courseId == null) {
            throw new IllegalArgumentException("Course id cannot be empty");
        }
        if (request == null || request.getUserId() == null) {
            throw new IllegalArgumentException("User id cannot be empty");
        }

        Course course = ensureCourseExists(courseId);
        SysUser user = userMapper.findById(request.getUserId());
        if (user == null) {
            throw new IllegalArgumentException("User does not exist");
        }
        if (course.getTeacherId().equals(user.getId())) {
            throw new IllegalArgumentException("Teacher is already bound to the course");
        }
        if (courseMapper.findMember(courseId, request.getUserId()) != null) {
            throw new IllegalArgumentException("User is already a course member");
        }

        String normalizedMemberRole = normalizeMemberRole(request.getRole());
        validateMemberUserRole(user, normalizedMemberRole);

        CourseMember courseMember = new CourseMember();
        courseMember.setCourseId(courseId);
        courseMember.setUserId(user.getId());
        courseMember.setMemberRole(normalizedMemberRole.toUpperCase(Locale.ROOT));

        Long id = courseMapper.insertMember(courseMember);
        if (id == null) {
            throw new IllegalStateException("Failed to add course member");
        }

        return new AddCourseMemberResponse(id, courseId, user.getId(), normalizedMemberRole);
    }

    private CourseListItemResponse toListItem(Course course) { // 把数据库实体转换成课程列表返回对象
        return new CourseListItemResponse(
                course.getId(),
                course.getCourseName(),
                course.getCourseCode(),
                course.getTeacherId(),
                course.getTeacherName(),
                course.getTerm(),
                course.getIntroduction(),
                course.getStatus()
        );
    }

    private CourseDetailResponse toDetail(Course course) { // 把数据库实体转换成课程详情返回对象
        return new CourseDetailResponse(
                course.getId(),
                course.getTeacherId(),
                course.getTeacherName(),
                course.getTeacherUsername(),
                course.getCourseName(),
                course.getCourseCode(),
                course.getTerm(),
                course.getIntroduction(),
                course.getStatus()
        );
    }

    private CourseMemberItemResponse toCourseMemberItem(CourseMember courseMember) { // 把数据库实体转换成课程成员返回对象
        String normalizedMemberRole = courseMember.getMemberRole() == null
                ? null
                : courseMember.getMemberRole().toLowerCase(Locale.ROOT);
        String normalizedUserRole = courseMember.getUserRole() == null
                ? null
                : courseMember.getUserRole().toLowerCase(Locale.ROOT);
        return new CourseMemberItemResponse(
                courseMember.getId(),
                courseMember.getUserId(),
                courseMember.getUsername(),
                courseMember.getRealName(),
                normalizedMemberRole,
                normalizedUserRole,
                courseMember.getStatus()
        );
    }

    private void validateCreateRequest(CreateCourseRequest request) { // 校验新增课程请求
        if (request == null) {
            throw new IllegalArgumentException("Request body cannot be empty");
        }
        if (request.getTeacherId() == null || isBlank(request.getCourseName())
                || isBlank(request.getCourseCode()) || isBlank(request.getTerm())) {
            throw new IllegalArgumentException("TeacherId, courseName, courseCode and term are required");
        }
        normalizeStatus(request.getStatus(), 1);
    }

    private void validateUpdateRequest(UpdateCourseRequest request) { // 校验修改课程请求
        if (request == null) {
            throw new IllegalArgumentException("Request body cannot be empty");
        }
        if (request.getTeacherId() == null || isBlank(request.getCourseName())
                || isBlank(request.getCourseCode()) || isBlank(request.getTerm())) {
            throw new IllegalArgumentException("TeacherId, courseName, courseCode and term are required");
        }
        if (request.getStatus() == null) {
            throw new IllegalArgumentException("Status is required");
        }
        normalizeStatus(request.getStatus(), 1);
    }

    private Course ensureCourseExists(Long courseId) { // 确认课程存在
        Course course = courseMapper.findById(courseId);
        if (course == null) {
            throw new IllegalArgumentException("Course does not exist");
        }
        return course;
    }

    private SysUser validateTeacher(Long teacherId) { // 校验教师是否存在且角色正确
        SysUser teacher = userMapper.findById(teacherId);
        if (teacher == null) {
            throw new IllegalArgumentException("Teacher does not exist");
        }
        if (teacher.getRole() == null || !"TEACHER".equalsIgnoreCase(teacher.getRole())) {
            throw new IllegalArgumentException("TeacherId must point to a teacher user");
        }
        return teacher;
    }

    private void validateMemberUserRole(SysUser user, String memberRole) { // 校验成员角色与系统角色是否匹配
        String normalizedUserRole = user.getRole() == null
                ? ""
                : user.getRole().trim().toLowerCase(Locale.ROOT);
        if (!normalizedUserRole.equals(memberRole)) {
            throw new IllegalArgumentException("User role does not match member role");
        }
    }

    private String normalizeMemberRole(String role) { // 统一并校验课程成员角色
        String normalizedRole = isBlank(role) ? "student" : role.trim().toLowerCase(Locale.ROOT);
        if (!normalizedRole.equals("student") && !normalizedRole.equals("teacher")) {
            throw new IllegalArgumentException("Member role must be student or teacher");
        }
        return normalizedRole;
    }

    private Integer normalizeStatus(Integer status, Integer defaultStatus) { // 统一并校验课程状态
        Integer finalStatus = status == null ? defaultStatus : status;
        if (finalStatus != 0 && finalStatus != 1) {
            throw new IllegalArgumentException("Status must be 0 or 1");
        }
        return finalStatus;
    }

    private boolean isBlank(String value) { // 判断字符串是否为空或全是空格
        return value == null || value.trim().isEmpty();
    }

    private String blankToNull(String value) { // 把空字符串转换成 null，方便入库
        return isBlank(value) ? null : value.trim();
    }
}
