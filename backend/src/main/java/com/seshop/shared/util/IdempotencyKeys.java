package com.seshop.shared.util;

public final class IdempotencyKeys {

    private IdempotencyKeys() {
    }

    public static boolean isPresent(String key) {
        return key != null && !key.isBlank();
    }
}
