package com.opencode.teachingplatform.grading.service;

import com.opencode.teachingplatform.analysis.entity.ScoreRecord;
import com.opencode.teachingplatform.analysis.repository.ScoreRecordRepository;
import com.opencode.teachingplatform.common.enums.BusinessType;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;

@Service
public class ScoreRecordWriter {

    private final ScoreRecordRepository scoreRecordRepository;

    public ScoreRecordWriter(ScoreRecordRepository scoreRecordRepository) {
        this.scoreRecordRepository = scoreRecordRepository;
    }

    public ScoreRecord write(BusinessType businessType,
                             Long businessId,
                             Long studentId,
                             Long classId,
                             Double score,
                             OffsetDateTime gradedAt) {
        ScoreRecord scoreRecord = scoreRecordRepository
                .findByBusinessTypeAndBusinessIdAndStudentId(businessType, businessId, studentId)
                .orElseGet(ScoreRecord::new);
        scoreRecord.setBusinessType(businessType);
        scoreRecord.setBusinessId(businessId);
        scoreRecord.setStudentId(studentId);
        scoreRecord.setClassId(classId);
        scoreRecord.setScore(score);
        scoreRecord.setGradedAt(gradedAt == null ? OffsetDateTime.now() : gradedAt);
        return scoreRecordRepository.save(scoreRecord);
    }
}
