package com.seshop.identity.api;

import com.seshop.identity.application.MeApplicationService;
import com.seshop.shared.api.ApiResponse;
import com.seshop.shared.security.AuthenticatedUser;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth/me")
public class MeController {

    private final MeApplicationService meApplicationService;

    public MeController(MeApplicationService meApplicationService) {
        this.meApplicationService = meApplicationService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<UserProfileDto>> getMyProfile(@AuthenticationPrincipal AuthenticatedUser authenticatedUser) {
        UserProfileDto profile = meApplicationService.getProfile(authenticatedUser.userId());
        return ResponseEntity.ok(ApiResponse.success(profile));
    }

    @PutMapping
    public ResponseEntity<ApiResponse<UserProfileDto>> updateMyProfile(
            @AuthenticationPrincipal AuthenticatedUser authenticatedUser,
            @Valid @RequestBody UpdateProfileRequest request) {
        UserProfileDto profile = meApplicationService.updateProfile(authenticatedUser.userId(), request);
        return ResponseEntity.ok(ApiResponse.success(profile));
    }

    @PutMapping("/password")
    public ResponseEntity<ApiResponse<Void>> updateMyPassword(
            @AuthenticationPrincipal AuthenticatedUser authenticatedUser,
            @Valid @RequestBody UpdatePasswordRequest request) {
        meApplicationService.updatePassword(authenticatedUser.userId(), request);
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}
