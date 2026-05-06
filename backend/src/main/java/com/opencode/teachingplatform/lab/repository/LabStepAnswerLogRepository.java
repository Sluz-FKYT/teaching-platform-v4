package com.opencode.teachingplatform.lab.repository;

import com.opencode.teachingplatform.lab.entity.LabStepAnswerLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface LabStepAnswerLogRepository extends JpaRepository<LabStepAnswerLog, Long> {
    Optional<LabStepAnswerLog> findFirstByLabStepAnswerIdOrderByFillTimeDescIdDesc(Long labStepAnswerId);
}
