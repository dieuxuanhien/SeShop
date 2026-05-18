package com.seshop.identity.api;

import java.util.List;

public record AdminUserDto(
        Long id,
        String username,
        String email,
        String phoneNumber,
        String userType,
        String status,
        List<UserRoleAssignmentDto> roles,
        List<String> permissions
) {
}
