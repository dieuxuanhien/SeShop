package com.seshop.shared.security;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.Authentication;

class JwtTokenProviderTest {

    @Test
    void tokenCarriesPermissionsIntoAuthentication() {
        JwtTokenProvider provider = new JwtTokenProvider(
                new JwtProperties(
                        "SeShop",
                        "01234567890123456789012345678901",
                        30
                )
        );

        String token = provider.generateToken(
                42L,
                "staff.user",
                "STAFF",
                List.of("inventory.adjust", "order.read")
        );

        assertThat(provider.validate(token)).isTrue();

        Authentication authentication = provider.authentication(token);
        assertThat(authentication.getAuthorities())
                .extracting("authority")
                .containsExactly("inventory.adjust", "order.read");

        assertThat(authentication.getPrincipal()).isInstanceOf(AuthenticatedUser.class);
        AuthenticatedUser principal = (AuthenticatedUser) authentication.getPrincipal();
        assertThat(principal.userId()).isEqualTo(42L);
        assertThat(principal.username()).isEqualTo("staff.user");
        assertThat(principal.userType()).isEqualTo("STAFF");
        assertThat(principal.permissions()).containsExactly("inventory.adjust", "order.read");
    }
}