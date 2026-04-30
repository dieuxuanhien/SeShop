package com.seshop.shared.exception;

import org.springframework.http.HttpStatus;

public class DuplicateResourceException extends SeShopException {

    public DuplicateResourceException(String code, String message) {
        super(code, message, HttpStatus.CONFLICT);
    }
}
