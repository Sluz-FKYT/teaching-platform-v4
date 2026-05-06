package com.opencode.teachingplatform.homework.entity;

import com.opencode.teachingplatform.common.model.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "homework_question")
public class HomeworkQuestion extends BaseEntity {
    @Column(name = "homework_id", nullable = false)
    private Long homeworkId;
    @Column(name = "question_id")
    private Long questionId;
    @Column(name = "sort_order", nullable = false)
    private Integer sortOrder;
    @Column(name = "question_score", nullable = false)
    private Double questionScore;
    @Column(name = "question_snapshot_json", nullable = false, columnDefinition = "json")
    private String questionSnapshotJson = "{}";

    public Long getHomeworkId() { return homeworkId; }
    public void setHomeworkId(Long homeworkId) { this.homeworkId = homeworkId; }
    public Long getQuestionId() { return questionId; }
    public void setQuestionId(Long questionId) { this.questionId = questionId; }
    public Integer getSortOrder() { return sortOrder; }
    public void setSortOrder(Integer sortOrder) { this.sortOrder = sortOrder; }
    public Double getQuestionScore() { return questionScore; }
    public void setQuestionScore(Double questionScore) { this.questionScore = questionScore; }
    public String getQuestionSnapshotJson() { return questionSnapshotJson; }
    public void setQuestionSnapshotJson(String questionSnapshotJson) { this.questionSnapshotJson = questionSnapshotJson; }
}
