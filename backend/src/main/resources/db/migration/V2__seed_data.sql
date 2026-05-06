INSERT INTO sys_user (id, username, password_hash, role, display_name, status, must_change_password, last_login_at, created_at, updated_at)
VALUES
  (1, 't9001', '{noop}123456', 'TEACHER', '演示教师', 'ACTIVE', FALSE, NOW(), NOW(), NOW()),
  (2, '20260001', '{noop}123456', 'STUDENT', '演示学生', 'ACTIVE', FALSE, NOW(), NOW(), NOW());

INSERT INTO class_room (id, name, code, teacher_user_id, status, created_at, updated_at)
VALUES (1, '软件工程 2026-1 班', 'SE2026-1', 1, 'ACTIVE', NOW(), NOW());

INSERT INTO class_member (id, class_id, student_user_id, joined_at, created_at, updated_at)
VALUES (1, 1, 2, NOW(), NOW(), NOW());

INSERT INTO course_material (id, title, category, description, file_path, file_name, uploader_user_id, visibility, created_at, updated_at)
VALUES
  (1, '课程简介', 'COURSE', '基础课程资料', 'materials/course-intro.txt', 'course-intro.txt', 1, 'ALL', NOW(), NOW()),
  (2, '实验安全规范', 'NOTICE', '实验前必读', 'materials/lab-safety.txt', 'lab-safety.txt', 1, 'ALL', NOW(), NOW());

INSERT INTO question_bank (id, code, type, stem, difficulty, default_score, options_json, answer_json, analysis_text, created_by, created_at, updated_at)
VALUES
  (1, 'Q-FOUND-001', 'SINGLE', '软件体系结构的核心目标是什么？', 'EASY', 5, '["A.降低复杂度","B.增加偶然性"]', '["A"]', '强调控制复杂度', 1, NOW(), NOW()),
  (2, 'Q-FOUND-002', 'JUDGE', '分层设计有助于职责隔离。', 'EASY', 3, '["true","false"]', '["true"]', '职责清晰更易维护', 1, NOW(), NOW()),
  (3, 'Q-FOUND-003', 'SHORT', '简述接口统一返回结构的意义。', 'MEDIUM', 10, NULL, '["统一错误处理与前端解析"]', '便于一致处理', 1, NOW(), NOW());

INSERT INTO homework (id, title, description, class_id, start_at, due_at, attachment_path, created_by, status, created_at, updated_at)
VALUES
  (-1, '体系结构分析作业', '围绕分层设计撰写结构分析说明。', 1, NOW(), TIMESTAMPADD(DAY, 7, NOW()), NULL, 1, 'PUBLISHED', NOW(), NOW()),
  (-2, '作业草稿演示', '用于教师演示草稿态作业的编辑与发布流程。', 1, NOW(), TIMESTAMPADD(DAY, 10, NOW()), 'homework/draft-guide.docx', 1, 'DRAFT', NOW(), NOW()),
  (-3, '待批改作业演示', '用于教师演示作业批改保存流程。', 1, NOW(), TIMESTAMPADD(DAY, 5, NOW()), NULL, 1, 'PUBLISHED', NOW(), NOW());

INSERT INTO homework_submission (id, homework_id, student_id, submit_status, answer_text, answer_file_path, plagiarism_rate, total_score, teacher_comment, created_at, updated_at)
VALUES
  (-1, -1, 2, 'GRADED', '分层设计能够隔离职责并降低重构风险。', NULL, 42.5, 92, '论述完整，能准确说明分层设计价值。', NOW(), NOW()),
  (-2, -3, 2, 'SUBMITTED', '模块边界清晰后，批改流程、测试范围与重构影响都更容易控制。', NULL, 15.0, NULL, '', NOW(), NOW());

INSERT INTO exam (id, title, description, class_id, start_at, end_at, duration_minutes, status, created_by, created_at, updated_at)
VALUES (-1, '体系结构阶段测验', '覆盖分层、职责隔离和统一返回结构。', 1, TIMESTAMPADD(HOUR, -1, NOW()), TIMESTAMPADD(DAY, 1, NOW()), 30, 'PUBLISHED', 1, NOW(), NOW()),
       (-2, '期末综合考试草稿', '用于演示教师端草稿态考试的编辑与发布流程。', 1, TIMESTAMPADD(DAY, 2, NOW()), TIMESTAMPADD(DAY, 3, NOW()), 90, 'DRAFT', 1, NOW(), NOW()),
       (-3, '待评分考试演示', '用于教师演示考试主观题批改保存流程。', 1, TIMESTAMPADD(HOUR, -2, NOW()), TIMESTAMPADD(DAY, 1, NOW()), 45, 'PUBLISHED', 1, NOW(), NOW());

INSERT INTO exam_question (id, exam_id, question_id, sort_order, question_score, created_at, updated_at)
VALUES (-1, -1, 1, 1, 5, NOW(), NOW()),
       (-2, -1, 2, 2, 3, NOW(), NOW()),
       (-3, -1, 3, 3, 10, NOW(), NOW()),
       (-4, -2, 1, 1, 5, NOW(), NOW()),
       (-5, -2, 3, 2, 10, NOW(), NOW()),
       (-6, -3, 1, 1, 5, NOW(), NOW()),
       (-7, -3, 2, 2, 3, NOW(), NOW()),
       (-8, -3, 3, 3, 10, NOW(), NOW());

INSERT INTO exam_submission (id, exam_id, student_id, started_at, submitted_at, deadline_at, auto_score, manual_score, total_score, status, created_at, updated_at)
VALUES (-1, -1, 2, TIMESTAMPADD(MINUTE, -25, NOW()), TIMESTAMPADD(MINUTE, -5, NOW()), TIMESTAMPADD(MINUTE, 5, NOW()), 8, 7, 15, 'GRADED', NOW(), NOW()),
       (-2, -3, 2, TIMESTAMPADD(MINUTE, -20, NOW()), TIMESTAMPADD(MINUTE, -3, NOW()), TIMESTAMPADD(MINUTE, 25, NOW()), 8, 0, 8, 'SUBMITTED', NOW(), NOW());

INSERT INTO exam_answer (id, exam_submission_id, question_id, answer_json, is_correct, score, teacher_comment, created_at, updated_at)
VALUES
  (-1, -1, 1, '["A"]', TRUE, 5, NULL, NOW(), NOW()),
  (-2, -1, 2, '["true"]', TRUE, 3, NULL, NOW(), NOW()),
  (-3, -1, 3, '["统一错误处理与前端解析"]', NULL, 7, '回答正确', NOW(), NOW()),
  (-4, -2, 1, '["A"]', TRUE, 5, NULL, NOW(), NOW()),
  (-5, -2, 2, '["true"]', TRUE, 3, NULL, NOW(), NOW()),
  (-6, -2, 3, '["统一错误处理有助于前端稳定解析异常。"]', NULL, NULL, NULL, NOW(), NOW());

INSERT INTO plagiarism_task (id, business_type, business_id, student_id, status, similarity_rate, top_match_summary, raw_result_json, created_at, updated_at)
VALUES
  (-1, 'HOMEWORK', -1, 2, 'COMPLETED', 42.5, '最高相似来源: 历史作业#-1 (42.50%)', '{"topMatchSummary":"最高相似来源: 历史作业#-1 (42.50%)","topMatchReference":"历史作业#-1","matchedSegments":[{"title":"历史作业#-1","summary":"分层设计能够隔离职责并降低重构风险。","similarityRate":42.5}]}', NOW(), NOW()),
  (-2, 'LAB', -1, 2, 'PENDING', 18.0, '实验总结与历史实验相似度偏低，待教师复核确认。', '{"topMatchSummary":"实验总结与历史实验相似度偏低，待教师复核确认。","topMatchReference":"实验报告#-1","matchedSegments":[{"title":"实验报告#-1","summary":"分层设计让模块边界清晰，便于按职责编写测试并降低重构风险。","similarityRate":18.0}]}', NOW(), NOW());

INSERT INTO score_record (id, business_type, business_id, student_id, class_id, score, graded_at, created_at, updated_at)
VALUES
  (-1, 'LAB', -1, 2, 1, 88.0, NOW(), NOW(), NOW()),
  (-2, 'HOMEWORK', -1, 2, 1, 92.0, NOW(), NOW(), NOW()),
  (-3, 'EXAM', -1, 2, 1, 15.0, NOW(), NOW(), NOW());
