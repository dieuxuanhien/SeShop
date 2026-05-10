package com.seshop.shared.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.seshop.shared.exception.ForbiddenOperationException;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

class PermissionValidatorTest {

    private final PermissionValidator validator = new PermissionValidator();

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void requireAllowsGrantedPermission() {
        setAuthenticationWithPermissions(List.of("inventory.adjust", "order.read"));

        assertThatCode(() -> validator.require("inventory.adjust")).doesNotThrowAnyException();
    }

    @Test
    void requireRejectsMissingPermission() {
        setAuthenticationWithPermissions(List.of("order.read"));

        assertThatThrownBy(() -> validator.require("refund.process"))
                .isInstanceOf(ForbiddenOperationException.class)
                .hasMessage("Missing permission: refund.process");
    }

    @Test
    void hasPermissionReturnsFalseWithoutAuthentication() {
        assertThat(validator.hasPermission("audit.read")).isFalse();
    }

    private void setAuthenticationWithPermissions(List<String> permissions) {
        var authentication = new UsernamePasswordAuthenticationToken(
                new AuthenticatedUser(42L, "staff.user", "STAFF", permissions),
                "token",
                permissions.stream().map(SimpleGrantedAuthority::new).toList()
        );
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }
}