package com.seshop.shared.exception;

import org.springframework.http.HttpStatus;

public abstract class SeShopException extends RuntimeException {

    private final String code;
    private final HttpStatus status;

    protected SeShopException(String code, String message, HttpStatus status) {
        super(message);
        this.code = code;
        this.status = status;
    }

    public String code() {
        return code;
    }

    public HttpStatus status() {
        return status;
    }
}
