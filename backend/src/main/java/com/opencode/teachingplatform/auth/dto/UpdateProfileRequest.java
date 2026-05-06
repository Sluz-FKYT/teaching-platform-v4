package com.opencode.teachingplatform.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdateProfileRequest(
        @NotBlank(message = "姓名不能为空")
        @Size(max = 128, message = "姓名长度不能超过128")
        String displayName,

        @Email(message = "邮箱格式不正确")
        @Size(max = 128, message = "邮箱长度不能超过128")
        String email,

        @Size(max = 32, message = "手机号长度不能超过32")
        String phone,

        @Size(max = 255, message = "办公时间长度不能超过255")
        String officeHours,

        @Size(max = 1000, message = "简介长度不能超过1000")
        String bio
) {
}
