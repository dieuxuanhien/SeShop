package com.seshop.shared.security;

import java.util.List;

public record AuthenticatedUser(
        Long userId,
        String username,
        String userType,
        List<String> permissions
) {
    public boolean hasPermission(String permission) {
        return permissions != null && permissions.contains(permission);
    }
}
