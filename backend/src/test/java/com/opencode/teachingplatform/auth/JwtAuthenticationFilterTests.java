package com.opencode.teachingplatform.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.opencode.teachingplatform.auth.entity.SysUser;
import com.opencode.teachingplatform.auth.entity.UserLoginSession;
import com.opencode.teachingplatform.auth.repository.UserLoginSessionRepository;
import com.opencode.teachingplatform.auth.security.JwtAuthenticationFilter;
import com.opencode.teachingplatform.auth.security.JwtTokenService;
import com.opencode.teachingplatform.common.enums.LoginSessionStatus;
import com.opencode.teachingplatform.common.enums.UserRole;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.time.OffsetDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JwtAuthenticationFilterTests {

    @Mock
    private JwtTokenService jwtTokenService;

    @Mock
    private UserLoginSessionRepository userLoginSessionRepository;

    @Test
    void kickedSessionReturnsSpecificUnauthorizedCode() throws Exception {
        JwtAuthenticationFilter filter = new JwtAuthenticationFilter(jwtTokenService, userLoginSessionRepository, new ObjectMapper());
        Claims claims = mock(Claims.class);
        SysUser user = new SysUser() {
            @Override
            public Long getId() {
                return 1L;
            }
        };
        UserLoginSession loginSession = new UserLoginSession();
        loginSession.setUser(user);
        loginSession.setSessionKey("session-1");
        loginSession.setStatus(LoginSessionStatus.KICKED);
        loginSession.setExpiresAt(OffsetDateTime.now().plusHours(1));

        when(jwtTokenService.parse("jwt-token")).thenReturn(claims);
        when(claims.get("role", String.class)).thenReturn(UserRole.TEACHER.name());
        when(claims.get("uid")).thenReturn(1L);
        when(claims.get("sid", String.class)).thenReturn("session-1");
        when(userLoginSessionRepository.findBySessionKey("session-1")).thenReturn(Optional.of(loginSession));

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer jwt-token");
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain filterChain = mock(FilterChain.class);

        filter.doFilter(request, response, filterChain);

        assertEquals(401, response.getStatus());
        assertEquals(true, response.getContentAsString().contains("40102"));
        assertEquals(true, response.getContentAsString().contains("账号已在其他设备登录"));
        verifyNoInteractions(filterChain);
    }
}
