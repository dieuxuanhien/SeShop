package com.seshop.shared.domain;

public interface CommandHandler<C, R> {
    R handle(C command);
}
