CREATE TABLE lab_step_answer_log (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  lab_step_answer_id BIGINT NOT NULL,
  content TEXT NULL,
  fill_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT fk_lab_step_answer_log_answer
    FOREIGN KEY (lab_step_answer_id) REFERENCES experiment_answer (id)
);

CREATE INDEX idx_lab_answer_log_answer_fill_time
  ON lab_step_answer_log (lab_step_answer_id, fill_time);
