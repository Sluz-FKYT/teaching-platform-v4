package com.opencode.teachingplatform.lab.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.OffsetDateTime;

@Entity
@Table(name = "lab_step_answer_log")
public class LabStepAnswerLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "lab_step_answer_id", nullable = false)
    private Long labStepAnswerId;

    @Column(name = "content", columnDefinition = "TEXT")
    private String content;

    @Column(name = "fill_time", nullable = false)
    private OffsetDateTime fillTime;

    public Long getId() {
        return id;
    }

    public Long getLabStepAnswerId() {
        return labStepAnswerId;
    }

    public void setLabStepAnswerId(Long labStepAnswerId) {
        this.labStepAnswerId = labStepAnswerId;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public OffsetDateTime getFillTime() {
        return fillTime;
    }

    public void setFillTime(OffsetDateTime fillTime) {
        this.fillTime = fillTime;
    }
}
