package com.opencode.teachingplatform.lab.repository;

import com.opencode.teachingplatform.lab.entity.Lab;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface LabRepository extends JpaRepository<Lab, Long> {
    List<Lab> findByCreatedByOrderByIdDesc(Long createdBy);
    List<Lab> findByClassIdInAndStatusOrderByIdDesc(Collection<Long> classIds, com.opencode.teachingplatform.common.enums.ActivityStatus status);
    Optional<Lab> findByIdAndCreatedBy(Long id, Long createdBy);
}
