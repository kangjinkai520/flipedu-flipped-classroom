package com.flipclassroom.mapper;

import com.flipclassroom.entity.TeachingMaterial;
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
public class JdbcMaterialMapper implements MaterialMapper {

    private static final String FIND_BY_COURSE_ID_SQL = """
            SELECT id, course_id, title, material_type, description, material_url, publish_time, status
            FROM teaching_material
            WHERE course_id = ?
            ORDER BY id ASC
            """;
    private static final String FIND_BY_ID_SQL = """
            SELECT id, course_id, title, material_type, description, material_url, publish_time, status
            FROM teaching_material
            WHERE id = ?
            LIMIT 1
            """;
    private static final String INSERT_SQL = """
            INSERT INTO teaching_material (course_id, title, material_type, description, material_url, publish_time, status)
            VALUES (?, ?, ?, ?, ?, ?, ?)
            """;
    private static final String UPDATE_BASIC_INFO_SQL = """
            UPDATE teaching_material
            SET title = ?, material_type = ?, description = ?, material_url = ?, publish_time = ?, status = ?
            WHERE id = ?
            """;
    private static final String UPDATE_STATUS_SQL = """
            UPDATE teaching_material
            SET status = ?
            WHERE id = ?
            """;

    private final JdbcTemplate jdbcTemplate;

    public JdbcMaterialMapper(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public List<TeachingMaterial> findByCourseId(Long courseId) { // 按课程查询教学资料列表
        return jdbcTemplate.query(FIND_BY_COURSE_ID_SQL, new TeachingMaterialRowMapper(), courseId);
    }

    @Override
    public TeachingMaterial findById(Long id) { // 按 id 查询教学资料
        return jdbcTemplate.query(FIND_BY_ID_SQL, new TeachingMaterialRowMapper(), id)
                .stream()
                .findFirst()
                .orElse(null);
    }

    @Override
    public Long insert(TeachingMaterial material) { // 插入教学资料并返回新记录 id
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            var statement = connection.prepareStatement(INSERT_SQL, Statement.RETURN_GENERATED_KEYS);
            statement.setLong(1, material.getCourseId());
            statement.setString(2, material.getTitle());
            statement.setString(3, material.getMaterialType());
            statement.setString(4, material.getDescription());
            statement.setString(5, material.getMaterialUrl());
            statement.setString(6, material.getPublishTime());
            statement.setInt(7, material.getStatus());
            return statement;
        }, keyHolder);
        return keyHolder.getKey() == null ? null : keyHolder.getKey().longValue();
    }

    @Override
    public int updateBasicInfo(TeachingMaterial material) { // 更新教学资料基础信息
        return jdbcTemplate.update(
                UPDATE_BASIC_INFO_SQL,
                material.getTitle(),
                material.getMaterialType(),
                material.getDescription(),
                material.getMaterialUrl(),
                material.getPublishTime(),
                material.getStatus(),
                material.getId()
        );
    }

    @Override
    public int updateStatus(Long id, Integer status) { // 更新教学资料状态
        return jdbcTemplate.update(UPDATE_STATUS_SQL, status, id);
    }

    private static class TeachingMaterialRowMapper implements RowMapper<TeachingMaterial> {
        @Override
        public TeachingMaterial mapRow(ResultSet rs, int rowNum) throws SQLException { // 把查询结果映射成 TeachingMaterial 对象
            TeachingMaterial material = new TeachingMaterial();
            material.setId(rs.getLong("id"));
            material.setCourseId(rs.getLong("course_id"));
            material.setTitle(rs.getString("title"));
            material.setMaterialType(rs.getString("material_type"));
            material.setDescription(rs.getString("description"));
            material.setMaterialUrl(rs.getString("material_url"));
            material.setPublishTime(String.valueOf(rs.getTimestamp("publish_time")));
            material.setStatus(rs.getInt("status"));
            return material;
        }
    }
}
