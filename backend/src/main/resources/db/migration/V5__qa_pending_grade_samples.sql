INSERT INTO homework (id, title, description, class_id, start_at, due_at, attachment_path, created_by, status, created_at, updated_at)
SELECT -3, '待批改作业演示', '用于教师演示作业批改保存流程。', 1, NOW(), TIMESTAMPADD(DAY, 5, NOW()), NULL, 1, 'PUBLISHED', NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM homework WHERE id = -3);

INSERT INTO homework_submission (id, homework_id, student_id, submit_status, answer_text, answer_file_path, plagiarism_rate, total_score, teacher_comment, created_at, updated_at)
SELECT -2, -3, 2, 'SUBMITTED', '模块边界清晰后，批改流程、测试范围与重构影响都更容易控制。', NULL, 15.0, NULL, '', NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM homework_submission WHERE id = -2);

INSERT INTO exam (id, title, description, class_id, start_at, end_at, duration_minutes, status, created_by, created_at, updated_at)
SELECT -3, '待评分考试演示', '用于教师演示考试主观题批改保存流程。', 1, TIMESTAMPADD(HOUR, -2, NOW()), TIMESTAMPADD(DAY, 1, NOW()), 45, 'PUBLISHED', 1, NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM exam WHERE id = -3);

INSERT INTO exam_question (id, exam_id, question_id, sort_order, question_score, created_at, updated_at)
SELECT -6, -3, 1, 1, 5, NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM exam_question WHERE id = -6);

INSERT INTO exam_question (id, exam_id, question_id, sort_order, question_score, created_at, updated_at)
SELECT -7, -3, 2, 2, 3, NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM exam_question WHERE id = -7);

INSERT INTO exam_question (id, exam_id, question_id, sort_order, question_score, created_at, updated_at)
SELECT -8, -3, 3, 3, 10, NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM exam_question WHERE id = -8);

INSERT INTO exam_submission (id, exam_id, student_id, started_at, submitted_at, deadline_at, auto_score, manual_score, total_score, status, created_at, updated_at)
SELECT -2, -3, 2, TIMESTAMPADD(MINUTE, -20, NOW()), TIMESTAMPADD(MINUTE, -3, NOW()), TIMESTAMPADD(MINUTE, 25, NOW()), 8, 0, 8, 'SUBMITTED', NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM exam_submission WHERE id = -2);

INSERT INTO exam_answer (id, exam_submission_id, question_id, answer_json, is_correct, score, teacher_comment, created_at, updated_at)
SELECT -4, -2, 1, '["A"]', TRUE, 5, NULL, NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM exam_answer WHERE id = -4);

INSERT INTO exam_answer (id, exam_submission_id, question_id, answer_json, is_correct, score, teacher_comment, created_at, updated_at)
SELECT -5, -2, 2, '["true"]', TRUE, 3, NULL, NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM exam_answer WHERE id = -5);

INSERT INTO exam_answer (id, exam_submission_id, question_id, answer_json, is_correct, score, teacher_comment, created_at, updated_at)
SELECT -6, -2, 3, '["统一错误处理有助于前端稳定解析异常。"]', NULL, NULL, NULL, NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM exam_answer WHERE id = -6);
