package com.seshop.identity.api;

public record PermissionDto(
        Long id,
        String code,
        String description
) {
}
