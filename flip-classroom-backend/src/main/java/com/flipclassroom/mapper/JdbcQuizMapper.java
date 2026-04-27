package com.flipclassroom.mapper;

import com.flipclassroom.entity.PreviewQuiz;
import com.flipclassroom.entity.QuizQuestion;
import com.flipclassroom.entity.QuizRecord;
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
public class JdbcQuizMapper implements QuizMapper {

    private static final String FIND_BY_COURSE_ID_SQL = """
            SELECT id, course_id, title, description, total_score, status, publish_time
            FROM preview_quiz
            WHERE course_id = ?
            ORDER BY id DESC
            """;
    private static final String FIND_BY_ID_SQL = """
            SELECT id, course_id, title, description, total_score, status, publish_time
            FROM preview_quiz
            WHERE id = ?
            LIMIT 1
            """;
    private static final String INSERT_QUIZ_SQL = """
            INSERT INTO preview_quiz (course_id, title, description, total_score, status, publish_time)
            VALUES (?, ?, ?, ?, ?, ?)
            """;
    private static final String UPDATE_QUIZ_STATUS_SQL = """
            UPDATE preview_quiz
            SET status = ?
            WHERE id = ?
            """;
    private static final String FIND_QUESTIONS_BY_QUIZ_ID_SQL = """
            SELECT id, quiz_id, type, title, option_a, option_b, option_c, option_d, correct_answer, score, sort_order
            FROM quiz_question
            WHERE quiz_id = ?
            ORDER BY sort_order ASC, id ASC
            """;
    private static final String INSERT_QUESTION_SQL = """
            INSERT INTO quiz_question (quiz_id, type, title, option_a, option_b, option_c, option_d, correct_answer, score, sort_order)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            """;
    private static final String FIND_RECORDS_BY_QUIZ_ID_SQL = """
            SELECT qr.id,
                   qr.quiz_id,
                   qr.student_id,
                   u.username AS student_username,
                   u.name AS student_name,
                   qr.score,
                   qr.status,
                   qr.answers_json,
                   qr.submitted_at
            FROM quiz_record qr
            LEFT JOIN sys_user u ON qr.student_id = u.id
            WHERE qr.quiz_id = ?
            ORDER BY qr.id ASC
            """;
    private static final String FIND_RECORD_SQL = """
            SELECT qr.id,
                   qr.quiz_id,
                   qr.student_id,
                   u.username AS student_username,
                   u.name AS student_name,
                   qr.score,
                   qr.status,
                   qr.answers_json,
                   qr.submitted_at
            FROM quiz_record qr
            LEFT JOIN sys_user u ON qr.student_id = u.id
            WHERE qr.quiz_id = ? AND qr.student_id = ?
            LIMIT 1
            """;
    private static final String INSERT_RECORD_SQL = """
            INSERT INTO quiz_record (quiz_id, student_id, score, status, answers_json, submitted_at)
            VALUES (?, ?, ?, ?, ?, ?)
            """;

    private final JdbcTemplate jdbcTemplate;

    public JdbcQuizMapper(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public List<PreviewQuiz> findByCourseId(Long courseId) {
        return jdbcTemplate.query(FIND_BY_COURSE_ID_SQL, new PreviewQuizRowMapper(), courseId);
    }

    @Override
    public PreviewQuiz findById(Long id) {
        return jdbcTemplate.query(FIND_BY_ID_SQL, new PreviewQuizRowMapper(), id)
                .stream()
                .findFirst()
                .orElse(null);
    }

    @Override
    public Long insertQuiz(PreviewQuiz quiz) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            var statement = connection.prepareStatement(INSERT_QUIZ_SQL, Statement.RETURN_GENERATED_KEYS);
            statement.setLong(1, quiz.getCourseId());
            statement.setString(2, quiz.getTitle());
            statement.setString(3, quiz.getDescription());
            statement.setInt(4, quiz.getTotalScore());
            statement.setInt(5, quiz.getStatus());
            statement.setString(6, quiz.getPublishTime());
            return statement;
        }, keyHolder);
        return keyHolder.getKey() == null ? null : keyHolder.getKey().longValue();
    }

    @Override
    public int updateQuizStatus(Long id, Integer status) {
        return jdbcTemplate.update(UPDATE_QUIZ_STATUS_SQL, status, id);
    }

    @Override
    public List<QuizQuestion> findQuestionsByQuizId(Long quizId) {
        return jdbcTemplate.query(FIND_QUESTIONS_BY_QUIZ_ID_SQL, new QuizQuestionRowMapper(), quizId);
    }

    @Override
    public void insertQuestion(QuizQuestion question) {
        jdbcTemplate.update(
                INSERT_QUESTION_SQL,
                question.getQuizId(),
                question.getType(),
                question.getTitle(),
                question.getOptionA(),
                question.getOptionB(),
                question.getOptionC(),
                question.getOptionD(),
                question.getCorrectAnswer(),
                question.getScore(),
                question.getSortOrder()
        );
    }

    @Override
    public List<QuizRecord> findRecordsByQuizId(Long quizId) {
        return jdbcTemplate.query(FIND_RECORDS_BY_QUIZ_ID_SQL, new QuizRecordRowMapper(), quizId);
    }

    @Override
    public QuizRecord findRecord(Long quizId, Long studentId) {
        return jdbcTemplate.query(FIND_RECORD_SQL, new QuizRecordRowMapper(), quizId, studentId)
                .stream()
                .findFirst()
                .orElse(null);
    }

    @Override
    public Long insertRecord(QuizRecord quizRecord) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            var statement = connection.prepareStatement(INSERT_RECORD_SQL, Statement.RETURN_GENERATED_KEYS);
            statement.setLong(1, quizRecord.getQuizId());
            statement.setLong(2, quizRecord.getStudentId());
            statement.setInt(3, quizRecord.getScore());
            statement.setString(4, quizRecord.getStatus());
            statement.setString(5, quizRecord.getAnswersJson());
            statement.setString(6, quizRecord.getSubmittedAt());
            return statement;
        }, keyHolder);
        return keyHolder.getKey() == null ? null : keyHolder.getKey().longValue();
    }

    private static class PreviewQuizRowMapper implements RowMapper<PreviewQuiz> {
        @Override
        public PreviewQuiz mapRow(ResultSet rs, int rowNum) throws SQLException {
            PreviewQuiz quiz = new PreviewQuiz();
            quiz.setId(rs.getLong("id"));
            quiz.setCourseId(rs.getLong("course_id"));
            quiz.setTitle(rs.getString("title"));
            quiz.setDescription(rs.getString("description"));
            quiz.setTotalScore(rs.getInt("total_score"));
            quiz.setStatus(rs.getInt("status"));
            quiz.setPublishTime(rs.getTimestamp("publish_time") == null ? null : rs.getTimestamp("publish_time").toString());
            return quiz;
        }
    }

    private static class QuizQuestionRowMapper implements RowMapper<QuizQuestion> {
        @Override
        public QuizQuestion mapRow(ResultSet rs, int rowNum) throws SQLException {
            QuizQuestion question = new QuizQuestion();
            question.setId(rs.getLong("id"));
            question.setQuizId(rs.getLong("quiz_id"));
            question.setType(rs.getString("type"));
            question.setTitle(rs.getString("title"));
            question.setOptionA(rs.getString("option_a"));
            question.setOptionB(rs.getString("option_b"));
            question.setOptionC(rs.getString("option_c"));
            question.setOptionD(rs.getString("option_d"));
            question.setCorrectAnswer(rs.getString("correct_answer"));
            question.setScore(rs.getInt("score"));
            question.setSortOrder(rs.getInt("sort_order"));
            return question;
        }
    }

    private static class QuizRecordRowMapper implements RowMapper<QuizRecord> {
        @Override
        public QuizRecord mapRow(ResultSet rs, int rowNum) throws SQLException {
            QuizRecord quizRecord = new QuizRecord();
            quizRecord.setId(rs.getLong("id"));
            quizRecord.setQuizId(rs.getLong("quiz_id"));
            quizRecord.setStudentId(rs.getLong("student_id"));
            quizRecord.setStudentUsername(rs.getString("student_username"));
            quizRecord.setStudentName(rs.getString("student_name"));
            quizRecord.setScore(rs.getInt("score"));
            quizRecord.setStatus(rs.getString("status"));
            quizRecord.setAnswersJson(rs.getString("answers_json"));
            quizRecord.setSubmittedAt(rs.getTimestamp("submitted_at") == null ? null : rs.getTimestamp("submitted_at").toString());
            return quizRecord;
        }
    }
}
