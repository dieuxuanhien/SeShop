package com.seshop.shared.exception;

import java.util.List;

public record ErrorResponse(
        String code,
        String message,
        List<ErrorDetail> details,
        String traceId
) {
    public static ErrorResponse of(String code, String message, List<ErrorDetail> details, String traceId) {
        return new ErrorResponse(code, message, details, traceId);
    }
}
