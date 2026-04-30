package com.seshop.shared.exception;

import com.seshop.shared.api.TraceIdFilter;
import jakarta.validation.ConstraintViolationException;
import java.util.List;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(SeShopException.class)
    public ResponseEntity<ErrorResponse> handleSeShopException(SeShopException exception) {
        return ResponseEntity
                .status(exception.status())
                .body(ErrorResponse.of(exception.code(), exception.getMessage(), List.of(), traceId()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleMethodArgumentNotValid(MethodArgumentNotValidException exception) {
        List<ErrorDetail> details = exception.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(error -> new ErrorDetail(error.getField(), error.getDefaultMessage()))
                .toList();

        return ResponseEntity
                .badRequest()
                .body(ErrorResponse.of("GEN_001", "Invalid request payload", details, traceId()));
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> handleConstraintViolation(ConstraintViolationException exception) {
        List<ErrorDetail> details = exception.getConstraintViolations()
                .stream()
                .map(violation -> new ErrorDetail(violation.getPropertyPath().toString(), violation.getMessage()))
                .toList();

        return ResponseEntity
                .badRequest()
                .body(ErrorResponse.of("GEN_001", "Invalid request parameters", details, traceId()));
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ErrorResponse> handleAuthentication(AuthenticationException exception) {
        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(ErrorResponse.of("AUTH_001", "Authentication required", List.of(), traceId()));
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDenied(AccessDeniedException exception) {
        return ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .body(ErrorResponse.of("AUTH_002", "Insufficient permission", List.of(), traceId()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleUnexpected(Exception exception) {
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ErrorResponse.of("GEN_500", "Unexpected server error", List.of(), traceId()));
    }

    private String traceId() {
        return MDC.get(TraceIdFilter.TRACE_ID);
    }
}
