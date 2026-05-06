package com.opencode.teachingplatform.student.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public final class StudentRequests {

    private StudentRequests() {
    }

    public record CreateStudentRequest(
            @NotBlank(message = "用户名不能为空") String username,
            @NotBlank(message = "姓名不能为空") String displayName,
            @NotBlank(message = "密码不能为空") String password,
            @NotNull(message = "班级不能为空") Long classId
    ) {
    }

    public record UpdateStudentRequest(
            @NotBlank(message = "姓名不能为空") String displayName,
            @NotNull(message = "班级不能为空") Long classId
    ) {
    }

    public record ImportStudentRow(
            @NotBlank(message = "用户名不能为空") String username,
            @NotBlank(message = "姓名不能为空") String displayName,
            @NotBlank(message = "密码不能为空") String password
    ) {
    }

    public record ImportStudentsRequest(
            @NotNull(message = "班级不能为空") Long classId,
            @NotEmpty(message = "导入数据不能为空") List<@Valid ImportStudentRow> rows
    ) {
    }
}
