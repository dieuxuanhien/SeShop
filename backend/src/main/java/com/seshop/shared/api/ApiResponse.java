package com.seshop.shared.api;

public record ApiResponse<T>(T data, ApiMeta meta) {

    public static <T> ApiResponse<T> success(T data, String traceId) {
        return new ApiResponse<>(data, ApiMeta.of(traceId));
    }
}
