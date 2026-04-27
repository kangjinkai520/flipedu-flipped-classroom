package com.flipclassroom.mapper;

import com.flipclassroom.entity.ScoreRecord;
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
public class JdbcScoreMapper implements ScoreMapper {

    private static final String FIND_BY_COURSE_ID_SQL = """
            SELECT sr.id,
                   sr.course_id,
                   sr.student_id,
                   u.username AS student_username,
                   u.name AS student_name,
                   sr.item_type,
                   sr.item_name,
                   sr.score,
                   sr.remark
            FROM score_record sr
            LEFT JOIN sys_user u ON sr.student_id = u.id
            WHERE sr.course_id = ?
            ORDER BY sr.student_id ASC, sr.id ASC
            """;
    private static final String FIND_BY_STUDENT_ID_SQL = """
            SELECT sr.id,
                   sr.course_id,
                   sr.student_id,
                   u.username AS student_username,
                   u.name AS student_name,
                   sr.item_type,
                   sr.item_name,
                   sr.score,
                   sr.remark
            FROM score_record sr
            LEFT JOIN sys_user u ON sr.student_id = u.id
            WHERE sr.student_id = ?
            ORDER BY sr.course_id ASC, sr.id ASC
            """;
    private static final String FIND_BY_STUDENT_ID_AND_COURSE_ID_SQL = """
            SELECT sr.id,
                   sr.course_id,
                   sr.student_id,
                   u.username AS student_username,
                   u.name AS student_name,
                   sr.item_type,
                   sr.item_name,
                   sr.score,
                   sr.remark
            FROM score_record sr
            LEFT JOIN sys_user u ON sr.student_id = u.id
            WHERE sr.student_id = ? AND sr.course_id = ?
            ORDER BY sr.id ASC
            """;
    private static final String FIND_BY_ITEM_SQL = """
            SELECT sr.id,
                   sr.course_id,
                   sr.student_id,
                   u.username AS student_username,
                   u.name AS student_name,
                   sr.item_type,
                   sr.item_name,
                   sr.score,
                   sr.remark
            FROM score_record sr
            LEFT JOIN sys_user u ON sr.student_id = u.id
            WHERE sr.course_id = ? AND sr.student_id = ? AND sr.item_type = ? AND sr.item_name = ?
            LIMIT 1
            """;
    private static final String INSERT_SQL = """
            INSERT INTO score_record (course_id, student_id, item_type, item_name, score, remark)
            VALUES (?, ?, ?, ?, ?, ?)
            """;
    private static final String UPDATE_SQL = """
            UPDATE score_record
            SET course_id = ?, student_id = ?, item_type = ?, item_name = ?, score = ?, remark = ?
            WHERE id = ?
            """;

    private final JdbcTemplate jdbcTemplate;

    public JdbcScoreMapper(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public List<ScoreRecord> findByCourseId(Long courseId) {
        return jdbcTemplate.query(FIND_BY_COURSE_ID_SQL, new ScoreRecordRowMapper(), courseId);
    }

    @Override
    public List<ScoreRecord> findByStudentId(Long studentId) {
        return jdbcTemplate.query(FIND_BY_STUDENT_ID_SQL, new ScoreRecordRowMapper(), studentId);
    }

    @Override
    public List<ScoreRecord> findByStudentIdAndCourseId(Long studentId, Long courseId) {
        return jdbcTemplate.query(FIND_BY_STUDENT_ID_AND_COURSE_ID_SQL, new ScoreRecordRowMapper(), studentId, courseId);
    }

    @Override
    public ScoreRecord findByItem(Long courseId, Long studentId, String itemType, String itemName) {
        return jdbcTemplate.query(FIND_BY_ITEM_SQL, new ScoreRecordRowMapper(), courseId, studentId, itemType, itemName)
                .stream()
                .findFirst()
                .orElse(null);
    }

    @Override
    public Long insert(ScoreRecord scoreRecord) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            var statement = connection.prepareStatement(INSERT_SQL, Statement.RETURN_GENERATED_KEYS);
            statement.setLong(1, scoreRecord.getCourseId());
            statement.setLong(2, scoreRecord.getStudentId());
            statement.setString(3, scoreRecord.getItemType());
            statement.setString(4, scoreRecord.getItemName());
            statement.setInt(5, scoreRecord.getScore());
            statement.setString(6, scoreRecord.getRemark());
            return statement;
        }, keyHolder);
        return keyHolder.getKey() == null ? null : keyHolder.getKey().longValue();
    }

    @Override
    public int update(ScoreRecord scoreRecord) {
        return jdbcTemplate.update(
                UPDATE_SQL,
                scoreRecord.getCourseId(),
                scoreRecord.getStudentId(),
                scoreRecord.getItemType(),
                scoreRecord.getItemName(),
                scoreRecord.getScore(),
                scoreRecord.getRemark(),
                scoreRecord.getId()
        );
    }

    private static class ScoreRecordRowMapper implements RowMapper<ScoreRecord> {
        @Override
        public ScoreRecord mapRow(ResultSet rs, int rowNum) throws SQLException {
            ScoreRecord scoreRecord = new ScoreRecord();
            scoreRecord.setId(rs.getLong("id"));
            scoreRecord.setCourseId(rs.getLong("course_id"));
            scoreRecord.setStudentId(rs.getLong("student_id"));
            scoreRecord.setStudentUsername(rs.getString("student_username"));
            scoreRecord.setStudentName(rs.getString("student_name"));
            scoreRecord.setItemType(rs.getString("item_type"));
            scoreRecord.setItemName(rs.getString("item_name"));
            scoreRecord.setScore(rs.getInt("score"));
            scoreRecord.setRemark(rs.getString("remark"));
            return scoreRecord;
        }
    }
}
