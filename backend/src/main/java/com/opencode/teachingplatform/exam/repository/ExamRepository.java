package com.opencode.teachingplatform.exam.repository;

import com.opencode.teachingplatform.common.enums.ActivityStatus;
import com.opencode.teachingplatform.exam.entity.Exam;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ExamRepository extends JpaRepository<Exam, Long> {
    List<Exam> findByCreatedBy(Long createdBy);
    List<Exam> findByClassIdAndStatus(Long classId, ActivityStatus status);
    List<Exam> findByClassId(Long classId);
}
