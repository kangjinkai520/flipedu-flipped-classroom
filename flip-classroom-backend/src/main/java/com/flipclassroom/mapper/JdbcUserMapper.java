package com.flipclassroom.mapper;

import com.flipclassroom.entity.SysUser;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;

@Repository
public class JdbcUserMapper implements UserMapper {

    private static final String FIND_BY_USERNAME_SQL = """
            SELECT id, username, password, name, role, student_no, teacher_no, phone, status
            FROM sys_user
            WHERE username = ?
            LIMIT 1
            """;
    private static final String FIND_BY_ID_SQL = """
            SELECT id, username, password, name, role, student_no, teacher_no, phone, status
            FROM sys_user
            WHERE id = ?
            LIMIT 1
            """;
    private static final String FIND_ALL_SQL = """
            SELECT id, username, password, name, role, student_no, teacher_no, phone, status
            FROM sys_user
            ORDER BY id ASC
            """;
    private static final String FIND_ALL_BY_ROLE_SQL = """
            SELECT id, username, password, name, role, student_no, teacher_no, phone, status
            FROM sys_user
            WHERE UPPER(role) = UPPER(?)
            ORDER BY id ASC
            """;
    private static final String INSERT_SQL = """
            INSERT INTO sys_user (username, password, name, role, student_no, teacher_no, phone, status)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?)
            """;
    private static final String UPDATE_STATUS_SQL = """
            UPDATE sys_user
            SET status = ?
            WHERE id = ?
            """;
    private static final String UPDATE_BASIC_INFO_SQL = """
            UPDATE sys_user
            SET username = ?, name = ?, role = ?, student_no = ?, teacher_no = ?, phone = ?
            WHERE id = ?
            """;

    private final JdbcTemplate jdbcTemplate;

    public JdbcUserMapper(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public SysUser findByUsername(String username) { // 执行按用户名查询
        return jdbcTemplate.query(FIND_BY_USERNAME_SQL, new SysUserRowMapper(), username)
                .stream()
                .findFirst()
                .orElse(null);
    }

    @Override
    public SysUser findById(Long id) { // 执行按 id 查询
        return jdbcTemplate.query(FIND_BY_ID_SQL, new SysUserRowMapper(), id)
                .stream()
                .findFirst()
                .orElse(null);
    }

    @Override
    public List<SysUser> findAll(String role) { // 查询用户列表，role 为空时返回全部
        if (role == null || role.trim().isEmpty()) {
            return jdbcTemplate.query(FIND_ALL_SQL, new SysUserRowMapper());
        }
        return jdbcTemplate.query(FIND_ALL_BY_ROLE_SQL, new SysUserRowMapper(), role.trim());
    }

    @Override
    public Long insert(SysUser user) { // 插入用户并返回新记录 id
        // 使用主键回填，这样 Service 插入后可以立刻拿到新用户 id。
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            var statement = connection.prepareStatement(INSERT_SQL, Statement.RETURN_GENERATED_KEYS);
            statement.setString(1, user.getUsername());
            statement.setString(2, user.getPassword());
            statement.setString(3, user.getRealName());
            statement.setString(4, user.getRole());
            statement.setString(5, user.getStudentNo());
            statement.setString(6, user.getTeacherNo());
            statement.setString(7, user.getPhone());
            statement.setInt(8, user.getStatus());
            return statement;
        }, keyHolder);
        return keyHolder.getKey() == null ? null : keyHolder.getKey().longValue();
    }

    @Override
    public int updateStatus(Long id, Integer status) { // 更新用户启用/停用状态
        return jdbcTemplate.update(UPDATE_STATUS_SQL, status, id);
    }

    @Override
    public int updateBasicInfo(SysUser user) { // 更新用户基础资料
        return jdbcTemplate.update(
                UPDATE_BASIC_INFO_SQL,
                user.getUsername(),
                user.getRealName(),
                user.getRole(),
                user.getStudentNo(),
                user.getTeacherNo(),
                user.getPhone(),
                user.getId()
        );
    }

    private static class SysUserRowMapper implements RowMapper<SysUser> {
        @Override
        public SysUser mapRow(ResultSet rs, int rowNum) throws SQLException { // 把查询结果映射成 SysUser 对象
            // 把数据库字段映射成 SysUser，供上层 Service 和 Controller 使用。
            SysUser user = new SysUser();
            user.setId(rs.getLong("id"));
            user.setUsername(rs.getString("username"));
            user.setPassword(rs.getString("password"));
            user.setRealName(rs.getString("name"));
            user.setRole(rs.getString("role"));
            user.setStudentNo(rs.getString("student_no"));
            user.setTeacherNo(rs.getString("teacher_no"));
            user.setPhone(rs.getString("phone"));
            user.setStatus(rs.getInt("status"));
            return user;
        }
    }
}
