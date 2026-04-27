package com.flipclassroom.service.impl;

import com.flipclassroom.dto.review.CreateReviewRequest;
import com.flipclassroom.dto.review.ReviewDecisionRequest;
import com.flipclassroom.dto.review.ReviewRequestItemResponse;
import com.flipclassroom.service.ReviewService;
import jakarta.annotation.PostConstruct;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Service;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Service
public class ReviewServiceImpl implements ReviewService {

    private static final DateTimeFormatter TIMESTAMP_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final JdbcTemplate jdbcTemplate;

    public ReviewServiceImpl(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @PostConstruct
    public void ensureTable() {
        jdbcTemplate.execute("""
                CREATE TABLE IF NOT EXISTS review_request (
                    id BIGINT PRIMARY KEY AUTO_INCREMENT,
                    target_type VARCHAR(32) NOT NULL,
                    target_id BIGINT NOT NULL,
                    action_type VARCHAR(32) NOT NULL,
                    requester_id BIGINT NULL,
                    title VARCHAR(128) NOT NULL,
                    summary TEXT NULL,
                    payload TEXT NULL,
                    status VARCHAR(32) NOT NULL DEFAULT 'PENDING',
                    review_comment VARCHAR(255) NULL,
                    created_at TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP,
                    reviewed_at TIMESTAMP NULL DEFAULT NULL,
                    INDEX idx_review_request_status (status),
                    INDEX idx_review_request_target (target_type, target_id)
                )
                """);
        jdbcTemplate.update("""
                UPDATE course c
                SET c.status = -1
                WHERE c.status = 0
                  AND EXISTS (
                      SELECT 1 FROM review_request rr
                      WHERE rr.target_type = 'COURSE'
                        AND rr.target_id = c.id
                        AND rr.status = 'REJECTED'
                  )
                  AND NOT EXISTS (
                      SELECT 1 FROM review_request rr
                      WHERE rr.target_type = 'COURSE'
                        AND rr.target_id = c.id
                        AND rr.status = 'PENDING'
                  )
                """);
    }

    @Override
    public ReviewRequestItemResponse createReview(CreateReviewRequest request) {
        validateCreate(request);
        String targetType = normalize(request.getTargetType());
        String actionType = normalize(request.getActionType());

        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            var statement = connection.prepareStatement("""
                    INSERT INTO review_request
                    (target_type, target_id, action_type, requester_id, title, summary, payload, status, created_at)
                    VALUES (?, ?, ?, ?, ?, ?, ?, 'PENDING', ?)
                    """, Statement.RETURN_GENERATED_KEYS);
            statement.setString(1, targetType);
            statement.setLong(2, request.getTargetId());
            statement.setString(3, actionType);
            statement.setObject(4, request.getRequesterId());
            statement.setString(5, request.getTitle().trim());
            statement.setString(6, blankToNull(request.getSummary()));
            statement.setString(7, blankToNull(request.getPayload()));
            statement.setString(8, now());
            return statement;
        }, keyHolder);
        Long id = keyHolder.getKey() == null ? null : keyHolder.getKey().longValue();
        if (id == null) {
            throw new IllegalStateException("Failed to create review request");
        }
        return findById(id);
    }

    @Override
    public List<ReviewRequestItemResponse> listReviews(String status) {
        String baseSql = """
                SELECT rr.id, rr.target_type, rr.target_id, rr.action_type, rr.requester_id,
                       u.name AS requester_name, rr.title, rr.summary, rr.payload, rr.status,
                       rr.review_comment, rr.created_at, rr.reviewed_at
                FROM review_request rr
                LEFT JOIN sys_user u ON rr.requester_id = u.id
                """;
        if (isBlank(status)) {
            return jdbcTemplate.query(baseSql + " ORDER BY rr.id DESC", new ReviewRowMapper());
        }
        return jdbcTemplate.query(baseSql + " WHERE rr.status = ? ORDER BY rr.id DESC",
                new ReviewRowMapper(), normalize(status));
    }

    @Override
    public ReviewRequestItemResponse review(Long id, ReviewDecisionRequest request) {
        if (id == null) {
            throw new IllegalArgumentException("Review id cannot be empty");
        }
        if (request == null || isBlank(request.getStatus())) {
            throw new IllegalArgumentException("Review status is required");
        }
        String status = normalize(request.getStatus());
        if (!"APPROVED".equals(status) && !"REJECTED".equals(status)) {
            throw new IllegalArgumentException("Review status must be APPROVED or REJECTED");
        }

        ReviewRequestItemResponse existing = findById(id);
        if (existing == null) {
            throw new IllegalArgumentException("Review request does not exist");
        }
        if (!"PENDING".equalsIgnoreCase(existing.getStatus())) {
            throw new IllegalArgumentException("Review request has already been processed");
        }

        if ("APPROVED".equals(status)) {
            applyApprovedTarget(existing);
        } else {
            applyRejectedTarget(existing);
        }

        int affectedRows = jdbcTemplate.update("""
                UPDATE review_request
                SET status = ?, review_comment = ?, reviewed_at = ?
                WHERE id = ?
                """, status, blankToNull(request.getReviewComment()), now(), id);
        if (affectedRows == 0) {
            throw new IllegalStateException("Failed to update review request");
        }
        return findById(id);
    }

    private void applyApprovedTarget(ReviewRequestItemResponse review) {
        String targetType = normalize(review.getTargetType());
        if ("COURSE".equals(targetType)) {
            jdbcTemplate.update("UPDATE course SET status = 1 WHERE id = ?", review.getTargetId());
            return;
        }
        if ("MATERIAL".equals(targetType)) {
            jdbcTemplate.update("UPDATE teaching_material SET status = 1 WHERE id = ?", review.getTargetId());
            return;
        }
        if ("NOTICE".equals(targetType)) {
            jdbcTemplate.update("UPDATE notice SET status = 1 WHERE id = ?", review.getTargetId());
            return;
        }
        if ("QUIZ".equals(targetType)) {
            jdbcTemplate.update("UPDATE preview_quiz SET status = 1 WHERE id = ?", review.getTargetId());
            return;
        }
        if ("ASSIGNMENT".equals(targetType)) {
            jdbcTemplate.update("UPDATE assignment SET published = 1 WHERE id = ?", review.getTargetId());
            return;
        }
        if ("COURSE_MEMBER".equals(targetType)) {
            Map<String, String> payload = parsePayload(review.getPayload());
            Long userId = Long.valueOf(payload.get("userId"));
            String role = payload.getOrDefault("role", "student").toUpperCase(Locale.ROOT);
            Integer count = jdbcTemplate.queryForObject(
                    "SELECT COUNT(*) FROM course_member WHERE course_id = ? AND user_id = ?",
                    Integer.class, review.getTargetId(), userId);
            if (count == null || count == 0) {
                jdbcTemplate.update("INSERT INTO course_member (course_id, user_id, role) VALUES (?, ?, ?)",
                        review.getTargetId(), userId, role);
            }
        }
    }

    private void applyRejectedTarget(ReviewRequestItemResponse review) {
        String targetType = normalize(review.getTargetType());
        if ("COURSE".equals(targetType)) {
            jdbcTemplate.update("UPDATE course SET status = -1 WHERE id = ?", review.getTargetId());
        }
    }

    private ReviewRequestItemResponse findById(Long id) {
        return jdbcTemplate.query("""
                        SELECT rr.id, rr.target_type, rr.target_id, rr.action_type, rr.requester_id,
                               u.name AS requester_name, rr.title, rr.summary, rr.payload, rr.status,
                               rr.review_comment, rr.created_at, rr.reviewed_at
                        FROM review_request rr
                        LEFT JOIN sys_user u ON rr.requester_id = u.id
                        WHERE rr.id = ?
                        LIMIT 1
                        """, new ReviewRowMapper(), id)
                .stream()
                .findFirst()
                .orElse(null);
    }

    private void validateCreate(CreateReviewRequest request) {
        if (request == null || isBlank(request.getTargetType()) || request.getTargetId() == null
                || isBlank(request.getActionType()) || isBlank(request.getTitle())) {
            throw new IllegalArgumentException("Review targetType, targetId, actionType and title are required");
        }
    }

    private Map<String, String> parsePayload(String payload) {
        Map<String, String> result = new LinkedHashMap<>();
        if (isBlank(payload)) {
            return result;
        }
        for (String part : payload.split(";")) {
            String[] pair = part.split("=", 2);
            if (pair.length == 2) {
                result.put(pair[0].trim(), pair[1].trim());
            }
        }
        return result;
    }

    private String normalize(String value) {
        return value == null ? "" : value.trim().toUpperCase(Locale.ROOT);
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private String blankToNull(String value) {
        return isBlank(value) ? null : value.trim();
    }

    private String now() {
        return LocalDateTime.now().format(TIMESTAMP_FORMATTER);
    }

    private static class ReviewRowMapper implements RowMapper<ReviewRequestItemResponse> {
        @Override
        public ReviewRequestItemResponse mapRow(ResultSet rs, int rowNum) throws SQLException {
            return new ReviewRequestItemResponse(
                    rs.getLong("id"),
                    rs.getString("target_type"),
                    rs.getLong("target_id"),
                    rs.getString("action_type"),
                    rs.getObject("requester_id") == null ? null : rs.getLong("requester_id"),
                    rs.getString("requester_name"),
                    rs.getString("title"),
                    rs.getString("summary"),
                    rs.getString("payload"),
                    rs.getString("status"),
                    rs.getString("review_comment"),
                    rs.getTimestamp("created_at") == null ? null : rs.getTimestamp("created_at").toString(),
                    rs.getTimestamp("reviewed_at") == null ? null : rs.getTimestamp("reviewed_at").toString()
            );
        }
    }
}
