package com.opencode.teachingplatform.clazz.repository;

import com.opencode.teachingplatform.clazz.entity.ClassRoom;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ClassRoomRepository extends JpaRepository<ClassRoom, Long> {
    List<ClassRoom> findByTeacherUserId(Long teacherUserId);
    Optional<ClassRoom> findByIdAndTeacherUserId(Long id, Long teacherUserId);
    boolean existsByCode(String code);
}
