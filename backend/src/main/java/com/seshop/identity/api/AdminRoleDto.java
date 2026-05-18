package com.seshop.identity.api;

import java.util.List;

public record AdminRoleDto(
        Long id,
        String name,
        String description,
        String status,
        List<String> permissionCodes
) {
}
