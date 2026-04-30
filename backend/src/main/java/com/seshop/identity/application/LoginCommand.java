package com.seshop.identity.application;

public record LoginCommand(String usernameOrEmail, String password) {
}
