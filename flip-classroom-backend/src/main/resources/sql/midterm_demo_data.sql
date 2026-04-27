INSERT INTO sys_user (id, username, password, name, role, student_no, teacher_no, phone, avatar_url, status)
VALUES
    (1, 'teacher01', '$2a$10$9p6zLih6Ox6tlD65LaVlH.OgOd2nRBlwgY3GyNSJ65p4.bHF3yjZa', '张老师', 'TEACHER', NULL, 'T2026001', '13800000001', NULL, 1),
    (2, 'student01', '$2a$10$uKjQtPbN9UE2qkNs0xg65uR2YMNW2hbU/hSrrshMpxHoJmp/m1NGK', '李明', 'STUDENT', 'S2026001', NULL, '13800000002', NULL, 1),
    (3, 'student02', '$2a$10$giXHBPXjWhGX4hpRfgTR7evDc/XxRVlnbiw.XTqaxji3Tsm4XjxDC', '王芳', 'STUDENT', 'S2026002', NULL, '13800000003', NULL, 1),
    (4, 'student03', '$2a$10$9p6zLih6Ox6tlD65LaVlH.OgOd2nRBlwgY3GyNSJ65p4.bHF3yjZa', '赵新', 'STUDENT', 'S2026003', NULL, '13900000003', NULL, 1),
    (5, 'admin01', '$2a$10$9p6zLih6Ox6tlD65LaVlH.OgOd2nRBlwgY3GyNSJ65p4.bHF3yjZa', '系统管理员', 'ADMIN', NULL, NULL, '13800000005', NULL, 1)
ON DUPLICATE KEY UPDATE
    username = VALUES(username),
    password = VALUES(password),
    name = VALUES(name),
    role = VALUES(role),
    student_no = VALUES(student_no),
    teacher_no = VALUES(teacher_no),
    phone = VALUES(phone),
    avatar_url = VALUES(avatar_url),
    status = VALUES(status);

INSERT INTO course (id, teacher_id, name, code, description, cover_url, term, status)
VALUES
    (1, 1, 'Java程序设计', 'JAVA2026', '围绕翻转课堂开展课前预习、课中测验与课后反馈的示范课程。', NULL, '2025-2026-2', 1)
ON DUPLICATE KEY UPDATE
    teacher_id = VALUES(teacher_id),
    name = VALUES(name),
    code = VALUES(code),
    description = VALUES(description),
    cover_url = VALUES(cover_url),
    term = VALUES(term),
    status = VALUES(status);

INSERT INTO course_member (id, course_id, user_id, role)
VALUES
    (1, 1, 2, 'STUDENT'),
    (2, 1, 3, 'STUDENT')
ON DUPLICATE KEY UPDATE
    course_id = VALUES(course_id),
    user_id = VALUES(user_id),
    role = VALUES(role);

INSERT INTO teaching_material (id, course_id, title, material_type, description, material_url, publish_time, status)
VALUES
    (1, 1, '第一章导学 PPT', 'ppt', '用于课前预习的章节导学资料。', 'https://example.com/materials/java-ch1-ppt', '2026-04-05 09:00:00', 1),
    (2, 1, '预习视频链接', 'video', '配套视频讲解链接，帮助学生完成课前预习。', 'https://example.com/materials/java-ch1-video', '2026-04-05 09:10:00', 1)
ON DUPLICATE KEY UPDATE
    course_id = VALUES(course_id),
    title = VALUES(title),
    material_type = VALUES(material_type),
    description = VALUES(description),
    material_url = VALUES(material_url),
    publish_time = VALUES(publish_time),
    status = VALUES(status);

INSERT INTO preview_quiz (id, course_id, title, description, total_score, status, publish_time)
VALUES
    (1, 1, '第一章课堂测验', '用于课中检测学生对第一章核心知识点的掌握情况。', 100, 1, '2026-04-05 10:20:00')
ON DUPLICATE KEY UPDATE
    course_id = VALUES(course_id),
    title = VALUES(title),
    description = VALUES(description),
    total_score = VALUES(total_score),
    status = VALUES(status),
    publish_time = VALUES(publish_time);

INSERT INTO quiz_question (id, quiz_id, type, title, option_a, option_b, option_c, option_d, correct_answer, score, sort_order)
VALUES
    (1, 1, 'single', 'Java 是哪种语言平台特性最突出的编程语言？', '面向过程', '跨平台', '脚本解释', '函数式', 'B', 50, 1),
    (2, 1, 'single', 'JDK 的主要作用是什么？', '只运行程序', '只编辑文档', '提供 Java 开发与运行环境', '只管理数据库', 'C', 50, 2)
ON DUPLICATE KEY UPDATE
    quiz_id = VALUES(quiz_id),
    type = VALUES(type),
    title = VALUES(title),
    option_a = VALUES(option_a),
    option_b = VALUES(option_b),
    option_c = VALUES(option_c),
    option_d = VALUES(option_d),
    correct_answer = VALUES(correct_answer),
    score = VALUES(score),
    sort_order = VALUES(sort_order);

INSERT INTO quiz_record (id, quiz_id, student_id, score, status, answers_json, submitted_at)
VALUES
    (1, 1, 2, 100, 'SUBMITTED', '{\"1\":\"B\",\"2\":\"C\"}', '2026-04-05 10:28:00')
ON DUPLICATE KEY UPDATE
    quiz_id = VALUES(quiz_id),
    student_id = VALUES(student_id),
    score = VALUES(score),
    status = VALUES(status),
    answers_json = VALUES(answers_json),
    submitted_at = VALUES(submitted_at);

INSERT INTO notice (id, course_id, title, content, publish_time, status)
VALUES
    (1, 1, '第一周课堂通知', '请同学们提前完成课前资料学习，课堂中将进行签到和随堂测验。', '2026-04-05 09:30:00', 1)
ON DUPLICATE KEY UPDATE
    course_id = VALUES(course_id),
    title = VALUES(title),
    content = VALUES(content),
    publish_time = VALUES(publish_time),
    status = VALUES(status);

INSERT INTO sign_task (id, course_id, title, sign_code, starts_at, ends_at, status)
VALUES
    (1, 1, '第一周课堂签到', 'JAVA01', '2026-04-05 10:00:00', '2026-04-05 10:15:00', 'ONGOING')
ON DUPLICATE KEY UPDATE
    course_id = VALUES(course_id),
    title = VALUES(title),
    sign_code = VALUES(sign_code),
    starts_at = VALUES(starts_at),
    ends_at = VALUES(ends_at),
    status = VALUES(status);

INSERT INTO sign_record (id, sign_task_id, student_id, signed_at, status)
VALUES
    (1, 1, 2, '2026-04-05 10:05:00', 'SIGNED')
ON DUPLICATE KEY UPDATE
    sign_task_id = VALUES(sign_task_id),
    student_id = VALUES(student_id),
    signed_at = VALUES(signed_at),
    status = VALUES(status);

INSERT INTO assignment (id, course_id, title, description, deadline, published)
VALUES
    (1, 1, '第一章课后作业', '围绕第一章知识点完成基础练习并提交总结。', '2026-04-12 23:59:59', 1)
ON DUPLICATE KEY UPDATE
    course_id = VALUES(course_id),
    title = VALUES(title),
    description = VALUES(description),
    deadline = VALUES(deadline),
    published = VALUES(published);

INSERT INTO assignment_submit (id, assignment_id, student_id, content, attachment_url, score, feedback, submitted_at)
VALUES
    (1, 1, 2, '已完成课后练习并提交总结。', NULL, 88, '完成较好，注意代码规范。', '2026-04-11 20:30:00')
ON DUPLICATE KEY UPDATE
    assignment_id = VALUES(assignment_id),
    student_id = VALUES(student_id),
    content = VALUES(content),
    attachment_url = VALUES(attachment_url),
    score = VALUES(score),
    feedback = VALUES(feedback),
    submitted_at = VALUES(submitted_at);

INSERT INTO score_record (id, course_id, student_id, item_type, item_name, score, remark)
VALUES
    (1, 1, 2, 'quiz', '第一章课堂测验', 100, '课堂测验成绩'),
    (2, 1, 2, 'assignment', '第一章课后作业', 88, '课后作业成绩')
ON DUPLICATE KEY UPDATE
    course_id = VALUES(course_id),
    student_id = VALUES(student_id),
    item_type = VALUES(item_type),
    item_name = VALUES(item_name),
    score = VALUES(score),
    remark = VALUES(remark);

INSERT INTO course_feedback (id, course_id, student_id, lesson_title, rating, mastery_level, content, suggestion, created_at)
VALUES
    (1, 1, 2, '第一章 面向对象基础', 4, 'BASIC', '本节课整体能跟上，但继承和多态的关系还需要更多例子。', '下节课希望再讲一下接口和抽象类的区别。', '2026-04-17 15:30:00'),
    (2, 1, 3, '第一章 面向对象基础', 5, 'MASTERED', '课堂测验能帮助我确认预习效果，节奏比较清楚。', '希望保留课前资料和课堂测验结合的方式。', '2026-04-17 15:35:00')
ON DUPLICATE KEY UPDATE
    course_id = VALUES(course_id),
    student_id = VALUES(student_id),
    lesson_title = VALUES(lesson_title),
    rating = VALUES(rating),
    mastery_level = VALUES(mastery_level),
    content = VALUES(content),
    suggestion = VALUES(suggestion),
    created_at = VALUES(created_at);
