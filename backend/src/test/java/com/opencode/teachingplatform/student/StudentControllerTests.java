package com.opencode.teachingplatform.student;

import com.opencode.teachingplatform.auth.security.CurrentUser;
import com.opencode.teachingplatform.auth.security.JwtAuthenticationFilter;
import com.opencode.teachingplatform.auth.security.SecurityConfig;
import com.opencode.teachingplatform.common.enums.UserRole;
import com.opencode.teachingplatform.common.exception.BusinessException;
import com.opencode.teachingplatform.student.controller.StudentController;
import com.opencode.teachingplatform.student.dto.StudentRequests.CreateStudentRequest;
import com.opencode.teachingplatform.student.dto.StudentRequests.ImportStudentsRequest;
import com.opencode.teachingplatform.student.service.StudentService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(StudentController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(SecurityConfig.class)
class StudentControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private StudentService studentService;

    @MockBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Test
    void teacherCanCreateStudentInsideOwnedClass() throws Exception {
        UsernamePasswordAuthenticationToken authenticationToken = authenticateTeacher(1L, "t9001");
        when(studentService.createStudent(any(CurrentUser.class), any(CreateStudentRequest.class))).thenReturn(Map.of(
                "id", 3,
                "username", "20260002",
                "displayName", "李四",
                "classId", 1,
                "status", "ACTIVE"
        ));

        SecurityContextHolder.getContext().setAuthentication(authenticationToken);
        try {
            mockMvc.perform(post("/api/v1/students")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {
                                      "username": "20260002",
                                      "displayName": "李四",
                                      "password": "123456",
                                      "classId": 1
                                    }
                                    """))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(0))
                    .andExpect(jsonPath("$.message").value("OK"))
                    .andExpect(jsonPath("$.timestamp").isNotEmpty())
                    .andExpect(jsonPath("$.data.id").value(3))
                    .andExpect(jsonPath("$.data.username").value("20260002"))
                    .andExpect(jsonPath("$.data.classId").value(1));
        } finally {
            SecurityContextHolder.clearContext();
        }
    }

    @Test
    void importFailsWhenClassIsNotOwnedByTeacher() throws Exception {
        UsernamePasswordAuthenticationToken authenticationToken = authenticateTeacher(1L, "t9001");
        when(studentService.importStudents(any(CurrentUser.class), any(ImportStudentsRequest.class)))
                .thenThrow(new BusinessException(40300, "无权限访问该班级"));

        SecurityContextHolder.getContext().setAuthentication(authenticationToken);
        try {
            mockMvc.perform(post("/api/v1/students/import")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {
                                      "classId": 2,
                                      "rows": [
                                        {
                                          "username": "20260003",
                                          "displayName": "王五",
                                          "password": "123456"
                                        }
                                      ]
                                    }
                                    """))
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.code").value(40300))
                    .andExpect(jsonPath("$.message").value("无权限访问该班级"))
                    .andExpect(jsonPath("$.timestamp").isNotEmpty());
        } finally {
            SecurityContextHolder.clearContext();
        }
    }

    @Test
    void importReturnsSuccessAndFailureCounts() throws Exception {
        UsernamePasswordAuthenticationToken authenticationToken = authenticateTeacher(1L, "t9001");
        when(studentService.importStudents(any(CurrentUser.class), any(ImportStudentsRequest.class))).thenReturn(Map.of(
                "successCount", 1,
                "failureCount", 1,
                "failures", List.of(Map.of(
                        "row", 2,
                        "reason", "username already exists"
                ))
        ));

        SecurityContextHolder.getContext().setAuthentication(authenticationToken);
        try {
            mockMvc.perform(post("/api/v1/students/import")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {
                                      "classId": 1,
                                      "rows": [
                                        {
                                          "username": "20260002",
                                          "displayName": "李四",
                                          "password": "123456"
                                        },
                                        {
                                          "username": "20260001",
                                          "displayName": "重复学生",
                                          "password": "123456"
                                        }
                                      ]
                                    }
                                    """))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(0))
                    .andExpect(jsonPath("$.data.successCount").value(1))
                    .andExpect(jsonPath("$.data.failureCount").value(1))
                    .andExpect(jsonPath("$.data.failures[0].row").value(2))
                    .andExpect(jsonPath("$.data.failures[0].reason").value("username already exists"));
        } finally {
            SecurityContextHolder.clearContext();
        }
    }

    @Test
    void teacherCanUpdateStudentInsideOwnedClass() throws Exception {
        UsernamePasswordAuthenticationToken authenticationToken = authenticateTeacher(1L, "t9001");
        when(studentService.updateStudent(any(CurrentUser.class), any(Long.class), any())).thenReturn(Map.of(
                "id", 3,
                "username", "20260002",
                "displayName", "李四-已更新",
                "classId", 1,
                "status", "ACTIVE"
        ));

        SecurityContextHolder.getContext().setAuthentication(authenticationToken);
        try {
            mockMvc.perform(put("/api/v1/students/{id}", 3L)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {
                                      "displayName": "李四-已更新",
                                      "classId": 1
                                    }
                                    """))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(0))
                    .andExpect(jsonPath("$.data.id").value(3))
                    .andExpect(jsonPath("$.data.displayName").value("李四-已更新"))
                    .andExpect(jsonPath("$.data.classId").value(1));
        } finally {
            SecurityContextHolder.clearContext();
        }
    }

    @Test
    void teacherCanListStudentsWithClassScope() throws Exception {
        UsernamePasswordAuthenticationToken authenticationToken = authenticateTeacher(1L, "t9001");
        when(studentService.listStudents(any(CurrentUser.class), any())).thenReturn(new com.opencode.teachingplatform.common.api.PagedResult<>(List.of(
                Map.of(
                        "id", 2,
                        "username", "20260001",
                        "displayName", "演示学生",
                        "classId", 1,
                        "status", "ACTIVE"
                )
        ), 1, 10, 1));

        SecurityContextHolder.getContext().setAuthentication(authenticationToken);
        try {
            mockMvc.perform(get("/api/v1/students").param("classId", "1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(0))
                    .andExpect(jsonPath("$.data.items[0].username").value("20260001"))
                    .andExpect(jsonPath("$.data.items[0].classId").value(1))
                    .andExpect(jsonPath("$.data.total").value(1));
        } finally {
            SecurityContextHolder.clearContext();
        }
    }

    @Test
    void teacherCanChangeStudentStatus() throws Exception {
        UsernamePasswordAuthenticationToken authenticationToken = authenticateTeacher(1L, "t9001");
        when(studentService.changeStatus(any(CurrentUser.class), any(Long.class), any())).thenReturn(Map.of(
                "id", 3,
                "username", "20260002",
                "displayName", "李四",
                "classId", 1,
                "status", "DISABLED"
        ));

        SecurityContextHolder.getContext().setAuthentication(authenticationToken);
        try {
            mockMvc.perform(post("/api/v1/students/{id}/status", 3L)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {
                                      "status": "DISABLED"
                                    }
                                    """))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(0))
                    .andExpect(jsonPath("$.data.status").value("DISABLED"));
        } finally {
            SecurityContextHolder.clearContext();
        }
    }

    @Test
    void studentCannotCallStudentManagementEndpoint() throws Exception {
        UsernamePasswordAuthenticationToken authenticationToken = authenticateStudent(2L, "20260001");

        SecurityContextHolder.getContext().setAuthentication(authenticationToken);
        try {
            mockMvc.perform(post("/api/v1/students")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {
                                      "username": "20260002",
                                      "displayName": "李四",
                                      "password": "123456",
                                      "classId": 1
                                    }
                                    """))
                    .andExpect(status().isForbidden());
        } finally {
            SecurityContextHolder.clearContext();
        }
    }

    private UsernamePasswordAuthenticationToken authenticateTeacher(Long id, String username) {
        CurrentUser currentUser = new CurrentUser(id, username, "演示教师", UserRole.TEACHER);
        return new UsernamePasswordAuthenticationToken(
                currentUser,
                null,
                List.of(new SimpleGrantedAuthority("ROLE_TEACHER"))
        );
    }

    private UsernamePasswordAuthenticationToken authenticateStudent(Long id, String username) {
        CurrentUser currentUser = new CurrentUser(id, username, "演示学生", UserRole.STUDENT);
        return new UsernamePasswordAuthenticationToken(
                currentUser,
                null,
                List.of(new SimpleGrantedAuthority("ROLE_STUDENT"))
        );
    }
}
