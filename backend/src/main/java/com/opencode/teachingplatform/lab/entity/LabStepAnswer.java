package com.opencode.teachingplatform.lab.entity;

import com.opencode.teachingplatform.common.model.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "experiment_answer")
/**
 * 实验步骤答案表。
 *
 * <p>它既保存学生真实作答内容，也保存自动评分、建议分、最终分、分数来源和教师评语，
 * 是“学生作答链路”和“评分链路”汇合的核心表。</p>
 */
public class LabStepAnswer extends BaseEntity {

    @Column(name = "experiment_submission_id", nullable = false)
    private Long labSubmissionId;
    @Column(name = "experiment_item_id", nullable = false)
    private Long labStepId;
    @Column(name = "question_id")
    private Long questionId;
    @Column(name = "question_snapshot_json", nullable = false, columnDefinition = "TEXT")
    // 保存提交当时的题目快照，避免题目后续被修改后影响历史评分结果。
    private String questionSnapshotJson = "{}";
    @Column(name = "answer_text", columnDefinition = "TEXT")
    private String answerText;
    @Column(name = "answer_json", columnDefinition = "TEXT")
    private String answerJson;
    @Column(name = "answer_file_path")
    private String answerFilePath;
    @Column
    private Double score;
    @Column(name = "auto_score")
    // 系统自动计算出的分数。
    private Double autoScore;
    @Column(name = "suggested_score")
    // 系统给教师的建议分，常用于主观题或需要教师确认的情况。
    private Double suggestedScore;
    @Column(name = "score_source", nullable = false, length = 16)
    // 记录最终分是来自教师、自动评分还是教师采纳了自动建议分。
    private String scoreSource = "TEACHER";
    @Column(name = "auto_judge_detail", columnDefinition = "TEXT")
    private String autoJudgeDetail;
    @Column(name = "teacher_comment", columnDefinition = "TEXT")
    private String teacherComment;
    @Column(name = "accepted_auto_score", nullable = false)
    private boolean acceptedAutoScore;
    @Column(name = "editor_language", nullable = false, length = 32)
    private String editorLanguage = "TEXT";

    public Long getLabSubmissionId() { return labSubmissionId; }
    public void setLabSubmissionId(Long labSubmissionId) { this.labSubmissionId = labSubmissionId; }
    public Long getLabStepId() { return labStepId; }
    public void setLabStepId(Long labStepId) { this.labStepId = labStepId; }
    public Long getQuestionId() { return questionId; }
    public void setQuestionId(Long questionId) { this.questionId = questionId; }
    public String getQuestionSnapshotJson() { return questionSnapshotJson; }
    public void setQuestionSnapshotJson(String questionSnapshotJson) { this.questionSnapshotJson = questionSnapshotJson; }
    public String getAnswerText() { return answerText; }
    public void setAnswerText(String answerText) { this.answerText = answerText; }
    public String getAnswerJson() { return answerJson; }
    public void setAnswerJson(String answerJson) { this.answerJson = answerJson; }
    public String getAnswerFilePath() { return answerFilePath; }
    public void setAnswerFilePath(String answerFilePath) { this.answerFilePath = answerFilePath; }
    public Double getScore() { return score; }
    public void setScore(Double score) { this.score = score; }
    public Double getAutoScore() { return autoScore; }
    public void setAutoScore(Double autoScore) { this.autoScore = autoScore; }
    public Double getSuggestedScore() { return suggestedScore; }
    public void setSuggestedScore(Double suggestedScore) { this.suggestedScore = suggestedScore; }
    public String getScoreSource() { return scoreSource; }
    public void setScoreSource(String scoreSource) { this.scoreSource = scoreSource; }
    public String getAutoJudgeDetail() { return autoJudgeDetail; }
    public void setAutoJudgeDetail(String autoJudgeDetail) { this.autoJudgeDetail = autoJudgeDetail; }
    public String getTeacherComment() { return teacherComment; }
    public void setTeacherComment(String teacherComment) { this.teacherComment = teacherComment; }
    public boolean isAcceptedAutoScore() { return acceptedAutoScore; }
    public void setAcceptedAutoScore(boolean acceptedAutoScore) { this.acceptedAutoScore = acceptedAutoScore; }
    public String getEditorLanguage() { return editorLanguage; }
    public void setEditorLanguage(String editorLanguage) { this.editorLanguage = editorLanguage; }
}
