CREATE TABLE IF NOT EXISTS sys_user (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(128) NOT NULL,
    name VARCHAR(50) NOT NULL,
    role VARCHAR(20) NOT NULL,
    student_no VARCHAR(50) NULL,
    teacher_no VARCHAR(50) NULL,
    phone VARCHAR(20) NULL,
    avatar_url VARCHAR(255) NULL,
    status TINYINT NOT NULL DEFAULT 1,
    INDEX idx_sys_user_role (role),
    INDEX idx_sys_user_status (status)
);

CREATE TABLE IF NOT EXISTS course (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    teacher_id BIGINT NOT NULL,
    name VARCHAR(128) NOT NULL,
    code VARCHAR(64) NOT NULL UNIQUE,
    description TEXT NULL,
    cover_url VARCHAR(255) NULL,
    term VARCHAR(64) NULL,
    status TINYINT NOT NULL DEFAULT 1,
    CONSTRAINT fk_course_teacher FOREIGN KEY (teacher_id) REFERENCES sys_user(id),
    INDEX idx_course_teacher_id (teacher_id),
    INDEX idx_course_status (status)
);

CREATE TABLE IF NOT EXISTS course_member (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    course_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    role VARCHAR(32) NOT NULL,
    CONSTRAINT fk_course_member_course FOREIGN KEY (course_id) REFERENCES course(id),
    CONSTRAINT fk_course_member_user FOREIGN KEY (user_id) REFERENCES sys_user(id),
    CONSTRAINT uk_course_member UNIQUE (course_id, user_id),
    INDEX idx_course_member_course_id (course_id),
    INDEX idx_course_member_user_id (user_id)
);

CREATE TABLE IF NOT EXISTS course_feedback (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    course_id BIGINT NOT NULL,
    student_id BIGINT NOT NULL,
    lesson_title VARCHAR(128) NOT NULL,
    rating INT NOT NULL,
    mastery_level VARCHAR(32) NOT NULL,
    content TEXT NULL,
    suggestion TEXT NULL,
    created_at TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_course_feedback_course FOREIGN KEY (course_id) REFERENCES course(id),
    CONSTRAINT fk_course_feedback_student FOREIGN KEY (student_id) REFERENCES sys_user(id),
    INDEX idx_course_feedback_course_id (course_id),
    INDEX idx_course_feedback_student_id (student_id),
    INDEX idx_course_feedback_created_at (created_at)
);

CREATE TABLE IF NOT EXISTS teaching_material (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    course_id BIGINT NOT NULL,
    title VARCHAR(128) NOT NULL,
    material_type VARCHAR(32) NOT NULL,
    description TEXT NULL,
    material_url VARCHAR(255) NOT NULL,
    publish_time TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP,
    status TINYINT NOT NULL DEFAULT 1,
    CONSTRAINT fk_teaching_material_course FOREIGN KEY (course_id) REFERENCES course(id),
    INDEX idx_teaching_material_course_id (course_id),
    INDEX idx_teaching_material_status (status)
);

CREATE TABLE IF NOT EXISTS preview_quiz (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    course_id BIGINT NOT NULL,
    title VARCHAR(128) NOT NULL,
    description TEXT NULL,
    total_score INT NOT NULL DEFAULT 100,
    status TINYINT NOT NULL DEFAULT 1,
    publish_time TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_preview_quiz_course FOREIGN KEY (course_id) REFERENCES course(id),
    INDEX idx_preview_quiz_course_id (course_id),
    INDEX idx_preview_quiz_status (status)
);

CREATE TABLE IF NOT EXISTS quiz_question (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    quiz_id BIGINT NOT NULL,
    type VARCHAR(32) NOT NULL,
    title TEXT NOT NULL,
    option_a VARCHAR(255) NULL,
    option_b VARCHAR(255) NULL,
    option_c VARCHAR(255) NULL,
    option_d VARCHAR(255) NULL,
    correct_answer VARCHAR(32) NULL,
    score INT NOT NULL DEFAULT 10,
    sort_order INT NOT NULL DEFAULT 0,
    CONSTRAINT fk_quiz_question_quiz FOREIGN KEY (quiz_id) REFERENCES preview_quiz(id),
    INDEX idx_quiz_question_quiz_id (quiz_id),
    INDEX idx_quiz_question_sort_order (sort_order)
);

CREATE TABLE IF NOT EXISTS quiz_record (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    quiz_id BIGINT NOT NULL,
    student_id BIGINT NOT NULL,
    score INT NOT NULL DEFAULT 0,
    status VARCHAR(32) NOT NULL DEFAULT 'SUBMITTED',
    answers_json TEXT NULL,
    submitted_at TIMESTAMP NULL DEFAULT NULL,
    CONSTRAINT fk_quiz_record_quiz FOREIGN KEY (quiz_id) REFERENCES preview_quiz(id),
    CONSTRAINT fk_quiz_record_student FOREIGN KEY (student_id) REFERENCES sys_user(id),
    CONSTRAINT uk_quiz_record UNIQUE (quiz_id, student_id)
);

CREATE TABLE IF NOT EXISTS notice (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    course_id BIGINT NOT NULL,
    title VARCHAR(128) NOT NULL,
    content TEXT NOT NULL,
    publish_time TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP,
    status TINYINT NOT NULL DEFAULT 1,
    CONSTRAINT fk_notice_course FOREIGN KEY (course_id) REFERENCES course(id),
    INDEX idx_notice_course_id (course_id),
    INDEX idx_notice_status (status)
);

CREATE TABLE IF NOT EXISTS sign_task (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    course_id BIGINT NOT NULL,
    title VARCHAR(128) NOT NULL,
    sign_code VARCHAR(32) NOT NULL,
    starts_at TIMESTAMP NULL DEFAULT NULL,
    ends_at TIMESTAMP NULL DEFAULT NULL,
    location_name VARCHAR(128) NULL,
    target_latitude DECIMAL(10, 6) NULL,
    target_longitude DECIMAL(10, 6) NULL,
    allowed_radius_meters INT NULL,
    status VARCHAR(32) NOT NULL DEFAULT 'ONGOING',
    CONSTRAINT fk_sign_task_course FOREIGN KEY (course_id) REFERENCES course(id),
    INDEX idx_sign_task_course_id (course_id),
    INDEX idx_sign_task_status (status)
);

CREATE TABLE IF NOT EXISTS sign_record (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    sign_task_id BIGINT NOT NULL,
    student_id BIGINT NOT NULL,
    signed_at TIMESTAMP NULL DEFAULT NULL,
    latitude DECIMAL(10, 6) NULL,
    longitude DECIMAL(10, 6) NULL,
    distance_meters DECIMAL(10, 2) NULL,
    sign_type VARCHAR(32) NOT NULL DEFAULT 'NORMAL',
    status VARCHAR(32) NOT NULL DEFAULT 'SIGNED',
    exception_reason TEXT NULL,
    review_comment VARCHAR(255) NULL,
    CONSTRAINT fk_sign_record_task FOREIGN KEY (sign_task_id) REFERENCES sign_task(id),
    CONSTRAINT fk_sign_record_student FOREIGN KEY (student_id) REFERENCES sys_user(id),
    CONSTRAINT uk_sign_record UNIQUE (sign_task_id, student_id)
);

CREATE TABLE IF NOT EXISTS assignment (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    course_id BIGINT NOT NULL,
    title VARCHAR(128) NOT NULL,
    description TEXT NOT NULL,
    deadline TIMESTAMP NULL DEFAULT NULL,
    published INT NOT NULL DEFAULT 1,
    CONSTRAINT fk_assignment_course FOREIGN KEY (course_id) REFERENCES course(id),
    INDEX idx_assignment_course_id (course_id),
    INDEX idx_assignment_published (published)
);

CREATE TABLE IF NOT EXISTS assignment_submit (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    assignment_id BIGINT NOT NULL,
    student_id BIGINT NOT NULL,
    content TEXT NOT NULL,
    attachment_url VARCHAR(255) NULL,
    score INT NULL,
    feedback TEXT NULL,
    submitted_at TIMESTAMP NULL DEFAULT NULL,
    CONSTRAINT fk_assignment_submit_assignment FOREIGN KEY (assignment_id) REFERENCES assignment(id),
    CONSTRAINT fk_assignment_submit_student FOREIGN KEY (student_id) REFERENCES sys_user(id),
    INDEX idx_assignment_submit_ids (assignment_id, student_id)
);

CREATE TABLE IF NOT EXISTS score_record (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    course_id BIGINT NOT NULL,
    student_id BIGINT NOT NULL,
    item_type VARCHAR(32) NOT NULL,
    item_name VARCHAR(128) NOT NULL,
    score INT NOT NULL,
    remark VARCHAR(255) NULL,
    CONSTRAINT fk_score_record_course FOREIGN KEY (course_id) REFERENCES course(id),
    CONSTRAINT fk_score_record_student FOREIGN KEY (student_id) REFERENCES sys_user(id),
    INDEX idx_score_record_course_student (course_id, student_id)
);

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
    CONSTRAINT fk_review_request_requester FOREIGN KEY (requester_id) REFERENCES sys_user(id),
    INDEX idx_review_request_status (status),
    INDEX idx_review_request_target (target_type, target_id)
);
