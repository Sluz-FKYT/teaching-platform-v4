package com.opencode.teachingplatform.clazz;

import com.opencode.teachingplatform.auth.security.CurrentUser;
import com.opencode.teachingplatform.auth.security.JwtAuthenticationFilter;
import com.opencode.teachingplatform.auth.security.SecurityConfig;
import com.opencode.teachingplatform.clazz.controller.ClassController;
import com.opencode.teachingplatform.clazz.dto.ClassRequests.CreateClassRequest;
import com.opencode.teachingplatform.clazz.service.ClassService;
import com.opencode.teachingplatform.common.api.PagedResult;
import com.opencode.teachingplatform.common.enums.UserRole;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ClassController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(SecurityConfig.class)
class ClassControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ClassService classService;

    @MockBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Test
    void teacherCanCreateOwnClass() throws Exception {
        UsernamePasswordAuthenticationToken authenticationToken = authenticateTeacher(1L, "t9001");

        when(classService.createClass(any(CurrentUser.class), any(CreateClassRequest.class))).thenReturn(java.util.Map.of(
                "id", 10,
                "name", "软件测试 2026-2 班",
                "code", "SE2026-2",
                "teacherUserId", 1,
                "status", "ACTIVE",
                "studentCount", 0
        ));

        SecurityContextHolder.getContext().setAuthentication(authenticationToken);
        try {
            mockMvc.perform(post("/api/v1/classes")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {
                                      "name": "软件测试 2026-2 班",
                                      "code": "SE2026-2"
                                    }
                                    """))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(0))
                    .andExpect(jsonPath("$.message").value("OK"))
                    .andExpect(jsonPath("$.timestamp").isNotEmpty())
                    .andExpect(jsonPath("$.data.id").value(10))
                    .andExpect(jsonPath("$.data.code").value("SE2026-2"))
                    .andExpect(jsonPath("$.data.teacherUserId").value(1));
        } finally {
            SecurityContextHolder.clearContext();
        }
    }

    @Test
    void teacherCannotReadOtherTeachersClass() throws Exception {
        UsernamePasswordAuthenticationToken authenticationToken = authenticateTeacher(1L, "t9001");
        when(classService.getClass(any(CurrentUser.class), any(Long.class)))
                .thenThrow(new com.opencode.teachingplatform.common.exception.BusinessException(40300, "无权限访问该班级"));

        SecurityContextHolder.getContext().setAuthentication(authenticationToken);
        try {
            mockMvc.perform(get("/api/v1/classes/{id}", 2L))
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.code").isNotEmpty())
                    .andExpect(jsonPath("$.message").isNotEmpty())
                    .andExpect(jsonPath("$.timestamp").isNotEmpty());
        } finally {
            SecurityContextHolder.clearContext();
        }
    }

    @Test
    void teacherCanListOwnClasses() throws Exception {
        UsernamePasswordAuthenticationToken authenticationToken = authenticateTeacher(1L, "t9001");
        when(classService.listTeacherClasses(any(CurrentUser.class))).thenReturn(new PagedResult<>(List.of(
                java.util.Map.of(
                        "id", 1,
                        "name", "软件工程 2026-1 班",
                        "code", "SE2026-1",
                        "teacherUserId", 1,
                        "status", "ACTIVE",
                        "studentCount", 1
                )
        ), 1, 10, 1));

        SecurityContextHolder.getContext().setAuthentication(authenticationToken);
        try {
            mockMvc.perform(get("/api/v1/classes"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(0))
                    .andExpect(jsonPath("$.data.items[0].code").value("SE2026-1"))
                    .andExpect(jsonPath("$.data.items[0].studentCount").value(1))
                    .andExpect(jsonPath("$.data.total").value(1));
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
}
