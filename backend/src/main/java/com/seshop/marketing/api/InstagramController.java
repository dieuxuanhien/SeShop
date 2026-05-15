package com.seshop.marketing.api;

import com.seshop.marketing.api.dto.InstagramConnectionDto;
import com.seshop.marketing.api.dto.InstagramDraftDto;
import com.seshop.marketing.api.dto.InstagramPublishResultDto;
import com.seshop.marketing.application.InstagramService;
import com.seshop.shared.api.ApiResponse;
import com.seshop.shared.security.AuthenticatedUser;
import com.seshop.shared.security.PermissionValidator;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/marketing")
public class InstagramController {

    private static final String SOCIAL_COMPOSE = "social.compose";
    private static final String SOCIAL_CONNECT = "social.connect";

    private final InstagramService instagramService;
    private final PermissionValidator permissionValidator;

    public InstagramController(InstagramService instagramService, PermissionValidator permissionValidator) {
        this.instagramService = instagramService;
        this.permissionValidator = permissionValidator;
    }

    @PostMapping("/instagram/connect")
    public ApiResponse<Map<String, String>> connect(@AuthenticationPrincipal AuthenticatedUser user) {
        permissionValidator.require(SOCIAL_CONNECT);
        String authorizationUrl = instagramService.startConnection(user.userId());
        return ApiResponse.success(Map.of("authorizationUrl", authorizationUrl));
    }

    @PostMapping("/instagram/reconnect")
    public ApiResponse<Map<String, String>> reconnect(@AuthenticationPrincipal AuthenticatedUser user) {
        permissionValidator.require(SOCIAL_CONNECT);
        String authorizationUrl = instagramService.startConnection(user.userId());
        return ApiResponse.success(Map.of("authorizationUrl", authorizationUrl));
    }

    @GetMapping("/instagram/status")
    public ApiResponse<InstagramConnectionDto> getStatus(@AuthenticationPrincipal AuthenticatedUser user) {
        permissionValidator.require(SOCIAL_CONNECT);
        return ApiResponse.success(instagramService.getConnectionStatus(user.userId()));
    }

    @PostMapping("/drafts")
    public ApiResponse<InstagramDraftDto> createDraft(@AuthenticationPrincipal AuthenticatedUser user, @RequestBody InstagramDraftDto request) {
        permissionValidator.require(SOCIAL_COMPOSE);
        return ApiResponse.success(instagramService.createDraft(user.userId(), request));
    }

    @GetMapping("/drafts")
    public ApiResponse<List<InstagramDraftDto>> listDrafts() {
        permissionValidator.require(SOCIAL_COMPOSE);
        return ApiResponse.success(instagramService.listDrafts());
    }

    @PutMapping("/drafts/{draftId}")
    public ApiResponse<InstagramDraftDto> updateDraft(@PathVariable long draftId, @RequestBody InstagramDraftDto request) {
        permissionValidator.require(SOCIAL_COMPOSE);
        return ApiResponse.success(instagramService.updateDraft(draftId, request));
    }

    @PostMapping("/drafts/{draftId}/submit-review")
    public ApiResponse<InstagramDraftDto> submitReview(@PathVariable long draftId) {
        permissionValidator.require(SOCIAL_COMPOSE);
        return ApiResponse.success(instagramService.submitReview(draftId));
    }

    @PostMapping("/drafts/{draftId}/approve")
    public ApiResponse<InstagramDraftDto> approveDraft(@PathVariable long draftId) {
        permissionValidator.require(SOCIAL_COMPOSE);
        return ApiResponse.success(instagramService.approveDraft(draftId));
    }

    @PostMapping("/drafts/{draftId}/publish")
    public ApiResponse<InstagramPublishResultDto> publishDraft(@PathVariable long draftId) {
        permissionValidator.require(SOCIAL_COMPOSE);
        return ApiResponse.success(instagramService.publishDraft(draftId));
    }
}
