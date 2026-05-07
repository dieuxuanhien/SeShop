package com.seshop.shared.api;

import org.slf4j.MDC;

public record ApiResponse<T>(T data, ApiMeta meta) {

    public static <T> ApiResponse<T> success(T data) {
        String traceId = MDC.get(TraceIdFilter.TRACE_ID);
        return new ApiResponse<>(data, ApiMeta.of(traceId));
    }

    public static <T> ApiResponse<T> success(T data, String traceId) {
        return new ApiResponse<>(data, ApiMeta.of(traceId));
    }
}
