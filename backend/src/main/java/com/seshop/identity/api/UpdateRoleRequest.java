package com.seshop.identity.api;

import com.seshop.identity.domain.RoleStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record UpdateRoleRequest(
        @NotBlank @Size(max = 80) String name,
        String description,
        @NotNull RoleStatus status
) {
}
