package com.flipclassroom.mapper;

import com.flipclassroom.entity.CourseFeedback;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.List;

@Repository
public class JdbcCourseFeedbackMapper implements CourseFeedbackMapper {

    private static final String BASE_SELECT = """
            SELECT cf.id,
                   cf.course_id,
                   cf.student_id,
                   u.username AS student_username,
                   u.name AS student_name,
                   cf.lesson_title,
                   cf.rating,
                   cf.mastery_level,
                   cf.content,
                   cf.suggestion,
                   cf.created_at
            FROM course_feedback cf
            LEFT JOIN sys_user u ON cf.student_id = u.id
            """;
    private static final String FIND_BY_COURSE_ID_SQL = BASE_SELECT + """
            WHERE cf.course_id = ?
            ORDER BY cf.created_at DESC, cf.id DESC
            """;
    private static final String FIND_BY_COURSE_AND_STUDENT_SQL = BASE_SELECT + """
            WHERE cf.course_id = ? AND cf.student_id = ?
            ORDER BY cf.created_at DESC, cf.id DESC
            """;
    private static final String INSERT_SQL = """
            INSERT INTO course_feedback
                (course_id, student_id, lesson_title, rating, mastery_level, content, suggestion)
            VALUES (?, ?, ?, ?, ?, ?, ?)
            """;

    private final JdbcTemplate jdbcTemplate;

    public JdbcCourseFeedbackMapper(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public List<CourseFeedback> findByCourseId(Long courseId) {
        return jdbcTemplate.query(FIND_BY_COURSE_ID_SQL, new CourseFeedbackRowMapper(), courseId);
    }

    @Override
    public List<CourseFeedback> findByCourseAndStudent(Long courseId, Long studentId) {
        return jdbcTemplate.query(FIND_BY_COURSE_AND_STUDENT_SQL, new CourseFeedbackRowMapper(), courseId, studentId);
    }

    @Override
    public Long insert(CourseFeedback feedback) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            var statement = connection.prepareStatement(INSERT_SQL, Statement.RETURN_GENERATED_KEYS);
            statement.setLong(1, feedback.getCourseId());
            statement.setLong(2, feedback.getStudentId());
            statement.setString(3, feedback.getLessonTitle());
            statement.setInt(4, feedback.getRating());
            statement.setString(5, feedback.getMasteryLevel());
            statement.setString(6, feedback.getContent());
            statement.setString(7, feedback.getSuggestion());
            return statement;
        }, keyHolder);
        return keyHolder.getKey() == null ? null : keyHolder.getKey().longValue();
    }

    private static class CourseFeedbackRowMapper implements RowMapper<CourseFeedback> {
        @Override
        public CourseFeedback mapRow(ResultSet rs, int rowNum) throws SQLException {
            CourseFeedback feedback = new CourseFeedback();
            feedback.setId(rs.getLong("id"));
            feedback.setCourseId(rs.getLong("course_id"));
            feedback.setStudentId(rs.getLong("student_id"));
            feedback.setStudentUsername(rs.getString("student_username"));
            feedback.setStudentName(rs.getString("student_name"));
            feedback.setLessonTitle(rs.getString("lesson_title"));
            feedback.setRating(rs.getInt("rating"));
            feedback.setMasteryLevel(rs.getString("mastery_level"));
            feedback.setContent(rs.getString("content"));
            feedback.setSuggestion(rs.getString("suggestion"));
            feedback.setCreatedAt(formatTimestamp(rs.getTimestamp("created_at")));
            return feedback;
        }

        private String formatTimestamp(Timestamp timestamp) {
            return timestamp == null ? null : String.valueOf(timestamp);
        }
    }
}
