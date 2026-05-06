package com.opencode.teachingplatform.clazz;

import com.opencode.teachingplatform.auth.security.CurrentUser;
import com.opencode.teachingplatform.clazz.entity.ClassRoom;
import com.opencode.teachingplatform.clazz.repository.ClassRoomRepository;
import com.opencode.teachingplatform.clazz.service.ClassService;
import com.opencode.teachingplatform.common.audit.AuditLogService;
import com.opencode.teachingplatform.common.enums.ClassStatus;
import com.opencode.teachingplatform.common.enums.UserRole;
import com.opencode.teachingplatform.common.exception.BusinessException;
import com.opencode.teachingplatform.student.repository.ClassMemberRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ClassServiceTests {

    @Mock
    private ClassRoomRepository classRoomRepository;

    @Mock
    private ClassMemberRepository classMemberRepository;

    @Mock
    private AuditLogService auditLogService;

    @InjectMocks
    private ClassService classService;

    @Test
    void listTeacherClassesUsesRealStudentCount() {
        ClassRoom room = room(10L, 1L);
        when(classRoomRepository.findByTeacherUserId(1L)).thenReturn(List.of(room));
        when(classMemberRepository.countByClassId(10L)).thenReturn(3L);

        Object count = classService.listTeacherClasses(currentUser()).items().getFirst().get("studentCount");

        assertEquals(3L, count);
    }

    @Test
    void deleteClassFailsWhenMembersExist() {
        when(classRoomRepository.findByIdAndTeacherUserId(10L, 1L)).thenReturn(Optional.of(room(10L, 1L)));
        when(classMemberRepository.existsByClassId(10L)).thenReturn(true);

        BusinessException exception = assertThrows(BusinessException.class, () -> classService.deleteClass(currentUser(), 10L));

        assertEquals(40020, exception.getCode());
        assertEquals("班级下存在学生，不能删除", exception.getMessage());
        verify(classRoomRepository, never()).delete(org.mockito.ArgumentMatchers.any(ClassRoom.class));
    }

    private CurrentUser currentUser() {
        return new CurrentUser(1L, "t9001", "演示教师", UserRole.TEACHER);
    }

    private ClassRoom room(Long id, Long teacherUserId) {
        ClassRoom room = new ClassRoom() {
            @Override
            public Long getId() {
                return id;
            }
        };
        room.setName("软件工程 2026-1 班");
        room.setCode("SE2026-1");
        room.setTeacherUserId(teacherUserId);
        room.setStatus(ClassStatus.ACTIVE);
        return room;
    }
}
