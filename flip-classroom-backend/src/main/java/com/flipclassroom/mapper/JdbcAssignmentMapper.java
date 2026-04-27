package com.flipclassroom.mapper;

import com.flipclassroom.entity.Assignment;
import com.flipclassroom.entity.AssignmentSubmit;
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
public class JdbcAssignmentMapper implements AssignmentMapper {

    private static final String FIND_BY_COURSE_ID_SQL = """
            SELECT id, course_id, title, description, deadline, published
            FROM assignment
            WHERE course_id = ?
            ORDER BY id DESC
            """;
    private static final String FIND_BY_ID_SQL = """
            SELECT id, course_id, title, description, deadline, published
            FROM assignment
            WHERE id = ?
            LIMIT 1
            """;
    private static final String INSERT_SQL = """
            INSERT INTO assignment (course_id, title, description, deadline, published)
            VALUES (?, ?, ?, ?, ?)
            """;
    private static final String UPDATE_BASIC_INFO_SQL = """
            UPDATE assignment
            SET title = ?, description = ?, deadline = ?, published = ?
            WHERE id = ?
            """;
    private static final String UPDATE_PUBLISHED_SQL = """
            UPDATE assignment
            SET published = ?
            WHERE id = ?
            """;
    private static final String FIND_SUBMISSIONS_BY_ASSIGNMENT_ID_SQL = """
            SELECT s.id,
                   s.assignment_id,
                   s.student_id,
                   u.username AS student_username,
                   u.name AS student_name,
                   s.content,
                   s.attachment_url,
                   s.score,
                   s.feedback,
                   s.submitted_at
            FROM assignment_submit s
            LEFT JOIN sys_user u ON s.student_id = u.id
            WHERE s.assignment_id = ?
            ORDER BY s.id ASC
            """;
    private static final String FIND_SUBMISSION_SQL = """
            SELECT s.id,
                   s.assignment_id,
                   s.student_id,
                   u.username AS student_username,
                   u.name AS student_name,
                   s.content,
                   s.attachment_url,
                   s.score,
                   s.feedback,
                   s.submitted_at
            FROM assignment_submit s
            LEFT JOIN sys_user u ON s.student_id = u.id
            WHERE s.assignment_id = ? AND s.student_id = ?
            LIMIT 1
            """;
    private static final String FIND_SUBMISSION_BY_ID_SQL = """
            SELECT s.id,
                   s.assignment_id,
                   s.student_id,
                   u.username AS student_username,
                   u.name AS student_name,
                   s.content,
                   s.attachment_url,
                   s.score,
                   s.feedback,
                   s.submitted_at
            FROM assignment_submit s
            LEFT JOIN sys_user u ON s.student_id = u.id
            WHERE s.id = ?
            LIMIT 1
            """;
    private static final String INSERT_SUBMISSION_SQL = """
            INSERT INTO assignment_submit (assignment_id, student_id, content, attachment_url, score, feedback, submitted_at)
            VALUES (?, ?, ?, ?, ?, ?, ?)
            """;
    private static final String UPDATE_SUBMISSION_SQL = """
            UPDATE assignment_submit
            SET content = ?, attachment_url = ?, score = ?, feedback = ?, submitted_at = ?
            WHERE id = ?
            """;

    private final JdbcTemplate jdbcTemplate;

    public JdbcAssignmentMapper(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public List<Assignment> findByCourseId(Long courseId) {
        return jdbcTemplate.query(FIND_BY_COURSE_ID_SQL, new AssignmentRowMapper(), courseId);
    }

    @Override
    public Assignment findById(Long id) {
        return jdbcTemplate.query(FIND_BY_ID_SQL, new AssignmentRowMapper(), id)
                .stream()
                .findFirst()
                .orElse(null);
    }

    @Override
    public Long insert(Assignment assignment) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            var statement = connection.prepareStatement(INSERT_SQL, Statement.RETURN_GENERATED_KEYS);
            statement.setLong(1, assignment.getCourseId());
            statement.setString(2, assignment.getTitle());
            statement.setString(3, assignment.getDescription());
            statement.setString(4, assignment.getDeadline());
            statement.setInt(5, assignment.getPublished());
            return statement;
        }, keyHolder);
        return keyHolder.getKey() == null ? null : keyHolder.getKey().longValue();
    }

    @Override
    public int updateBasicInfo(Assignment assignment) {
        return jdbcTemplate.update(
                UPDATE_BASIC_INFO_SQL,
                assignment.getTitle(),
                assignment.getDescription(),
                assignment.getDeadline(),
                assignment.getPublished(),
                assignment.getId()
        );
    }

    @Override
    public int updatePublished(Long id, Integer published) {
        return jdbcTemplate.update(UPDATE_PUBLISHED_SQL, published, id);
    }

    @Override
    public List<AssignmentSubmit> findSubmissionsByAssignmentId(Long assignmentId) {
        return jdbcTemplate.query(FIND_SUBMISSIONS_BY_ASSIGNMENT_ID_SQL, new AssignmentSubmitRowMapper(), assignmentId);
    }

    @Override
    public AssignmentSubmit findSubmission(Long assignmentId, Long studentId) {
        return jdbcTemplate.query(FIND_SUBMISSION_SQL, new AssignmentSubmitRowMapper(), assignmentId, studentId)
                .stream()
                .findFirst()
                .orElse(null);
    }

    @Override
    public AssignmentSubmit findSubmissionById(Long id) {
        return jdbcTemplate.query(FIND_SUBMISSION_BY_ID_SQL, new AssignmentSubmitRowMapper(), id)
                .stream()
                .findFirst()
                .orElse(null);
    }

    @Override
    public Long insertSubmission(AssignmentSubmit assignmentSubmit) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            var statement = connection.prepareStatement(INSERT_SUBMISSION_SQL, Statement.RETURN_GENERATED_KEYS);
            statement.setLong(1, assignmentSubmit.getAssignmentId());
            statement.setLong(2, assignmentSubmit.getStudentId());
            statement.setString(3, assignmentSubmit.getContent());
            statement.setString(4, assignmentSubmit.getAttachmentUrl());
            statement.setObject(5, assignmentSubmit.getScore());
            statement.setString(6, assignmentSubmit.getFeedback());
            statement.setString(7, assignmentSubmit.getSubmittedAt());
            return statement;
        }, keyHolder);
        return keyHolder.getKey() == null ? null : keyHolder.getKey().longValue();
    }

    @Override
    public int updateSubmission(AssignmentSubmit assignmentSubmit) {
        return jdbcTemplate.update(
                UPDATE_SUBMISSION_SQL,
                assignmentSubmit.getContent(),
                assignmentSubmit.getAttachmentUrl(),
                assignmentSubmit.getScore(),
                assignmentSubmit.getFeedback(),
                assignmentSubmit.getSubmittedAt(),
                assignmentSubmit.getId()
        );
    }

    private static class AssignmentRowMapper implements RowMapper<Assignment> {
        @Override
        public Assignment mapRow(ResultSet rs, int rowNum) throws SQLException {
            Assignment assignment = new Assignment();
            assignment.setId(rs.getLong("id"));
            assignment.setCourseId(rs.getLong("course_id"));
            assignment.setTitle(rs.getString("title"));
            assignment.setDescription(rs.getString("description"));
            assignment.setDeadline(rs.getTimestamp("deadline") == null ? null : rs.getTimestamp("deadline").toString());
            assignment.setPublished(rs.getInt("published"));
            return assignment;
        }
    }

    private static class AssignmentSubmitRowMapper implements RowMapper<AssignmentSubmit> {
        @Override
        public AssignmentSubmit mapRow(ResultSet rs, int rowNum) throws SQLException {
            AssignmentSubmit assignmentSubmit = new AssignmentSubmit();
            assignmentSubmit.setId(rs.getLong("id"));
            assignmentSubmit.setAssignmentId(rs.getLong("assignment_id"));
            assignmentSubmit.setStudentId(rs.getLong("student_id"));
            assignmentSubmit.setStudentUsername(rs.getString("student_username"));
            assignmentSubmit.setStudentName(rs.getString("student_name"));
            assignmentSubmit.setContent(rs.getString("content"));
            assignmentSubmit.setAttachmentUrl(rs.getString("attachment_url"));
            assignmentSubmit.setScore((Integer) rs.getObject("score"));
            assignmentSubmit.setFeedback(rs.getString("feedback"));
            assignmentSubmit.setSubmittedAt(rs.getTimestamp("submitted_at") == null ? null : rs.getTimestamp("submitted_at").toString());
            return assignmentSubmit;
        }
    }
}
