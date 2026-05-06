package com.opencode.teachingplatform.homework.entity;

import com.opencode.teachingplatform.common.model.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "homework_answer")
public class HomeworkAnswer extends BaseEntity {
    @Column(name = "homework_submission_id", nullable = false)
    private Long homeworkSubmissionId;
    @Column(name = "question_id")
    private Long questionId;
    @Column(name = "homework_question_id")
    private Long homeworkQuestionId;
    @Column(name = "answer_text", columnDefinition = "TEXT")
    private String answerText;
    @Column(name = "answer_json", columnDefinition = "json")
    private String answerJson;
    @Column
    private Double score;
    @Column(name = "auto_score")
    private Double autoScore;
    @Column(name = "suggested_score")
    private Double suggestedScore;
    @Column(name = "score_source", nullable = false, length = 32)
    private String scoreSource = "TEACHER";
    @Column(name = "judge_detail", columnDefinition = "TEXT")
    private String judgeDetail;
    @Column(name = "teacher_comment", columnDefinition = "TEXT")
    private String teacherComment;
    @Column(name = "accepted_auto_score", nullable = false)
    private boolean acceptedAutoScore;

    public Long getHomeworkSubmissionId() { return homeworkSubmissionId; }
    public void setHomeworkSubmissionId(Long homeworkSubmissionId) { this.homeworkSubmissionId = homeworkSubmissionId; }
    public Long getQuestionId() { return questionId; }
    public void setQuestionId(Long questionId) { this.questionId = questionId; }
    public Long getHomeworkQuestionId() { return homeworkQuestionId; }
    public void setHomeworkQuestionId(Long homeworkQuestionId) { this.homeworkQuestionId = homeworkQuestionId; }
    public String getAnswerText() { return answerText; }
    public void setAnswerText(String answerText) { this.answerText = answerText; }
    public String getAnswerJson() { return answerJson; }
    public void setAnswerJson(String answerJson) { this.answerJson = answerJson; }
    public Double getScore() { return score; }
    public void setScore(Double score) { this.score = score; }
    public Double getAutoScore() { return autoScore; }
    public void setAutoScore(Double autoScore) { this.autoScore = autoScore; }
    public Double getSuggestedScore() { return suggestedScore; }
    public void setSuggestedScore(Double suggestedScore) { this.suggestedScore = suggestedScore; }
    public String getScoreSource() { return scoreSource; }
    public void setScoreSource(String scoreSource) { this.scoreSource = scoreSource; }
    public String getJudgeDetail() { return judgeDetail; }
    public void setJudgeDetail(String judgeDetail) { this.judgeDetail = judgeDetail; }
    public String getTeacherComment() { return teacherComment; }
    public void setTeacherComment(String teacherComment) { this.teacherComment = teacherComment; }
    public boolean isAcceptedAutoScore() { return acceptedAutoScore; }
    public void setAcceptedAutoScore(boolean acceptedAutoScore) { this.acceptedAutoScore = acceptedAutoScore; }
}
