package com.opencode.teachingplatform.question.entity;

import com.opencode.teachingplatform.common.model.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "question_bank")
public class QuestionBank extends BaseEntity {

    @Column(nullable = false, unique = true, length = 64)
    private String code;
    @Column(nullable = false, length = 32)
    private String type;
    @Column(nullable = false, columnDefinition = "TEXT")
    private String stem;
    @Column(nullable = false, length = 32)
    private String difficulty;
    @Column(name = "default_score", nullable = false)
    private Integer defaultScore;
    @Column(name = "options_json", columnDefinition = "json")
    private String optionsJson;
    @Column(name = "answer_json", columnDefinition = "json")
    private String answerJson;
    @Column(name = "analysis_text", columnDefinition = "TEXT")
    private String analysisText;
    @Column(name = "scoring_config_json", columnDefinition = "json")
    private String scoringConfigJson;
    @Column(name = "created_by", nullable = false)
    private Long createdBy;

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public String getStem() { return stem; }
    public void setStem(String stem) { this.stem = stem; }
    public String getDifficulty() { return difficulty; }
    public void setDifficulty(String difficulty) { this.difficulty = difficulty; }
    public Integer getDefaultScore() { return defaultScore; }
    public void setDefaultScore(Integer defaultScore) { this.defaultScore = defaultScore; }
    public String getOptionsJson() { return optionsJson; }
    public void setOptionsJson(String optionsJson) { this.optionsJson = optionsJson; }
    public String getAnswerJson() { return answerJson; }
    public void setAnswerJson(String answerJson) { this.answerJson = answerJson; }
    public String getAnalysisText() { return analysisText; }
    public void setAnalysisText(String analysisText) { this.analysisText = analysisText; }
    public String getScoringConfigJson() { return scoringConfigJson; }
    public void setScoringConfigJson(String scoringConfigJson) { this.scoringConfigJson = scoringConfigJson; }
    public Long getCreatedBy() { return createdBy; }
    public void setCreatedBy(Long createdBy) { this.createdBy = createdBy; }
}
