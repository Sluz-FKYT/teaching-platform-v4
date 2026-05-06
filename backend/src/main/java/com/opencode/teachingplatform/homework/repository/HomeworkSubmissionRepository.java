package com.opencode.teachingplatform.homework.repository;

import com.opencode.teachingplatform.homework.entity.HomeworkSubmission;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface HomeworkSubmissionRepository extends JpaRepository<HomeworkSubmission, Long> {
    Optional<HomeworkSubmission> findByHomeworkIdAndStudentId(Long homeworkId, Long studentId);
    List<HomeworkSubmission> findByHomeworkId(Long homeworkId);
    List<HomeworkSubmission> findByHomeworkIdIn(List<Long> homeworkIds);
}
