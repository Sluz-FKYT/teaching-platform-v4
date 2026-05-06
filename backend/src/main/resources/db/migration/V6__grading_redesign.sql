ALTER TABLE question_bank ADD COLUMN scoring_config_json TEXT NULL;

ALTER TABLE homework ADD COLUMN score_visibility_mode VARCHAR(32) NOT NULL DEFAULT 'AFTER_TEACHER_CONFIRM';

ALTER TABLE homework_question ALTER COLUMN question_id BIGINT NULL;
ALTER TABLE homework_question ADD COLUMN question_snapshot_json TEXT NOT NULL DEFAULT '{}';

ALTER TABLE homework_answer ALTER COLUMN question_id BIGINT NULL;
ALTER TABLE homework_answer ADD COLUMN homework_question_id BIGINT NULL;
ALTER TABLE homework_answer ADD COLUMN answer_text TEXT NULL;
ALTER TABLE homework_answer ADD COLUMN auto_score DOUBLE NULL;
ALTER TABLE homework_answer ADD COLUMN suggested_score DOUBLE NULL;
ALTER TABLE homework_answer ADD COLUMN score_source VARCHAR(32) NOT NULL DEFAULT 'TEACHER';
ALTER TABLE homework_answer ADD COLUMN judge_detail TEXT NULL;
ALTER TABLE homework_answer ADD COLUMN accepted_auto_score BOOLEAN NOT NULL DEFAULT FALSE;

ALTER TABLE homework_submission ADD COLUMN graded_at TIMESTAMP NULL;

ALTER TABLE exam_question ADD COLUMN question_snapshot_json TEXT NOT NULL DEFAULT '{}';

ALTER TABLE exam_answer ADD COLUMN auto_score DOUBLE NULL;
ALTER TABLE exam_answer ADD COLUMN suggested_score DOUBLE NULL;
ALTER TABLE exam_answer ADD COLUMN score_source VARCHAR(32) NOT NULL DEFAULT 'TEACHER';
ALTER TABLE exam_answer ADD COLUMN judge_detail TEXT NULL;
ALTER TABLE exam_answer ADD COLUMN accepted_auto_score BOOLEAN NOT NULL DEFAULT FALSE;

ALTER TABLE lab_step ADD COLUMN scoring_config_json TEXT NULL;

CREATE TABLE question_attachment (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  owner_type VARCHAR(32) NOT NULL,
  owner_id BIGINT NOT NULL,
  file_name VARCHAR(255) NOT NULL,
  file_path VARCHAR(512) NOT NULL,
  media_type VARCHAR(128) NULL,
  sort_order INT NOT NULL,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);
