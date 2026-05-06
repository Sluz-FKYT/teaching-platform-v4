package com.opencode.teachingplatform.student.service;

import com.opencode.teachingplatform.auth.entity.SysUser;
import com.opencode.teachingplatform.auth.repository.SysUserRepository;
import com.opencode.teachingplatform.auth.security.CurrentUser;
import com.opencode.teachingplatform.clazz.entity.ClassRoom;
import com.opencode.teachingplatform.clazz.repository.ClassRoomRepository;
import com.opencode.teachingplatform.common.api.PagedResult;
import com.opencode.teachingplatform.common.audit.AuditLogService;
import com.opencode.teachingplatform.common.enums.UserRole;
import com.opencode.teachingplatform.common.enums.UserStatus;
import com.opencode.teachingplatform.common.exception.BusinessException;
import com.opencode.teachingplatform.student.dto.StudentRequests.CreateStudentRequest;
import com.opencode.teachingplatform.student.dto.StudentRequests.ImportStudentRow;
import com.opencode.teachingplatform.student.dto.StudentRequests.ImportStudentsRequest;
import com.opencode.teachingplatform.student.dto.StudentRequests.UpdateStudentRequest;
import com.opencode.teachingplatform.student.entity.ClassMember;
import com.opencode.teachingplatform.student.repository.ClassMemberRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class StudentService {

    private final SysUserRepository sysUserRepository;
    private final ClassRoomRepository classRoomRepository;
    private final ClassMemberRepository classMemberRepository;
    private final AuditLogService auditLogService;

    public StudentService(
            SysUserRepository sysUserRepository,
            ClassRoomRepository classRoomRepository,
            ClassMemberRepository classMemberRepository,
            AuditLogService auditLogService
    ) {
        this.sysUserRepository = sysUserRepository;
        this.classRoomRepository = classRoomRepository;
        this.classMemberRepository = classMemberRepository;
        this.auditLogService = auditLogService;
    }

    @Transactional(readOnly = true)
    public PagedResult<Map<String, Object>> listStudents(CurrentUser currentUser, Long classId) {
        List<Long> accessibleClassIds = resolveAccessibleClassIds(currentUser, classId);
        if (accessibleClassIds.isEmpty()) {
            return new PagedResult<>(List.of(), 1, 10, 0);
        }

        Set<Long> studentIds = classMemberRepository.findByClassIdIn(accessibleClassIds).stream()
                .map(ClassMember::getStudentUserId)
                .collect(java.util.stream.Collectors.toCollection(LinkedHashSet::new));

        List<Map<String, Object>> items = sysUserRepository.findAllById(studentIds).stream()
                .filter(user -> user.getRole() == UserRole.STUDENT)
                .map(user -> toView(user, findClassIdForStudent(accessibleClassIds, user.getId())))
                .toList();
        return new PagedResult<>(items, 1, 10, items.size());
    }

    @Transactional
    public Map<String, Object> createStudent(CurrentUser currentUser, CreateStudentRequest request) {
        ClassRoom ownedClass = findOwnedClass(currentUser, request.classId());
        if (sysUserRepository.existsByUsername(request.username())) {
            throw new BusinessException(40900, "用户名已存在");
        }

        SysUser student = buildStudent(request.username(), request.displayName(), request.password());
        SysUser saved = saveStudentOrThrow(student);
        linkStudentToClass(saved.getId(), ownedClass.getId());
        auditLogService.log(currentUser.id(), "STUDENT_CREATE", "STUDENT", saved.getId(), Map.of(
                "username", saved.getUsername(),
                "classId", ownedClass.getId()
        ));
        return toView(saved, ownedClass.getId());
    }

    @Transactional
    public Map<String, Object> importStudents(CurrentUser currentUser, ImportStudentsRequest request) {
        ClassRoom ownedClass = findOwnedClass(currentUser, request.classId());
        List<Map<String, Object>> failures = new ArrayList<>();
        Set<String> seenUsernames = new HashSet<>();
        int successCount = 0;

        for (int index = 0; index < request.rows().size(); index++) {
            ImportStudentRow row = request.rows().get(index);
            String username = row.username();
            String reason = null;
            if (!seenUsernames.add(username) || sysUserRepository.existsByUsername(username)) {
                reason = "username already exists";
            }

            if (reason != null) {
                failures.add(Map.of(
                        "row", index + 1,
                        "reason", reason
                ));
                continue;
            }

            SysUser saved;
            try {
                saved = saveStudentOrThrow(buildStudent(username, row.displayName(), row.password()));
            } catch (BusinessException ex) {
                failures.add(Map.of(
                        "row", index + 1,
                        "reason", "username already exists"
                ));
                continue;
            }
            linkStudentToClass(saved.getId(), ownedClass.getId());
            auditLogService.log(currentUser.id(), "STUDENT_CREATE", "STUDENT", saved.getId(), Map.of(
                    "username", saved.getUsername(),
                    "classId", ownedClass.getId(),
                    "source", "import"
            ));
            successCount++;
        }

        Map<String, Object> summary = Map.of(
                "classId", ownedClass.getId(),
                "successCount", successCount,
                "failureCount", failures.size()
        );
        auditLogService.log(currentUser.id(), "STUDENT_IMPORT", "CLASS", ownedClass.getId(), summary);

        return Map.of(
                "successCount", successCount,
                "failureCount", failures.size(),
                "failures", failures
        );
    }

    @Transactional
    public Map<String, Object> updateStudent(CurrentUser currentUser, Long studentId, UpdateStudentRequest request) {
        SysUser student = findOwnedStudent(currentUser, studentId);
        ClassRoom targetClass = findOwnedClass(currentUser, request.classId());
        student.setDisplayName(request.displayName());
        SysUser saved = sysUserRepository.save(student);

        ClassMember membership = classMemberRepository.findByStudentUserId(studentId).stream()
                .filter(member -> classRoomRepository.findByIdAndTeacherUserId(member.getClassId(), currentUser.id()).isPresent())
                .findFirst()
                .orElseThrow(() -> new BusinessException(40300, "无权限访问该学生"));
        membership.setClassId(targetClass.getId());
        classMemberRepository.save(membership);

        return toView(saved, targetClass.getId());
    }

    @Transactional
    public Map<String, Object> changeStatus(CurrentUser currentUser, Long studentId, String status) {
        UserStatus targetStatus = parseStatus(status);
        SysUser student = findOwnedStudent(currentUser, studentId);
        student.setStatus(targetStatus);
        SysUser saved = sysUserRepository.save(student);
        Long classId = classMemberRepository.findByStudentUserId(studentId).stream()
                .map(ClassMember::getClassId)
                .filter(id -> classRoomRepository.findByIdAndTeacherUserId(id, currentUser.id()).isPresent())
                .findFirst()
                .orElseThrow(() -> new BusinessException(40300, "无权限访问该学生"));
        return toView(saved, classId);
    }

    private List<Long> resolveAccessibleClassIds(CurrentUser currentUser, Long classId) {
        if (classId != null) {
            return List.of(findOwnedClass(currentUser, classId).getId());
        }
        return classRoomRepository.findByTeacherUserId(currentUser.id()).stream()
                .map(ClassRoom::getId)
                .toList();
    }

    private Long findClassIdForStudent(List<Long> accessibleClassIds, Long studentId) {
        return classMemberRepository.findByStudentUserId(studentId).stream()
                .map(ClassMember::getClassId)
                .filter(accessibleClassIds::contains)
                .findFirst()
                .orElse(null);
    }

    private SysUser findOwnedStudent(CurrentUser currentUser, Long studentId) {
        SysUser student = sysUserRepository.findById(studentId)
                .filter(user -> user.getRole() == UserRole.STUDENT)
                .orElseThrow(() -> new BusinessException(40400, "学生不存在"));
        boolean owned = classMemberRepository.findByStudentUserId(studentId).stream()
                .map(ClassMember::getClassId)
                .anyMatch(classId -> classRoomRepository.findByIdAndTeacherUserId(classId, currentUser.id()).isPresent());
        if (!owned) {
            throw new BusinessException(40300, "无权限访问该学生");
        }
        return student;
    }

    private ClassRoom findOwnedClass(CurrentUser currentUser, Long classId) {
        return classRoomRepository.findByIdAndTeacherUserId(classId, currentUser.id())
                .orElseThrow(() -> new BusinessException(40300, "无权限访问该班级"));
    }

    private UserStatus parseStatus(String status) {
        if (status == null || status.isBlank()) {
            throw new BusinessException(40000, "学生状态不合法");
        }
        try {
            return UserStatus.valueOf(status.trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw new BusinessException(40000, "学生状态不合法");
        }
    }

    private SysUser buildStudent(String username, String displayName, String password) {
        SysUser user = new SysUser();
        user.setUsername(username);
        user.setPasswordHash("{noop}" + password);
        user.setRole(UserRole.STUDENT);
        user.setDisplayName(displayName);
        user.setStatus(UserStatus.ACTIVE);
        user.setMustChangePassword(false);
        return user;
    }

    private SysUser saveStudentOrThrow(SysUser student) {
        try {
            return sysUserRepository.saveAndFlush(student);
        } catch (DataIntegrityViolationException ex) {
            throw new BusinessException(40900, "用户名已存在");
        }
    }

    private void linkStudentToClass(Long studentId, Long classId) {
        ClassMember member = new ClassMember();
        member.setClassId(classId);
        member.setStudentUserId(studentId);
        member.setJoinedAt(OffsetDateTime.now());
        classMemberRepository.save(member);
    }

    private Map<String, Object> toView(SysUser user, Long classId) {
        return Map.of(
                "id", user.getId(),
                "username", user.getUsername(),
                "displayName", user.getDisplayName(),
                "classId", classId,
                "status", user.getStatus().name()
        );
    }
}
