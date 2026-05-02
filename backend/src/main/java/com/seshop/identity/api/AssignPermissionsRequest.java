package com.seshop.identity.api;

import jakarta.validation.constraints.NotEmpty;
import java.util.List;

public record AssignPermissionsRequest(
        @NotEmpty List<String> permissionCodes
) {
}
