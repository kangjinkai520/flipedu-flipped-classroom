CREATE TABLE IF NOT EXISTS sys_user (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(128) NOT NULL,
    role VARCHAR(20) NOT NULL,
    name VARCHAR(50) NOT NULL,
    status TINYINT NOT NULL DEFAULT 1
);

INSERT INTO sys_user (username, password, role, name, status)
VALUES
    ('teacher01', '$2a$10$9p6zLih6Ox6tlD65LaVlH.OgOd2nRBlwgY3GyNSJ65p4.bHF3yjZa', 'TEACHER', '张老师', 1),
    ('student01', '$2a$10$uKjQtPbN9UE2qkNs0xg65uR2YMNW2hbU/hSrrshMpxHoJmp/m1NGK', 'STUDENT', '李明', 1),
    ('student02', '$2a$10$giXHBPXjWhGX4hpRfgTR7evDc/XxRVlnbiw.XTqaxji3Tsm4XjxDC', 'STUDENT', '王芳', 1)
ON DUPLICATE KEY UPDATE
    password = VALUES(password),
    role = VALUES(role),
    name = VALUES(name),
    status = VALUES(status);
