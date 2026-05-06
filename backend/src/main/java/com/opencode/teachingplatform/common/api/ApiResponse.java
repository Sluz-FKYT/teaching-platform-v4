package com.opencode.teachingplatform.common.api;

import java.time.OffsetDateTime;

public record ApiResponse<T>(int code, String message, T data, String timestamp) {

    public static <T> ApiResponse<T> ok(T data) {
        return new ApiResponse<>(0, "OK", data, OffsetDateTime.now().toString());
    }

    public static <T> ApiResponse<T> of(int code, String message, T data) {
        return new ApiResponse<>(code, message, data, OffsetDateTime.now().toString());
    }
}
