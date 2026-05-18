package com.seshop.identity.api;

import java.time.OffsetDateTime;

public record UserRoleAssignmentDto(
        Long assignmentId,
        Long roleId,
        String roleName,
        OffsetDateTime assignedAt
) {
}
