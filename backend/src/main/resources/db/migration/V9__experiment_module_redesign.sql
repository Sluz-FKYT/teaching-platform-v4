CREATE TABLE IF NOT EXISTS t_experiment (
  experiment_id INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  experiment_no INT NOT NULL,
  experiment_name VARCHAR(30) NOT NULL,
  experiment_type INT DEFAULT NULL,
  instruction_type VARCHAR(10) DEFAULT NULL,
  experiment_requirement TEXT,
  experiment_content TEXT,
  state INT DEFAULT NULL
);

ALTER TABLE t_experiment ADD COLUMN IF NOT EXISTS status VARCHAR(16) NOT NULL DEFAULT 'DRAFT';
ALTER TABLE t_experiment ADD COLUMN IF NOT EXISTS start_at TIMESTAMP NULL;
ALTER TABLE t_experiment ADD COLUMN IF NOT EXISTS end_at TIMESTAMP NULL;
ALTER TABLE t_experiment ADD COLUMN IF NOT EXISTS material_id BIGINT NULL;
ALTER TABLE t_experiment ADD COLUMN IF NOT EXISTS class_id BIGINT NULL;
ALTER TABLE t_experiment ADD COLUMN IF NOT EXISTS created_by BIGINT NULL;
ALTER TABLE t_experiment ADD COLUMN IF NOT EXISTS score_visibility_mode VARCHAR(32) NOT NULL DEFAULT 'AFTER_TEACHER_CONFIRM';
ALTER TABLE t_experiment ADD COLUMN IF NOT EXISTS score_released BOOLEAN NOT NULL DEFAULT FALSE;
ALTER TABLE t_experiment ADD COLUMN IF NOT EXISTS summary_required BOOLEAN NOT NULL DEFAULT FALSE;
ALTER TABLE t_experiment ADD COLUMN IF NOT EXISTS created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP;
ALTER TABLE t_experiment ADD COLUMN IF NOT EXISTS updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP;

INSERT INTO sys_user (id, username, password_hash, role, display_name, status, must_change_password, last_login_at, created_at, updated_at)
SELECT seed.id,
       seed.username,
       seed.password_hash,
       seed.role,
       seed.display_name,
       seed.status,
       seed.must_change_password,
       seed.last_login_at,
       seed.created_at,
       seed.updated_at
FROM (
    SELECT 3 AS id, '20260002' AS username, '{noop}123456' AS password_hash, 'STUDENT' AS role,
           '联调学生' AS display_name, 'ACTIVE' AS status, FALSE AS must_change_password,
           CURRENT_TIMESTAMP AS last_login_at, CURRENT_TIMESTAMP AS created_at, CURRENT_TIMESTAMP AS updated_at
) seed
WHERE NOT EXISTS (
    SELECT 1 FROM sys_user su WHERE su.id = seed.id
);

INSERT INTO t_experiment (experiment_id, experiment_no, experiment_name, experiment_type, instruction_type, experiment_requirement, experiment_content, state, status, start_at, end_at, material_id, class_id, created_by, score_visibility_mode, score_released, summary_required, created_at, updated_at)
SELECT CAST(l.id AS INT),
       CAST(l.id AS INT),
       l.title,
       1,
       '1',
       l.description,
       COALESCE(l.description, ''),
       CASE l.status
           WHEN 'DRAFT' THEN 0
           WHEN 'PUBLISHED' THEN 1
           WHEN 'CLOSED' THEN 2
           ELSE 1
       END,
       l.status,
       l.start_at,
       l.end_at,
       l.material_id,
       l.class_id,
       l.created_by,
       l.score_visibility_mode,
       l.score_released,
       FALSE,
       l.created_at,
       l.updated_at
FROM lab l
WHERE l.id <= 2147483647
  AND NOT EXISTS (
    SELECT 1 FROM t_experiment te WHERE te.experiment_id = CAST(l.id AS INT)
);

CREATE TABLE IF NOT EXISTS t_experiment_item (
  id BIGINT PRIMARY KEY,
  experiment_id BIGINT NOT NULL,
  experiment_item_no INT NOT NULL,
  experiment_item_name VARCHAR(128) NOT NULL,
  experiment_item_type INT NOT NULL DEFAULT 5,
  experiment_item_content TEXT NOT NULL,
  experiment_item_answer TEXT NULL,
  experiment_item_score INT NOT NULL,
  state INT NOT NULL DEFAULT 1,
  question_type VARCHAR(32) NOT NULL,
  answer_config_json TEXT NULL,
  scoring_config_json TEXT NULL,
  question_id BIGINT NULL,
  question_snapshot_json TEXT NOT NULL DEFAULT '{}',
  editor_language VARCHAR(32) NOT NULL DEFAULT 'TEXT',
  allow_paste BOOLEAN NOT NULL DEFAULT TRUE,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT uk_t_experiment_item_no UNIQUE (experiment_id, experiment_item_no)
);

INSERT INTO t_experiment_item (id, experiment_id, experiment_item_no, experiment_item_name, experiment_item_type, experiment_item_content, experiment_item_answer, experiment_item_score, state, question_type, answer_config_json, scoring_config_json, question_id, question_snapshot_json, editor_language, allow_paste, created_at, updated_at)
SELECT ls.id,
       ls.lab_id,
       ls.step_no,
       ls.title,
       CASE ls.question_type
           WHEN 'SINGLE_CHOICE' THEN 1
           WHEN 'MULTIPLE_CHOICE' THEN 2
           WHEN 'FILL_BLANK' THEN 3
           WHEN 'TRUE_FALSE' THEN 4
           WHEN 'TEXT' THEN 5
           WHEN 'SHORT' THEN 5
           WHEN 'CODE' THEN 6
           ELSE 5
       END,
       ls.content,
       ls.answer_config_json,
       ls.step_score,
       1,
       ls.question_type,
       ls.answer_config_json,
       ls.scoring_config_json,
       NULL,
       CONCAT('{"legacyQuestionType":"', ls.question_type, '"}'),
       CASE WHEN ls.question_type = 'CODE' THEN 'JAVA' ELSE 'TEXT' END,
       ls.allow_paste,
       ls.created_at,
       ls.updated_at
FROM lab_step ls
WHERE NOT EXISTS (
    SELECT 1 FROM t_experiment_item tei WHERE tei.id = ls.id
);

CREATE TABLE IF NOT EXISTS experiment_submission (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  experiment_id BIGINT NOT NULL,
  student_id BIGINT NOT NULL,
  submit_status VARCHAR(16) NOT NULL,
  plagiarism_rate DOUBLE NULL,
  total_score DOUBLE NULL,
  teacher_comment TEXT NULL,
  summary_text TEXT NULL,
  summary_attachment_path VARCHAR(255) NULL,
  submitted_at TIMESTAMP NULL,
  graded_at TIMESTAMP NULL,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT uk_experiment_submission UNIQUE (experiment_id, student_id)
);

INSERT INTO experiment_submission (id, experiment_id, student_id, submit_status, plagiarism_rate, total_score, teacher_comment, summary_text, summary_attachment_path, submitted_at, graded_at, created_at, updated_at)
SELECT ls.id,
       ls.lab_id,
       ls.student_id,
       ls.submit_status,
       ls.plagiarism_rate,
       ls.total_score,
       ls.teacher_comment,
       NULL,
       NULL,
       ls.submitted_at,
       ls.graded_at,
       ls.created_at,
       ls.updated_at
FROM lab_submission ls
WHERE NOT EXISTS (
    SELECT 1 FROM experiment_submission es WHERE es.id = ls.id
);

CREATE TABLE IF NOT EXISTS experiment_answer (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  experiment_submission_id BIGINT NOT NULL,
  experiment_item_id BIGINT NOT NULL,
  question_id BIGINT NULL,
  question_snapshot_json TEXT NOT NULL DEFAULT '{}',
  answer_text TEXT NULL,
  answer_json TEXT NULL,
  answer_file_path VARCHAR(255) NULL,
  auto_score DOUBLE NULL,
  suggested_score DOUBLE NULL,
  score_source VARCHAR(16) NOT NULL DEFAULT 'TEACHER',
  auto_judge_detail TEXT NULL,
  score DOUBLE NULL,
  teacher_comment TEXT NULL,
  accepted_auto_score BOOLEAN NOT NULL DEFAULT FALSE,
  editor_language VARCHAR(32) NOT NULL DEFAULT 'TEXT',
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT uk_experiment_answer UNIQUE (experiment_submission_id, experiment_item_id)
);

INSERT INTO experiment_answer (id, experiment_submission_id, experiment_item_id, question_id, question_snapshot_json, answer_text, answer_json, answer_file_path, auto_score, suggested_score, score_source, auto_judge_detail, score, teacher_comment, accepted_auto_score, editor_language, created_at, updated_at)
SELECT lsa.id,
       lsa.lab_submission_id,
       lsa.lab_step_id,
       tei.question_id,
       tei.question_snapshot_json,
       lsa.answer_text,
       NULL,
       lsa.answer_file_path,
       lsa.auto_score,
       lsa.suggested_score,
       lsa.score_source,
       lsa.auto_judge_detail,
       lsa.score,
       lsa.teacher_comment,
       FALSE,
       tei.editor_language,
       lsa.created_at,
       lsa.updated_at
FROM lab_step_answer lsa
JOIN t_experiment_item tei ON tei.id = lsa.lab_step_id
WHERE NOT EXISTS (
    SELECT 1 FROM experiment_answer ea WHERE ea.id = lsa.id
);

CREATE TABLE IF NOT EXISTS experiment_blank_answer_override (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  experiment_id BIGINT NOT NULL,
  experiment_item_id BIGINT NOT NULL,
  accepted_answer TEXT NOT NULL,
  normalized_answer VARCHAR(255) NOT NULL,
  blank_answers_json TEXT NULL,
  created_by BIGINT NOT NULL,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT uk_experiment_blank_override UNIQUE (experiment_id, experiment_item_id, normalized_answer)
);

ALTER TABLE experiment_blank_answer_override ADD COLUMN IF NOT EXISTS blank_answers_json TEXT NULL;

INSERT INTO t_experiment (experiment_id, experiment_no, experiment_name, experiment_type, instruction_type, experiment_requirement, experiment_content, state, status, start_at, end_at, material_id, class_id, created_by, score_visibility_mode, score_released, summary_required, created_at, updated_at)
SELECT seed.id,
       seed.experiment_no,
       seed.experiment_name,
       seed.experiment_type,
       seed.instruction_type,
       seed.experiment_requirement,
       seed.experiment_content,
       seed.state,
       seed.status,
       seed.start_at,
       seed.end_at,
       seed.material_id,
       seed.class_id,
       seed.created_by,
       seed.score_visibility_mode,
       seed.score_released,
       seed.summary_required,
       seed.created_at,
       seed.updated_at
FROM (
    SELECT 1001 AS id, 1001 AS experiment_no, '分层架构分析实验' AS experiment_name, 1 AS experiment_type, '1' AS instruction_type,
           '完成教学平台分层架构识别、关键职责划分与实验总结。' AS experiment_requirement,
           '完成教学平台分层架构识别、关键职责划分与实验总结。' AS experiment_content,
           1 AS state, 'PUBLISHED' AS status, CURRENT_TIMESTAMP AS start_at, TIMESTAMPADD(DAY, 7, CURRENT_TIMESTAMP) AS end_at,
           2 AS material_id, 1 AS class_id, 1 AS created_by, 'AFTER_TEACHER_CONFIRM' AS score_visibility_mode,
           FALSE AS score_released, TRUE AS summary_required, CURRENT_TIMESTAMP AS created_at, CURRENT_TIMESTAMP AS updated_at
    UNION ALL
    SELECT 1002, 1002, '实验报告规范练习', 1, '1',
           '用于演示草稿态实验的编辑与发布流程。',
           '用于演示草稿态实验的编辑与发布流程。',
           0, 'DRAFT', CURRENT_TIMESTAMP, TIMESTAMPADD(DAY, 10, CURRENT_TIMESTAMP),
           2, 1, 1, 'AFTER_TEACHER_CONFIRM', FALSE, FALSE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
    UNION ALL
    SELECT 1003, 1003, '实验模块联调填空实验', 1, '1',
           '用于真实联调实验链路，支持教师查看填空分布与学生提交必填实验小结。',
           '用于真实联调实验链路，支持教师查看填空分布与学生提交必填实验小结。',
           1, 'PUBLISHED', CURRENT_TIMESTAMP, TIMESTAMPADD(DAY, 14, CURRENT_TIMESTAMP),
           2, 1, 1, 'AFTER_TEACHER_CONFIRM', FALSE, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
) seed
WHERE NOT EXISTS (
    SELECT 1 FROM t_experiment te WHERE te.experiment_id = seed.id
);

INSERT INTO t_experiment_item (id, experiment_id, experiment_item_no, experiment_item_name, experiment_item_type, experiment_item_content, experiment_item_answer, experiment_item_score, state, question_type, answer_config_json, scoring_config_json, question_id, question_snapshot_json, editor_language, allow_paste, created_at, updated_at)
SELECT seed.id,
       seed.experiment_id,
       seed.experiment_item_no,
       seed.experiment_item_name,
       seed.experiment_item_type,
       seed.experiment_item_content,
       seed.experiment_item_answer,
       seed.experiment_item_score,
       seed.state,
       seed.question_type,
       seed.answer_config_json,
       seed.scoring_config_json,
       seed.question_id,
       seed.question_snapshot_json,
       seed.editor_language,
       seed.allow_paste,
       seed.created_at,
       seed.updated_at
FROM (
    SELECT 2001 AS id, 1001 AS experiment_id, 1 AS experiment_item_no, '识别系统分层' AS experiment_item_name, 1 AS experiment_item_type,
           '请选择表示层对应职责。' AS experiment_item_content,
           '{"options":[{"key":"A","label":"页面交互"},{"key":"B","label":"事务管理"}],"correctAnswer":["A"],"gradingMode":"exact","editorLanguage":"TEXT"}' AS experiment_item_answer,
           30 AS experiment_item_score, 1 AS state, 'SINGLE_CHOICE' AS question_type,
           '{"options":[{"key":"A","label":"页面交互"},{"key":"B","label":"事务管理"}],"correctAnswer":["A"],"gradingMode":"exact","editorLanguage":"TEXT"}' AS answer_config_json,
           '{"correctAnswer":["A"],"gradingMode":"exact"}' AS scoring_config_json,
           1 AS question_id,
           '{"legacyQuestionType":"SINGLE_CHOICE","questionId":1,"stem":"请选择表示层对应职责。"}' AS question_snapshot_json,
           'TEXT' AS editor_language, TRUE AS allow_paste, CURRENT_TIMESTAMP AS created_at, CURRENT_TIMESTAMP AS updated_at
    UNION ALL
    SELECT 2002, 1001, 2, '分析职责边界', 5,
           '结合实验材料，描述实验管理模块与资料管理模块的职责边界。',
           '{"keywords":[{"term":"实验管理","weight":15},{"term":"资料管理","weight":15}],"minLength":20,"commentTemplate":"已覆盖主要职责点，请教师确认。","editorLanguage":"TEXT"}',
           30, 1, 'TEXT',
           '{"keywords":[{"term":"实验管理","weight":15},{"term":"资料管理","weight":15}],"minLength":20,"commentTemplate":"已覆盖主要职责点，请教师确认。","editorLanguage":"TEXT"}',
           '{"keywords":[{"term":"实验管理","weight":15},{"term":"资料管理","weight":15}],"minLength":20,"commentTemplate":"已覆盖主要职责点，请教师确认。"}',
           3,
           '{"legacyQuestionType":"TEXT","questionId":3,"stem":"结合实验材料，描述实验管理模块与资料管理模块的职责边界。"}',
           'TEXT', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
    UNION ALL
    SELECT 2003, 1001, 3, '提交实验总结', 5,
           '总结分层设计对后续系统重构和测试的帮助。',
           '{"summaryRequired":true,"editorLanguage":"TEXT"}',
           40, 1, 'TEXT',
           '{"summaryRequired":true,"editorLanguage":"TEXT"}',
           '{}',
           3,
           '{"legacyQuestionType":"TEXT","questionId":3,"stem":"总结分层设计对后续系统重构和测试的帮助。"}',
           'TEXT', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
    UNION ALL
    SELECT 2004, 1002, 1, '草稿实验步骤', 5,
           '这是教师编辑实验时使用的演示步骤。',
           '{"editorLanguage":"TEXT"}',
           20, 1, 'TEXT',
           '{"editorLanguage":"TEXT"}',
           '{}',
           NULL,
           '{"legacyQuestionType":"TEXT"}',
           'TEXT', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
    UNION ALL
    SELECT 2005, 1003, 1, '填写分层职责映射', 3,
           '请依次填写承接 HTTP 请求的层和封装业务规则的层。',
           '{"blanks":[{"answers":["Controller"],"score":10},{"answers":["Service"],"score":10}],"ignoreCase":true,"editorLanguage":"TEXT"}',
           20, 1, 'FILL_BLANK',
           '{"blanks":[{"answers":["Controller"],"score":10},{"answers":["Service"],"score":10}],"ignoreCase":true,"editorLanguage":"TEXT"}',
           '{"blanks":[{"answers":["Controller"],"score":10},{"answers":["Service"],"score":10}],"ignoreCase":true}',
           NULL,
           '{"legacyQuestionType":"FILL_BLANK","stem":"请依次填写承接 HTTP 请求的层和封装业务规则的层。","scoringConfig":{"blanks":[{"answers":["Controller"],"score":10},{"answers":["Service"],"score":10}],"ignoreCase":true}}',
           'TEXT', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
    UNION ALL
    SELECT 2006, 1003, 2, '说明总结约束', 5,
           '说明为什么本实验要求在提交前填写实验小结。',
           '{"keywords":[{"term":"实验小结","weight":10},{"term":"提交","weight":10}],"minLength":10,"editorLanguage":"TEXT"}',
           20, 1, 'TEXT',
           '{"keywords":[{"term":"实验小结","weight":10},{"term":"提交","weight":10}],"minLength":10,"editorLanguage":"TEXT"}',
           '{"keywords":[{"term":"实验小结","weight":10},{"term":"提交","weight":10}],"minLength":10}',
           NULL,
           '{"legacyQuestionType":"TEXT","stem":"说明为什么本实验要求在提交前填写实验小结。"}',
           'TEXT', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
) seed
WHERE NOT EXISTS (
    SELECT 1 FROM t_experiment_item tei WHERE tei.id = seed.id
);

INSERT INTO experiment_submission (id, experiment_id, student_id, submit_status, plagiarism_rate, total_score, teacher_comment, summary_text, summary_attachment_path, submitted_at, graded_at, created_at, updated_at)
SELECT seed.id,
       seed.experiment_id,
       seed.student_id,
       seed.submit_status,
       seed.plagiarism_rate,
       seed.total_score,
       seed.teacher_comment,
       seed.summary_text,
       seed.summary_attachment_path,
       seed.submitted_at,
       seed.graded_at,
       seed.created_at,
       seed.updated_at
FROM (
    SELECT 3001 AS id, 1001 AS experiment_id, 2 AS student_id, 'GRADED' AS submit_status, 18.0 AS plagiarism_rate,
           88.0 AS total_score, '实验总结完整，职责边界分析清晰。' AS teacher_comment,
           '分层设计让模块边界清晰，便于按职责编写测试并降低重构风险。' AS summary_text,
           NULL AS summary_attachment_path, CURRENT_TIMESTAMP AS submitted_at, CURRENT_TIMESTAMP AS graded_at,
           CURRENT_TIMESTAMP AS created_at, CURRENT_TIMESTAMP AS updated_at
    UNION ALL
    SELECT 3002, 1003, 3, 'SUBMITTED', 0.0,
           20.0, '用于教师查看填空答案分布。',
           '先观察填空答案分布，再决定是否追加接受答案。',
           NULL, CURRENT_TIMESTAMP, NULL,
           CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
) seed
WHERE NOT EXISTS (
    SELECT 1 FROM experiment_submission es WHERE es.id = seed.id
);

INSERT INTO experiment_answer (id, experiment_submission_id, experiment_item_id, question_id, question_snapshot_json, answer_text, answer_json, answer_file_path, auto_score, suggested_score, score_source, auto_judge_detail, score, teacher_comment, accepted_auto_score, editor_language, created_at, updated_at)
SELECT seed.id,
       seed.experiment_submission_id,
       seed.experiment_item_id,
       seed.question_id,
       seed.question_snapshot_json,
       seed.answer_text,
       seed.answer_json,
       seed.answer_file_path,
       seed.auto_score,
       seed.suggested_score,
       seed.score_source,
       seed.auto_judge_detail,
       seed.score,
       seed.teacher_comment,
       seed.accepted_auto_score,
       seed.editor_language,
       seed.created_at,
       seed.updated_at
FROM (
    SELECT 4001 AS id, 3001 AS experiment_submission_id, 2001 AS experiment_item_id, 1 AS question_id,
           '{"legacyQuestionType":"SINGLE_CHOICE","questionId":1,"stem":"请选择表示层对应职责。"}' AS question_snapshot_json,
           'A' AS answer_text, NULL AS answer_json, NULL AS answer_file_path, 30.0 AS auto_score, 30.0 AS suggested_score,
           'AUTO' AS score_source, '客观题自动判分通过。' AS auto_judge_detail, 30.0 AS score, '选择正确。' AS teacher_comment,
           FALSE AS accepted_auto_score, 'TEXT' AS editor_language, CURRENT_TIMESTAMP AS created_at, CURRENT_TIMESTAMP AS updated_at
    UNION ALL
    SELECT 4002, 3001, 2002, 3,
           '{"legacyQuestionType":"TEXT","questionId":3,"stem":"结合实验材料，描述实验管理模块与资料管理模块的职责边界。"}',
           '实验管理聚焦实验发布、步骤配置和报告评分，资料管理聚焦课件上传、下载与可见范围控制。', NULL, NULL, 25.0, 28.0,
           'TEACHER', '已覆盖实验管理、资料管理等关键词。', 28.0, '职责边界说明较完整。',
           FALSE, 'TEXT', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
    UNION ALL
    SELECT 4003, 3001, 2003, 3,
           '{"legacyQuestionType":"TEXT","questionId":3,"stem":"总结分层设计对后续系统重构和测试的帮助。"}',
           '分层设计让模块边界清晰，便于按职责编写测试并降低重构风险。', NULL, NULL, 28.0, 30.0,
           'TEACHER', '总结已覆盖重构与测试价值。', 30.0, '总结到位。',
           FALSE, 'TEXT', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
    UNION ALL
    SELECT 4004, 3002, 2005, NULL,
           '{"legacyQuestionType":"FILL_BLANK","stem":"请依次填写承接 HTTP 请求的层和封装业务规则的层。","scoringConfig":{"blanks":[{"answers":["Controller"],"score":10},{"answers":["Service"],"score":10}],"ignoreCase":true}}',
           'Controller Service', NULL, NULL, 20.0, 20.0,
           'AUTO', 'blank 1: matched; blank 2: matched', 20.0, '两个空都已命中标准答案。',
           FALSE, 'TEXT', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
    UNION ALL
    SELECT 4005, 3002, 2006, NULL,
           '{"legacyQuestionType":"TEXT","stem":"说明为什么本实验要求在提交前填写实验小结。"}',
           '实验小结可以帮助教师核对学生是否真正理解提交内容。', NULL, NULL, 16.0, 16.0,
           'TEACHER', '已覆盖实验小结与提交流程价值。', 16.0, '说明较完整。',
           FALSE, 'TEXT', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
) seed
WHERE NOT EXISTS (
    SELECT 1 FROM experiment_answer ea WHERE ea.id = seed.id
);

INSERT INTO experiment_blank_answer_override (id, experiment_id, experiment_item_id, accepted_answer, normalized_answer, blank_answers_json, created_by, created_at, updated_at)
SELECT seed.id,
       seed.experiment_id,
       seed.experiment_item_id,
       seed.accepted_answer,
       seed.normalized_answer,
       seed.blank_answers_json,
       seed.created_by,
       seed.created_at,
       seed.updated_at
FROM (
    SELECT 5001 AS id, 1001 AS experiment_id, 2002 AS experiment_item_id, '资料管理' AS accepted_answer, '资料管理' AS normalized_answer, 1 AS created_by,
            NULL AS blank_answers_json,
            CURRENT_TIMESTAMP AS created_at, CURRENT_TIMESTAMP AS updated_at
    UNION ALL
    SELECT 5002, 1003, 2005, 'Controller Service', 'controller service', 1,
            '["Controller","Service"]',
            CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
) seed
WHERE NOT EXISTS (
    SELECT 1 FROM experiment_blank_answer_override ebao WHERE ebao.id = seed.id
);
