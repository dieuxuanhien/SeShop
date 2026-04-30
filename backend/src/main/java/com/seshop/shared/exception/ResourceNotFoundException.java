package com.seshop.shared.exception;

import org.springframework.http.HttpStatus;

public class ResourceNotFoundException extends SeShopException {

    public ResourceNotFoundException(String code, String message) {
        super(code, message, HttpStatus.NOT_FOUND);
    }
}
