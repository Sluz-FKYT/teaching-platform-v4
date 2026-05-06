package com.opencode.teachingplatform.material.repository;

import com.opencode.teachingplatform.common.enums.MaterialVisibility;
import com.opencode.teachingplatform.material.entity.CourseMaterial;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CourseMaterialRepository extends JpaRepository<CourseMaterial, Long> {
    List<CourseMaterial> findByUploaderUserIdOrderByIdDesc(Long uploaderUserId);
    List<CourseMaterial> findByVisibilityOrderByIdDesc(MaterialVisibility visibility);
    Optional<CourseMaterial> findByIdAndUploaderUserId(Long id, Long uploaderUserId);
}
