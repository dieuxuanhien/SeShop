package com.seshop.identity.api;

import java.util.List;

public record UserProfileDto(
        Long id,
        String username,
        String email,
        String phoneNumber,
        String userType,
        List<String> roles,
        List<String> permissions
) {}
