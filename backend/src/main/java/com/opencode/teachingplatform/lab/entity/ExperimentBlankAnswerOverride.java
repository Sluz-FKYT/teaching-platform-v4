package com.opencode.teachingplatform.lab.entity;

import com.opencode.teachingplatform.common.model.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "experiment_blank_answer_override")
public class ExperimentBlankAnswerOverride extends BaseEntity {

    @Column(name = "experiment_id", nullable = false)
    private Long experimentId;

    @Column(name = "experiment_item_id", nullable = false)
    private Long experimentItemId;

    @Column(name = "accepted_answer", nullable = false, columnDefinition = "TEXT")
    private String acceptedAnswer;

    @Column(name = "normalized_answer", nullable = false, length = 255)
    private String normalizedAnswer;

    @Column(name = "blank_answers_json", columnDefinition = "TEXT")
    private String blankAnswersJson;

    @Column(name = "created_by", nullable = false)
    private Long createdBy;

    public Long getExperimentId() { return experimentId; }
    public void setExperimentId(Long experimentId) { this.experimentId = experimentId; }
    public Long getExperimentItemId() { return experimentItemId; }
    public void setExperimentItemId(Long experimentItemId) { this.experimentItemId = experimentItemId; }
    public String getAcceptedAnswer() { return acceptedAnswer; }
    public void setAcceptedAnswer(String acceptedAnswer) { this.acceptedAnswer = acceptedAnswer; }
    public String getNormalizedAnswer() { return normalizedAnswer; }
    public void setNormalizedAnswer(String normalizedAnswer) { this.normalizedAnswer = normalizedAnswer; }
    public String getBlankAnswersJson() { return blankAnswersJson; }
    public void setBlankAnswersJson(String blankAnswersJson) { this.blankAnswersJson = blankAnswersJson; }
    public Long getCreatedBy() { return createdBy; }
    public void setCreatedBy(Long createdBy) { this.createdBy = createdBy; }
}
