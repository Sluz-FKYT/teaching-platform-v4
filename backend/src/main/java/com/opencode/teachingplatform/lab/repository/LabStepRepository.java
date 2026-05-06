package com.opencode.teachingplatform.lab.repository;

import com.opencode.teachingplatform.lab.entity.LabStep;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface LabStepRepository extends JpaRepository<LabStep, Long> {
    List<LabStep> findByLabIdOrderByStepNoAsc(Long labId);
    Optional<LabStep> findByIdAndLabId(Long id, Long labId);
}
