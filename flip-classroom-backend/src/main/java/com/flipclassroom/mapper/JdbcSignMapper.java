package com.flipclassroom.mapper;

import com.flipclassroom.entity.SignRecord;
import com.flipclassroom.entity.SignTask;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.List;

@Repository
public class JdbcSignMapper implements SignMapper {

    private static final String FIND_TASKS_BY_COURSE_ID_SQL = """
            SELECT id, course_id, title, sign_code, starts_at, ends_at,
                   location_name, target_latitude, target_longitude, allowed_radius_meters, status
            FROM sign_task
            WHERE course_id = ?
            ORDER BY id DESC
            """;
    private static final String FIND_TASK_BY_ID_SQL = """
            SELECT id, course_id, title, sign_code, starts_at, ends_at,
                   location_name, target_latitude, target_longitude, allowed_radius_meters, status
            FROM sign_task
            WHERE id = ?
            LIMIT 1
            """;
    private static final String INSERT_TASK_SQL = """
            INSERT INTO sign_task (
                course_id, title, sign_code, starts_at, ends_at,
                location_name, target_latitude, target_longitude, allowed_radius_meters, status
            )
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            """;
    private static final String UPDATE_TASK_STATUS_SQL = """
            UPDATE sign_task
            SET status = ?
            WHERE id = ?
            """;
    private static final String FIND_RECORDS_BY_TASK_ID_SQL = """
            SELECT sr.id,
                   sr.sign_task_id,
                   sr.student_id,
                   u.username AS student_username,
                   u.name AS student_name,
                   sr.signed_at,
                   sr.latitude,
                   sr.longitude,
                   sr.distance_meters,
                   sr.sign_type,
                   sr.status,
                   sr.exception_reason,
                   sr.review_comment
            FROM sign_record sr
            LEFT JOIN sys_user u ON sr.student_id = u.id
            WHERE sr.sign_task_id = ?
            ORDER BY sr.id ASC
            """;
    private static final String FIND_RECORD_SQL = """
            SELECT sr.id,
                   sr.sign_task_id,
                   sr.student_id,
                   u.username AS student_username,
                   u.name AS student_name,
                   sr.signed_at,
                   sr.latitude,
                   sr.longitude,
                   sr.distance_meters,
                   sr.sign_type,
                   sr.status,
                   sr.exception_reason,
                   sr.review_comment
            FROM sign_record sr
            LEFT JOIN sys_user u ON sr.student_id = u.id
            WHERE sr.sign_task_id = ? AND sr.student_id = ?
            LIMIT 1
            """;
    private static final String FIND_RECORD_BY_ID_SQL = """
            SELECT sr.id,
                   sr.sign_task_id,
                   sr.student_id,
                   u.username AS student_username,
                   u.name AS student_name,
                   sr.signed_at,
                   sr.latitude,
                   sr.longitude,
                   sr.distance_meters,
                   sr.sign_type,
                   sr.status,
                   sr.exception_reason,
                   sr.review_comment
            FROM sign_record sr
            LEFT JOIN sys_user u ON sr.student_id = u.id
            WHERE sr.id = ?
            LIMIT 1
            """;
    private static final String INSERT_RECORD_SQL = """
            INSERT INTO sign_record (
                sign_task_id, student_id, signed_at, latitude, longitude,
                distance_meters, sign_type, status, exception_reason, review_comment
            )
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            """;
    private static final String UPDATE_RECORD_REVIEW_SQL = """
            UPDATE sign_record
            SET status = ?, review_comment = ?
            WHERE id = ?
            """;

    private final JdbcTemplate jdbcTemplate;

    public JdbcSignMapper(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public List<SignTask> findTasksByCourseId(Long courseId) {
        return jdbcTemplate.query(FIND_TASKS_BY_COURSE_ID_SQL, new SignTaskRowMapper(), courseId);
    }

    @Override
    public SignTask findTaskById(Long id) {
        return jdbcTemplate.query(FIND_TASK_BY_ID_SQL, new SignTaskRowMapper(), id)
                .stream()
                .findFirst()
                .orElse(null);
    }

    @Override
    public Long insertTask(SignTask signTask) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement statement = connection.prepareStatement(INSERT_TASK_SQL, Statement.RETURN_GENERATED_KEYS);
            statement.setLong(1, signTask.getCourseId());
            statement.setString(2, signTask.getTitle());
            statement.setString(3, signTask.getSignCode());
            statement.setString(4, signTask.getStartsAt());
            statement.setString(5, signTask.getEndsAt());
            statement.setString(6, signTask.getLocationName());
            statement.setObject(7, signTask.getTargetLatitude(), Types.DECIMAL);
            statement.setObject(8, signTask.getTargetLongitude(), Types.DECIMAL);
            statement.setObject(9, signTask.getAllowedRadiusMeters(), Types.INTEGER);
            statement.setString(10, signTask.getStatus());
            return statement;
        }, keyHolder);
        return keyHolder.getKey() == null ? null : keyHolder.getKey().longValue();
    }

    @Override
    public int updateTaskStatus(Long id, String status) {
        return jdbcTemplate.update(UPDATE_TASK_STATUS_SQL, status, id);
    }

    @Override
    public List<SignRecord> findRecordsByTaskId(Long signTaskId) {
        return jdbcTemplate.query(FIND_RECORDS_BY_TASK_ID_SQL, new SignRecordRowMapper(), signTaskId);
    }

    @Override
    public SignRecord findRecord(Long signTaskId, Long studentId) {
        return jdbcTemplate.query(FIND_RECORD_SQL, new SignRecordRowMapper(), signTaskId, studentId)
                .stream()
                .findFirst()
                .orElse(null);
    }

    @Override
    public SignRecord findRecordById(Long id) {
        return jdbcTemplate.query(FIND_RECORD_BY_ID_SQL, new SignRecordRowMapper(), id)
                .stream()
                .findFirst()
                .orElse(null);
    }

    @Override
    public Long insertRecord(SignRecord signRecord) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement statement = connection.prepareStatement(INSERT_RECORD_SQL, Statement.RETURN_GENERATED_KEYS);
            statement.setLong(1, signRecord.getSignTaskId());
            statement.setLong(2, signRecord.getStudentId());
            statement.setString(3, signRecord.getSignedAt());
            statement.setObject(4, signRecord.getLatitude(), Types.DECIMAL);
            statement.setObject(5, signRecord.getLongitude(), Types.DECIMAL);
            statement.setObject(6, signRecord.getDistanceMeters(), Types.DECIMAL);
            statement.setString(7, signRecord.getSignType());
            statement.setString(8, signRecord.getStatus());
            statement.setString(9, signRecord.getExceptionReason());
            statement.setString(10, signRecord.getReviewComment());
            return statement;
        }, keyHolder);
        return keyHolder.getKey() == null ? null : keyHolder.getKey().longValue();
    }

    @Override
    public int updateRecordReview(Long id, String status, String reviewComment) {
        return jdbcTemplate.update(UPDATE_RECORD_REVIEW_SQL, status, reviewComment, id);
    }

    private static class SignTaskRowMapper implements RowMapper<SignTask> {
        @Override
        public SignTask mapRow(ResultSet rs, int rowNum) throws SQLException {
            SignTask signTask = new SignTask();
            signTask.setId(rs.getLong("id"));
            signTask.setCourseId(rs.getLong("course_id"));
            signTask.setTitle(rs.getString("title"));
            signTask.setSignCode(rs.getString("sign_code"));
            signTask.setStartsAt(rs.getTimestamp("starts_at") == null ? null : rs.getTimestamp("starts_at").toString());
            signTask.setEndsAt(rs.getTimestamp("ends_at") == null ? null : rs.getTimestamp("ends_at").toString());
            signTask.setLocationName(rs.getString("location_name"));
            signTask.setTargetLatitude(toDouble(rs.getBigDecimal("target_latitude")));
            signTask.setTargetLongitude(toDouble(rs.getBigDecimal("target_longitude")));
            signTask.setAllowedRadiusMeters((Integer) rs.getObject("allowed_radius_meters"));
            signTask.setStatus(rs.getString("status"));
            return signTask;
        }
    }

    private static class SignRecordRowMapper implements RowMapper<SignRecord> {
        @Override
        public SignRecord mapRow(ResultSet rs, int rowNum) throws SQLException {
            SignRecord signRecord = new SignRecord();
            signRecord.setId(rs.getLong("id"));
            signRecord.setSignTaskId(rs.getLong("sign_task_id"));
            signRecord.setStudentId(rs.getLong("student_id"));
            signRecord.setStudentUsername(rs.getString("student_username"));
            signRecord.setStudentName(rs.getString("student_name"));
            signRecord.setSignedAt(rs.getTimestamp("signed_at") == null ? null : rs.getTimestamp("signed_at").toString());
            signRecord.setLatitude(toDouble(rs.getBigDecimal("latitude")));
            signRecord.setLongitude(toDouble(rs.getBigDecimal("longitude")));
            signRecord.setDistanceMeters(toDouble(rs.getBigDecimal("distance_meters")));
            signRecord.setSignType(rs.getString("sign_type"));
            signRecord.setStatus(rs.getString("status"));
            signRecord.setExceptionReason(rs.getString("exception_reason"));
            signRecord.setReviewComment(rs.getString("review_comment"));
            return signRecord;
        }
    }

    private static Double toDouble(BigDecimal value) {
        return value == null ? null : value.doubleValue();
    }
}
