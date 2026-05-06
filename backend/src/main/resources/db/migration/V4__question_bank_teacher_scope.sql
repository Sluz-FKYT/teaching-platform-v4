UPDATE question_bank
SET created_by = 1
WHERE created_by IS NULL;

ALTER TABLE question_bank ALTER COLUMN created_by BIGINT NOT NULL;
