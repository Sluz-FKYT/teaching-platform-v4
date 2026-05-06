package com.opencode.teachingplatform.lab.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.OffsetDateTime;
import java.util.concurrent.ThreadLocalRandom;

@Entity
@Table(name = "t_experiment_item")
@EntityListeners(AuditingEntityListener.class)
public class LabStep {

    private static final long GENERATED_ID_BOUND_EXCLUSIVE = 9_007_199_254_739_992L;

    @Id
    private Long id;

    @CreatedDate
    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    @Column(name = "experiment_id", nullable = false)
    private Long labId;
    @Column(name = "experiment_item_no", nullable = false)
    private Integer stepNo;
    @Column(name = "experiment_item_name", nullable = false, length = 128)
    private String title;
    @Column(name = "experiment_item_type", nullable = false)
    private Integer experimentItemType = 5;
    @Column(name = "question_type", nullable = false, length = 32)
    private String questionType;
    @Column(name = "experiment_item_content", nullable = false, columnDefinition = "TEXT")
    private String content;
    @Column(name = "experiment_item_answer", columnDefinition = "TEXT")
    private String experimentItemAnswer;
    @Column(name = "answer_config_json", columnDefinition = "json")
    private String answerConfigJson;
    @Column(name = "scoring_config_json", columnDefinition = "json")
    private String scoringConfigJson;
    @Column(name = "experiment_item_score", nullable = false)
    private Integer stepScore;
    @Column(name = "state", nullable = false)
    private Integer state = 1;
    @Column(name = "question_id")
    private Long questionId;
    @Column(name = "question_snapshot_json", nullable = false, columnDefinition = "TEXT")
    private String questionSnapshotJson = "{}";
    @Column(name = "editor_language", nullable = false, length = 32)
    private String editorLanguage = "TEXT";
    @Column(name = "allow_paste", nullable = false)
    private boolean allowPaste;

    @PrePersist
    void prePersist() {
        ensureId();
        applyDerivedFields();
        OffsetDateTime now = OffsetDateTime.now();
        if (createdAt == null) {
            createdAt = now;
        }
        if (updatedAt == null) {
            updatedAt = now;
        }
    }

    @PreUpdate
    void preUpdate() {
        applyDerivedFields();
        updatedAt = OffsetDateTime.now();
    }

    private void ensureId() {
        if (getId() != null) {
            return;
        }
        long candidate = ThreadLocalRandom.current().nextLong(1, GENERATED_ID_BOUND_EXCLUSIVE);
        this.id = candidate;
    }

    private void applyDerivedFields() {
        if (experimentItemType == null) {
            experimentItemType = mapQuestionType(questionType);
        }
        if (experimentItemAnswer == null) {
            experimentItemAnswer = answerConfigJson;
        }
        if ((questionSnapshotJson == null || questionSnapshotJson.isBlank() || "{}".equals(questionSnapshotJson)) && questionType != null) {
            questionSnapshotJson = "{\"legacyQuestionType\":\"" + questionType + "\"}";
        }
        if (editorLanguage == null || editorLanguage.isBlank()) {
            editorLanguage = "CODE".equals(questionType) ? "JAVA" : "TEXT";
        }
    }

    private Integer mapQuestionType(String value) {
        if (value == null) {
            return 5;
        }
        return switch (value) {
            case "SINGLE_CHOICE" -> 1;
            case "MULTIPLE_CHOICE" -> 2;
            case "FILL_BLANK" -> 3;
            case "TRUE_FALSE" -> 4;
            case "CODE" -> 6;
            default -> 5;
        };
    }

    public Long getId() { return id; }
    public OffsetDateTime getCreatedAt() { return createdAt; }
    public OffsetDateTime getUpdatedAt() { return updatedAt; }
    public Long getLabId() { return labId; }
    public void setLabId(Long labId) { this.labId = labId; }
    public Integer getStepNo() { return stepNo; }
    public void setStepNo(Integer stepNo) { this.stepNo = stepNo; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getQuestionType() { return questionType; }
    public void setQuestionType(String questionType) {
        this.questionType = questionType;
        this.experimentItemType = mapQuestionType(questionType);
        if (questionSnapshotJson == null || questionSnapshotJson.isBlank() || "{}".equals(questionSnapshotJson)) {
            this.questionSnapshotJson = questionType == null ? "{}" : "{\"legacyQuestionType\":\"" + questionType + "\"}";
        }
        if (editorLanguage == null || editorLanguage.isBlank() || "TEXT".equals(editorLanguage)) {
            this.editorLanguage = "CODE".equals(questionType) ? "JAVA" : "TEXT";
        }
    }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public String getAnswerConfigJson() { return answerConfigJson; }
    public void setAnswerConfigJson(String answerConfigJson) {
        this.answerConfigJson = answerConfigJson;
        this.experimentItemAnswer = answerConfigJson;
    }
    public String getScoringConfigJson() { return scoringConfigJson; }
    public void setScoringConfigJson(String scoringConfigJson) { this.scoringConfigJson = scoringConfigJson; }
    public Integer getStepScore() { return stepScore; }
    public void setStepScore(Integer stepScore) { this.stepScore = stepScore; }
    public boolean isAllowPaste() { return allowPaste; }
    public void setAllowPaste(boolean allowPaste) { this.allowPaste = allowPaste; }
    public Integer getExperimentItemType() { return experimentItemType; }
    public void setExperimentItemType(Integer experimentItemType) { this.experimentItemType = experimentItemType; }
    public String getExperimentItemAnswer() { return experimentItemAnswer; }
    public void setExperimentItemAnswer(String experimentItemAnswer) { this.experimentItemAnswer = experimentItemAnswer; }
    public Integer getState() { return state; }
    public void setState(Integer state) { this.state = state; }
    public Long getQuestionId() { return questionId; }
    public void setQuestionId(Long questionId) { this.questionId = questionId; }
    public String getQuestionSnapshotJson() { return questionSnapshotJson; }
    public void setQuestionSnapshotJson(String questionSnapshotJson) { this.questionSnapshotJson = questionSnapshotJson; }
    public String getEditorLanguage() { return editorLanguage; }
    public void setEditorLanguage(String editorLanguage) { this.editorLanguage = editorLanguage; }
}
