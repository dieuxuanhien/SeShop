package com.seshop.identity.application;

import java.util.List;

public record LoginResult(String accessToken, UserSummary user) {

    public record UserSummary(Long id, String username, String userType, List<String> roles, List<String> permissions) {
    }
}
