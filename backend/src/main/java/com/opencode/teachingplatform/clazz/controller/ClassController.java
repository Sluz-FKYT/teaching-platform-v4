package com.opencode.teachingplatform.clazz.controller;

import com.opencode.teachingplatform.auth.security.CurrentUser;
import com.opencode.teachingplatform.auth.security.SecurityUtils;
import com.opencode.teachingplatform.clazz.dto.ClassRequests.CreateClassRequest;
import com.opencode.teachingplatform.clazz.dto.ClassRequests.UpdateClassRequest;
import com.opencode.teachingplatform.clazz.service.ClassService;
import com.opencode.teachingplatform.common.api.ApiResponse;
import com.opencode.teachingplatform.common.api.PagedResult;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/classes")
@Validated
public class ClassController {

    private final ClassService classService;

    public ClassController(ClassService classService) {
        this.classService = classService;
    }

    @GetMapping
    @PreAuthorize("hasRole('TEACHER')")
    public ApiResponse<PagedResult<?>> list() {
        CurrentUser currentUser = SecurityUtils.currentUser();
        return ApiResponse.ok(classService.listTeacherClasses(currentUser));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('TEACHER')")
    public ApiResponse<?> get(@PathVariable Long id) {
        CurrentUser currentUser = SecurityUtils.currentUser();
        return ApiResponse.ok(classService.getClass(currentUser, id));
    }

    @PostMapping
    @PreAuthorize("hasRole('TEACHER')")
    public ApiResponse<?> create(@Valid @RequestBody CreateClassRequest request) {
        CurrentUser currentUser = SecurityUtils.currentUser();
        return ApiResponse.ok(classService.createClass(currentUser, request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('TEACHER')")
    public ApiResponse<?> update(@PathVariable Long id, @Valid @RequestBody UpdateClassRequest request) {
        CurrentUser currentUser = SecurityUtils.currentUser();
        return ApiResponse.ok(classService.updateClass(currentUser, id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('TEACHER')")
    public ApiResponse<?> delete(@PathVariable Long id) {
        CurrentUser currentUser = SecurityUtils.currentUser();
        classService.deleteClass(currentUser, id);
        return ApiResponse.ok(Map.of("deleted", true));
    }
}
