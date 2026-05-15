package com.seshop.shared.security;

import com.seshop.shared.exception.ForbiddenOperationException;
import java.util.Arrays;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
public class PermissionValidator {

    public void require(String permission) {
        if (!hasPermission(permission)) {
            throw new ForbiddenOperationException("Missing permission: " + permission);
        }
    }

    public void requireAny(String... permissions) {
        if (permissions == null || permissions.length == 0 || Arrays.stream(permissions).noneMatch(this::hasPermission)) {
            throw new ForbiddenOperationException("Missing one of permissions: " + String.join(", ", permissions));
        }
    }

    public boolean hasPermission(String permission) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null
                && authentication.getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals(permission));
    }
}
