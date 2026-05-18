package com.seshop.identity.api;

import jakarta.validation.constraints.NotNull;
import java.util.List;

public record AssignPermissionsRequest(
        @NotNull List<String> permissionCodes
) {
}
