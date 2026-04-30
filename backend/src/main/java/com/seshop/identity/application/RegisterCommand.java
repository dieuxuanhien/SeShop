package com.seshop.identity.application;

public record RegisterCommand(
        String username,
        String email,
        String phoneNumber,
        String password
) {
}
