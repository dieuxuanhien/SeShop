package com.seshop.identity.api;

import jakarta.validation.constraints.NotNull;

public record AssignRoleRequest(
        @NotNull Long roleId
) {
}
