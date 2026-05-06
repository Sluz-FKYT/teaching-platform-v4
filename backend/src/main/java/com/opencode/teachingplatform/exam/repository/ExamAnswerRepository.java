package com.opencode.teachingplatform.exam.repository;

import com.opencode.teachingplatform.exam.entity.ExamAnswer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

public interface ExamAnswerRepository extends JpaRepository<ExamAnswer, Long> {
    List<ExamAnswer> findByExamSubmissionId(Long examSubmissionId);

    Optional<ExamAnswer> findByExamSubmissionIdAndQuestionId(Long examSubmissionId, Long questionId);

    @Transactional
    void deleteByExamSubmissionId(Long examSubmissionId);
}
