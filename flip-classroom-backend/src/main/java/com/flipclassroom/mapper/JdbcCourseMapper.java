package com.flipclassroom.mapper;

import com.flipclassroom.entity.Course;
import com.flipclassroom.entity.CourseMember;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

@Repository
public class JdbcCourseMapper implements CourseMapper {

    private static final String FIND_ALL_SQL = """
            SELECT c.id,
                   c.teacher_id,
                   u.username AS teacher_username,
                   u.name AS teacher_name,
                   c.name AS course_name,
                   c.code AS course_code,
                   c.description AS introduction,
                   c.term,
                   c.status
            FROM course c
            LEFT JOIN sys_user u ON c.teacher_id = u.id
            ORDER BY c.id ASC
            """;
    private static final String FIND_BY_TEACHER_ID_SQL = """
            SELECT c.id,
                   c.teacher_id,
                   u.username AS teacher_username,
                   u.name AS teacher_name,
                   c.name AS course_name,
                   c.code AS course_code,
                   c.description AS introduction,
                   c.term,
                   c.status
            FROM course c
            LEFT JOIN sys_user u ON c.teacher_id = u.id
            WHERE c.teacher_id = ?
            ORDER BY c.id ASC
            """;
    private static final String FIND_BY_MEMBER_USER_ID_SQL = """
            SELECT DISTINCT c.id,
                   c.teacher_id,
                   tu.username AS teacher_username,
                   tu.name AS teacher_name,
                   c.name AS course_name,
                   c.code AS course_code,
                   c.description AS introduction,
                   c.term,
                   c.status
            FROM course c
            INNER JOIN course_member cm ON c.id = cm.course_id
            LEFT JOIN sys_user tu ON c.teacher_id = tu.id
            WHERE cm.user_id = ?
            ORDER BY c.id ASC
            """;
    private static final String FIND_BY_ID_SQL = """
            SELECT c.id,
                   c.teacher_id,
                   u.username AS teacher_username,
                   u.name AS teacher_name,
                   c.name AS course_name,
                   c.code AS course_code,
                   c.description AS introduction,
                   c.term,
                   c.status
            FROM course c
            LEFT JOIN sys_user u ON c.teacher_id = u.id
            WHERE c.id = ?
            LIMIT 1
            """;
    private static final String FIND_BY_CODE_SQL = """
            SELECT c.id,
                   c.teacher_id,
                   u.username AS teacher_username,
                   u.name AS teacher_name,
                   c.name AS course_name,
                   c.code AS course_code,
                   c.description AS introduction,
                   c.term,
                   c.status
            FROM course c
            LEFT JOIN sys_user u ON c.teacher_id = u.id
            WHERE c.code = ?
            LIMIT 1
            """;
    private static final String INSERT_SQL = """
            INSERT INTO course (teacher_id, name, code, description, term, status)
            VALUES (?, ?, ?, ?, ?, ?)
            """;
    private static final String UPDATE_BASIC_INFO_SQL = """
            UPDATE course
            SET teacher_id = ?, name = ?, code = ?, description = ?, term = ?, status = ?
            WHERE id = ?
            """;
    private static final String FIND_MEMBERS_SQL = """
            SELECT cm.id,
                   cm.course_id,
                   cm.user_id,
                   cm.role AS member_role,
                   u.username,
                   u.name AS real_name,
                   u.role AS user_role,
                   u.status
            FROM course_member cm
            LEFT JOIN sys_user u ON cm.user_id = u.id
            WHERE cm.course_id = ?
            ORDER BY cm.id ASC
            """;
    private static final String FIND_MEMBER_SQL = """
            SELECT cm.id,
                   cm.course_id,
                   cm.user_id,
                   cm.role AS member_role,
                   u.username,
                   u.name AS real_name,
                   u.role AS user_role,
                   u.status
            FROM course_member cm
            LEFT JOIN sys_user u ON cm.user_id = u.id
            WHERE cm.course_id = ? AND cm.user_id = ?
            LIMIT 1
            """;
    private static final String INSERT_MEMBER_SQL = """
            INSERT INTO course_member (course_id, user_id, role)
            VALUES (?, ?, ?)
            """;

    private final JdbcTemplate jdbcTemplate;

    public JdbcCourseMapper(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public List<Course> findAll() { // 查询课程列表
        return jdbcTemplate.query(FIND_ALL_SQL, new CourseRowMapper());
    }

    @Override
    public List<Course> findByTeacherId(Long teacherId) { // 按教师查询课程列表
        return jdbcTemplate.query(FIND_BY_TEACHER_ID_SQL, new CourseRowMapper(), teacherId);
    }

    @Override
    public List<Course> findByMemberUserId(Long memberUserId) { // 按课程成员查询课程列表
        return jdbcTemplate.query(FIND_BY_MEMBER_USER_ID_SQL, new CourseRowMapper(), memberUserId);
    }

    @Override
    public Course findById(Long id) { // 按 id 查询课程
        return jdbcTemplate.query(FIND_BY_ID_SQL, new CourseRowMapper(), id)
                .stream()
                .findFirst()
                .orElse(null);
    }

    @Override
    public Course findByCode(String courseCode) { // 按课程编码查询课程
        return jdbcTemplate.query(FIND_BY_CODE_SQL, new CourseRowMapper(), courseCode)
                .stream()
                .findFirst()
                .orElse(null);
    }

    @Override
    public Long insert(Course course) { // 插入课程并返回新记录 id
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            var statement = connection.prepareStatement(INSERT_SQL, Statement.RETURN_GENERATED_KEYS);
            statement.setLong(1, course.getTeacherId());
            statement.setString(2, course.getCourseName());
            statement.setString(3, course.getCourseCode());
            statement.setString(4, course.getIntroduction());
            statement.setString(5, course.getTerm());
            statement.setInt(6, course.getStatus());
            return statement;
        }, keyHolder);
        return keyHolder.getKey() == null ? null : keyHolder.getKey().longValue();
    }

    @Override
    public int updateBasicInfo(Course course) { // 更新课程基础信息
        return jdbcTemplate.update(
                UPDATE_BASIC_INFO_SQL,
                course.getTeacherId(),
                course.getCourseName(),
                course.getCourseCode(),
                course.getIntroduction(),
                course.getTerm(),
                course.getStatus(),
                course.getId()
        );
    }

    @Override
    public List<CourseMember> findMembers(Long courseId) { // 查询课程成员列表
        return jdbcTemplate.query(FIND_MEMBERS_SQL, new CourseMemberRowMapper(), courseId);
    }

    @Override
    public CourseMember findMember(Long courseId, Long userId) { // 查询课程内是否已存在某成员
        return jdbcTemplate.query(FIND_MEMBER_SQL, new CourseMemberRowMapper(), courseId, userId)
                .stream()
                .findFirst()
                .orElse(null);
    }

    @Override
    public Long insertMember(CourseMember courseMember) { // 添加课程成员并返回新记录 id
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            var statement = connection.prepareStatement(INSERT_MEMBER_SQL, Statement.RETURN_GENERATED_KEYS);
            statement.setLong(1, courseMember.getCourseId());
            statement.setLong(2, courseMember.getUserId());
            statement.setString(3, courseMember.getMemberRole());
            return statement;
        }, keyHolder);
        return keyHolder.getKey() == null ? null : keyHolder.getKey().longValue();
    }

    private static class CourseRowMapper implements RowMapper<Course> {
        @Override
        public Course mapRow(ResultSet rs, int rowNum) throws SQLException { // 把查询结果映射成 Course 对象
            Course course = new Course();
            course.setId(rs.getLong("id"));
            course.setTeacherId(rs.getLong("teacher_id"));
            course.setTeacherUsername(rs.getString("teacher_username"));
            course.setTeacherName(rs.getString("teacher_name"));
            course.setCourseName(rs.getString("course_name"));
            course.setCourseCode(rs.getString("course_code"));
            course.setIntroduction(rs.getString("introduction"));
            course.setTerm(rs.getString("term"));
            course.setStatus(rs.getInt("status"));
            return course;
        }
    }

    private static class CourseMemberRowMapper implements RowMapper<CourseMember> {
        @Override
        public CourseMember mapRow(ResultSet rs, int rowNum) throws SQLException { // 把查询结果映射成 CourseMember 对象
            CourseMember courseMember = new CourseMember();
            courseMember.setId(rs.getLong("id"));
            courseMember.setCourseId(rs.getLong("course_id"));
            courseMember.setUserId(rs.getLong("user_id"));
            courseMember.setMemberRole(rs.getString("member_role"));
            courseMember.setUsername(rs.getString("username"));
            courseMember.setRealName(rs.getString("real_name"));
            courseMember.setUserRole(rs.getString("user_role"));
            courseMember.setStatus(rs.getInt("status"));
            return courseMember;
        }
    }
}
