package com.opencode.teachingplatform.lab.repository;

import com.opencode.teachingplatform.lab.entity.LabSubmission;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface LabSubmissionRepository extends JpaRepository<LabSubmission, Long> {
    Optional<LabSubmission> findByLabIdAndStudentId(Long labId, Long studentId);
    Optional<LabSubmission> findFirstByLabIdAndStudentIdOrderByIdDesc(Long labId, Long studentId);
    List<LabSubmission> findByStudentId(Long studentId);
    List<LabSubmission> findByLabId(Long labId);
    List<LabSubmission> findByLabIdIn(List<Long> labIds);
}
