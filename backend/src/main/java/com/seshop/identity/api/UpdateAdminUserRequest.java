package com.seshop.identity.api;

import com.seshop.identity.domain.UserStatus;
import com.seshop.identity.domain.UserType;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record UpdateAdminUserRequest(
        @NotBlank @Size(max = 80) String username,
        @NotBlank @Email @Size(max = 255) String email,
        @NotBlank @Size(max = 20) String phoneNumber,
        @Size(min = 8, max = 128) String password,
        @NotNull UserType userType,
        @NotNull UserStatus status
) {
}
