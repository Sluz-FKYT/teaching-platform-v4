package com.opencode.teachingplatform.clazz.service;

import com.opencode.teachingplatform.auth.security.CurrentUser;
import com.opencode.teachingplatform.clazz.dto.ClassRequests.CreateClassRequest;
import com.opencode.teachingplatform.clazz.dto.ClassRequests.UpdateClassRequest;
import com.opencode.teachingplatform.clazz.entity.ClassRoom;
import com.opencode.teachingplatform.clazz.repository.ClassRoomRepository;
import com.opencode.teachingplatform.common.api.PagedResult;
import com.opencode.teachingplatform.common.audit.AuditLogService;
import com.opencode.teachingplatform.common.enums.ClassStatus;
import com.opencode.teachingplatform.common.exception.BusinessException;
import com.opencode.teachingplatform.student.repository.ClassMemberRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
public class ClassService {

    private final ClassRoomRepository classRoomRepository;
    private final ClassMemberRepository classMemberRepository;
    private final AuditLogService auditLogService;

    public ClassService(ClassRoomRepository classRoomRepository, ClassMemberRepository classMemberRepository, AuditLogService auditLogService) {
        this.classRoomRepository = classRoomRepository;
        this.classMemberRepository = classMemberRepository;
        this.auditLogService = auditLogService;
    }

    @Transactional(readOnly = true)
    public PagedResult<Map<String, Object>> listTeacherClasses(CurrentUser currentUser) {
        List<Map<String, Object>> items = classRoomRepository.findByTeacherUserId(currentUser.id()).stream()
                .map(this::toView)
                .toList();
        return new PagedResult<>(items, 1, 10, items.size());
    }

    @Transactional(readOnly = true)
    public Map<String, Object> getClass(CurrentUser currentUser, Long id) {
        return toView(findOwnedClass(currentUser, id));
    }

    @Transactional
    public Map<String, Object> createClass(CurrentUser currentUser, CreateClassRequest request) {
        if (classRoomRepository.existsByCode(request.code())) {
            throw new BusinessException(40900, "班级编码已存在");
        }
        ClassRoom room = new ClassRoom();
        room.setName(request.name());
        room.setCode(request.code());
        room.setTeacherUserId(currentUser.id());
        room.setStatus(ClassStatus.ACTIVE);
        try {
            ClassRoom saved = classRoomRepository.save(room);
            auditLogService.log(currentUser.id(), "CLASS_CREATE", "CLASS", saved.getId(), Map.of("code", saved.getCode()));
            return toView(saved);
        } catch (DataIntegrityViolationException ex) {
            throw new BusinessException(40900, "班级编码已存在");
        }
    }

    @Transactional
    public Map<String, Object> updateClass(CurrentUser currentUser, Long id, UpdateClassRequest request) {
        ClassRoom room = findOwnedClass(currentUser, id);
        room.setName(request.name());
        room.setStatus(request.status());
        ClassRoom saved = classRoomRepository.save(room);
        return toView(saved);
    }

    @Transactional
    public void deleteClass(CurrentUser currentUser, Long id) {
        ClassRoom room = findOwnedClass(currentUser, id);
        if (classMemberRepository.existsByClassId(room.getId())) {
            throw new BusinessException(40020, "班级下存在学生，不能删除");
        }
        classRoomRepository.delete(room);
    }

    public void assertTeacherOwnsClass(CurrentUser currentUser, Long classId) {
        findOwnedClass(currentUser, classId);
    }

    private ClassRoom findOwnedClass(CurrentUser currentUser, Long classId) {
        return classRoomRepository.findByIdAndTeacherUserId(classId, currentUser.id())
                .orElseThrow(() -> new BusinessException(40300, "无权限访问该班级"));
    }

    private Map<String, Object> toView(ClassRoom room) {
        return Map.of(
                "id", room.getId(),
                "name", room.getName(),
                "code", room.getCode(),
                "teacherUserId", room.getTeacherUserId(),
                "status", room.getStatus().name(),
                "studentCount", classMemberRepository.countByClassId(room.getId())
        );
    }
}
