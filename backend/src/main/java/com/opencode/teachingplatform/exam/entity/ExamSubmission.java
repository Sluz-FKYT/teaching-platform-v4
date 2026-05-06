package com.opencode.teachingplatform.exam.entity;

import com.opencode.teachingplatform.common.model.BaseEntity;
import com.opencode.teachingplatform.exam.enums.ExamSubmissionStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;

import java.time.OffsetDateTime;

@Entity
@Table(name = "exam_submission")
public class ExamSubmission extends BaseEntity {
    @Column(name = "exam_id", nullable = false)
    private Long examId;
    @Column(name = "student_id", nullable = false)
    private Long studentId;
    @Column(name = "started_at")
    private OffsetDateTime startedAt;
    @Column(name = "submitted_at")
    private OffsetDateTime submittedAt;
    @Column(name = "deadline_at")
    private OffsetDateTime deadlineAt;
    @Column(name = "auto_score")
    private Double autoScore;
    @Column(name = "manual_score")
    private Double manualScore;
    @Column(name = "total_score")
    private Double totalScore;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private ExamSubmissionStatus status;

    public Long getExamId() { return examId; }
    public void setExamId(Long examId) { this.examId = examId; }
    public Long getStudentId() { return studentId; }
    public void setStudentId(Long studentId) { this.studentId = studentId; }
    public OffsetDateTime getStartedAt() { return startedAt; }
    public void setStartedAt(OffsetDateTime startedAt) { this.startedAt = startedAt; }
    public OffsetDateTime getSubmittedAt() { return submittedAt; }
    public void setSubmittedAt(OffsetDateTime submittedAt) { this.submittedAt = submittedAt; }
    public OffsetDateTime getDeadlineAt() { return deadlineAt; }
    public void setDeadlineAt(OffsetDateTime deadlineAt) { this.deadlineAt = deadlineAt; }
    public Double getAutoScore() { return autoScore; }
    public void setAutoScore(Double autoScore) { this.autoScore = autoScore; }
    public Double getManualScore() { return manualScore; }
    public void setManualScore(Double manualScore) { this.manualScore = manualScore; }
    public Double getTotalScore() { return totalScore; }
    public void setTotalScore(Double totalScore) { this.totalScore = totalScore; }
    public ExamSubmissionStatus getStatus() { return status; }
    public void setStatus(ExamSubmissionStatus status) { this.status = status; }
}
