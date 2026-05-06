package com.opencode.teachingplatform.auth.controller;

import com.opencode.teachingplatform.auth.dto.ChangePasswordRequest;
import com.opencode.teachingplatform.auth.dto.LoginRequest;
import com.opencode.teachingplatform.auth.dto.UpdateProfileRequest;
import com.opencode.teachingplatform.auth.service.AuthService;
import com.opencode.teachingplatform.auth.security.CurrentUser;
import com.opencode.teachingplatform.auth.security.SecurityUtils;
import com.opencode.teachingplatform.common.api.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public ApiResponse<?> login(@Valid @RequestBody LoginRequest request) {
        return ApiResponse.ok(authService.login(request));
    }

    @GetMapping("/me")
    public ApiResponse<?> me() {
        CurrentUser currentUser = SecurityUtils.currentUser();
        return ApiResponse.ok(authService.getCurrentUserProfile(currentUser));
    }

    @PutMapping("/profile")
    public ApiResponse<?> updateProfile(@Valid @RequestBody UpdateProfileRequest request) {
        CurrentUser currentUser = SecurityUtils.currentUser();
        return ApiResponse.ok(authService.updateProfile(currentUser, request));
    }

    @PostMapping("/change-password")
    public ApiResponse<?> changePassword(@Valid @RequestBody ChangePasswordRequest request) {
        CurrentUser currentUser = SecurityUtils.currentUser();
        return ApiResponse.ok(authService.changePassword(currentUser, request));
    }

    @PostMapping("/logout")
    public ApiResponse<?> logout() {
        CurrentUser currentUser = SecurityUtils.currentUser();
        return ApiResponse.ok(authService.logout(currentUser));
    }
}
