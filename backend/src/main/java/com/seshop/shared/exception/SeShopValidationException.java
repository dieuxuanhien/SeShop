package com.seshop.shared.exception;

import org.springframework.http.HttpStatus;

public class SeShopValidationException extends SeShopException {

    public SeShopValidationException(String message) {
        super("GEN_001", message, HttpStatus.BAD_REQUEST);
    }
}
