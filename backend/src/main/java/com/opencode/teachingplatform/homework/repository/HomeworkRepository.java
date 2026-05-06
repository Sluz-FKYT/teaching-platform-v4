package com.opencode.teachingplatform.homework.repository;

import com.opencode.teachingplatform.homework.entity.Homework;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface HomeworkRepository extends JpaRepository<Homework, Long> {
    List<Homework> findByCreatedByOrderByIdDesc(Long createdBy);
    List<Homework> findByClassIdInAndStatusOrderByIdDesc(Collection<Long> classIds, com.opencode.teachingplatform.common.enums.ActivityStatus status);
    Optional<Homework> findByIdAndCreatedBy(Long id, Long createdBy);
}
