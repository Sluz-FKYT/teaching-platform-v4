package com.opencode.teachingplatform.exam.repository;

import com.opencode.teachingplatform.exam.entity.ExamSubmission;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ExamSubmissionRepository extends JpaRepository<ExamSubmission, Long> {
    List<ExamSubmission> findByExamId(Long examId);
    Optional<ExamSubmission> findByExamIdAndStudentId(Long examId, Long studentId);
    long countByExamId(Long examId);
}
