package com.seshop.shared.exception;

import org.springframework.http.HttpStatus;

public class ForbiddenOperationException extends SeShopException {

    public ForbiddenOperationException(String message) {
        super("AUTH_002", message, HttpStatus.FORBIDDEN);
    }
}
