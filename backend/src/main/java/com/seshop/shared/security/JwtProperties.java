package com.seshop.shared.security;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "seshop.security.jwt")
public record JwtProperties(
        String issuer,
        String secret,
        long expirationMinutes
) {
}
