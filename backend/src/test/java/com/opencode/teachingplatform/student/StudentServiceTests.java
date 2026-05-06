package com.opencode.teachingplatform.student;

import com.opencode.teachingplatform.auth.entity.SysUser;
import com.opencode.teachingplatform.auth.repository.SysUserRepository;
import com.opencode.teachingplatform.auth.security.CurrentUser;
import com.opencode.teachingplatform.clazz.repository.ClassRoomRepository;
import com.opencode.teachingplatform.common.audit.AuditLogService;
import com.opencode.teachingplatform.common.enums.UserRole;
import com.opencode.teachingplatform.common.enums.UserStatus;
import com.opencode.teachingplatform.common.exception.BusinessException;
import com.opencode.teachingplatform.student.repository.ClassMemberRepository;
import com.opencode.teachingplatform.student.service.StudentService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StudentServiceTests {

    @Mock
    private SysUserRepository sysUserRepository;

    @Mock
    private ClassRoomRepository classRoomRepository;

    @Mock
    private ClassMemberRepository classMemberRepository;

    @Mock
    private AuditLogService auditLogService;

    @InjectMocks
    private StudentService studentService;

    @Test
    void changeStatusRejectsBlankStatus() {
        CurrentUser teacher = new CurrentUser(1L, "t9001", "演示教师", UserRole.TEACHER);
        BusinessException ex = assertThrows(BusinessException.class,
                () -> studentService.changeStatus(teacher, 3L, " "));

        assertEquals(40000, ex.getCode());
        assertEquals("学生状态不合法", ex.getMessage());
        verify(sysUserRepository, never()).save(org.mockito.ArgumentMatchers.any(SysUser.class));
    }

    @Test
    void createStudentConvertsUniqueConstraintConflictToBusinessError() {
        CurrentUser teacher = new CurrentUser(1L, "t9001", "演示教师", UserRole.TEACHER);

        when(classRoomRepository.findByIdAndTeacherUserId(1L, 1L)).thenReturn(Optional.of(new com.opencode.teachingplatform.clazz.entity.ClassRoom()));
        when(sysUserRepository.existsByUsername("20260002")).thenReturn(false);
        when(sysUserRepository.saveAndFlush(org.mockito.ArgumentMatchers.any(SysUser.class)))
                .thenThrow(new DataIntegrityViolationException("duplicate"));

        BusinessException ex = assertThrows(BusinessException.class,
                () -> studentService.createStudent(teacher, new com.opencode.teachingplatform.student.dto.StudentRequests.CreateStudentRequest("20260002", "李四", "123456", 1L)));

        assertEquals(40900, ex.getCode());
        assertEquals("用户名已存在", ex.getMessage());
    }
}
