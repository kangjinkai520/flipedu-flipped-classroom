package com.flipclassroom.mapper;

import com.flipclassroom.entity.Notice;
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
public class JdbcNoticeMapper implements NoticeMapper {

    private static final String FIND_BY_COURSE_ID_SQL = """
            SELECT id, course_id, title, content, publish_time, status
            FROM notice
            WHERE course_id = ?
            ORDER BY id DESC
            """;
    private static final String FIND_BY_ID_SQL = """
            SELECT id, course_id, title, content, publish_time, status
            FROM notice
            WHERE id = ?
            LIMIT 1
            """;
    private static final String INSERT_SQL = """
            INSERT INTO notice (course_id, title, content, publish_time, status)
            VALUES (?, ?, ?, ?, ?)
            """;
    private static final String UPDATE_BASIC_INFO_SQL = """
            UPDATE notice
            SET title = ?, content = ?, publish_time = ?, status = ?
            WHERE id = ?
            """;
    private static final String UPDATE_STATUS_SQL = """
            UPDATE notice
            SET status = ?
            WHERE id = ?
            """;

    private final JdbcTemplate jdbcTemplate;

    public JdbcNoticeMapper(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public List<Notice> findByCourseId(Long courseId) {
        return jdbcTemplate.query(FIND_BY_COURSE_ID_SQL, new NoticeRowMapper(), courseId);
    }

    @Override
    public Notice findById(Long id) {
        return jdbcTemplate.query(FIND_BY_ID_SQL, new NoticeRowMapper(), id)
                .stream()
                .findFirst()
                .orElse(null);
    }

    @Override
    public Long insert(Notice notice) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            var statement = connection.prepareStatement(INSERT_SQL, Statement.RETURN_GENERATED_KEYS);
            statement.setLong(1, notice.getCourseId());
            statement.setString(2, notice.getTitle());
            statement.setString(3, notice.getContent());
            statement.setString(4, notice.getPublishTime());
            statement.setInt(5, notice.getStatus());
            return statement;
        }, keyHolder);
        return keyHolder.getKey() == null ? null : keyHolder.getKey().longValue();
    }

    @Override
    public int updateBasicInfo(Notice notice) {
        return jdbcTemplate.update(
                UPDATE_BASIC_INFO_SQL,
                notice.getTitle(),
                notice.getContent(),
                notice.getPublishTime(),
                notice.getStatus(),
                notice.getId()
        );
    }

    @Override
    public int updateStatus(Long id, Integer status) {
        return jdbcTemplate.update(UPDATE_STATUS_SQL, status, id);
    }

    private static class NoticeRowMapper implements RowMapper<Notice> {
        @Override
        public Notice mapRow(ResultSet rs, int rowNum) throws SQLException {
            Notice notice = new Notice();
            notice.setId(rs.getLong("id"));
            notice.setCourseId(rs.getLong("course_id"));
            notice.setTitle(rs.getString("title"));
            notice.setContent(rs.getString("content"));
            notice.setPublishTime(String.valueOf(rs.getTimestamp("publish_time")));
            notice.setStatus(rs.getInt("status"));
            return notice;
        }
    }
}
