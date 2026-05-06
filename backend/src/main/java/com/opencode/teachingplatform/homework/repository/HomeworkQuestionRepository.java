package com.opencode.teachingplatform.homework.repository;

import com.opencode.teachingplatform.homework.entity.HomeworkQuestion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface HomeworkQuestionRepository extends JpaRepository<HomeworkQuestion, Long> {
    List<HomeworkQuestion> findByHomeworkIdOrderBySortOrder(Long homeworkId);
    List<HomeworkQuestion> findByHomeworkIdOrderBySortOrderAsc(Long homeworkId);
}
