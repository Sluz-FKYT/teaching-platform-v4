package com.opencode.teachingplatform.student.controller;

import com.opencode.teachingplatform.auth.security.CurrentUser;
import com.opencode.teachingplatform.auth.security.SecurityUtils;
import com.opencode.teachingplatform.common.api.ApiResponse;
import com.opencode.teachingplatform.student.dto.StudentRequests.CreateStudentRequest;
import com.opencode.teachingplatform.student.dto.StudentRequests.ImportStudentsRequest;
import com.opencode.teachingplatform.student.dto.StudentRequests.UpdateStudentRequest;
import com.opencode.teachingplatform.student.service.StudentService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/students")
@Validated
public class StudentController {

    private final StudentService studentService;

    public StudentController(StudentService studentService) {
        this.studentService = studentService;
    }

    @GetMapping
    @PreAuthorize("hasRole('TEACHER')")
    public ApiResponse<?> list(@RequestParam(required = false) Long classId) {
        CurrentUser currentUser = SecurityUtils.currentUser();
        return ApiResponse.ok(studentService.listStudents(currentUser, classId));
    }

    @PostMapping
    @PreAuthorize("hasRole('TEACHER')")
    public ApiResponse<?> create(@Valid @RequestBody CreateStudentRequest request) {
        CurrentUser currentUser = SecurityUtils.currentUser();
        return ApiResponse.ok(studentService.createStudent(currentUser, request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('TEACHER')")
    public ApiResponse<?> update(@PathVariable Long id, @Valid @RequestBody UpdateStudentRequest request) {
        CurrentUser currentUser = SecurityUtils.currentUser();
        return ApiResponse.ok(studentService.updateStudent(currentUser, id, request));
    }

    @PostMapping("/import")
    @PreAuthorize("hasRole('TEACHER')")
    public ApiResponse<?> importStudents(@Valid @RequestBody ImportStudentsRequest request) {
        CurrentUser currentUser = SecurityUtils.currentUser();
        return ApiResponse.ok(studentService.importStudents(currentUser, request));
    }

    @PostMapping("/{id}/status")
    @PreAuthorize("hasRole('TEACHER')")
    public ApiResponse<?> changeStatus(@PathVariable Long id, @RequestBody java.util.Map<String, String> payload) {
        CurrentUser currentUser = SecurityUtils.currentUser();
        return ApiResponse.ok(studentService.changeStatus(currentUser, id, payload.get("status")));
    }
}
