package com.seshop.marketing.api;

import com.seshop.marketing.api.dto.InstagramConnectionDto;
import com.seshop.marketing.api.dto.InstagramDraftDto;
import com.seshop.marketing.api.dto.InstagramPublishResultDto;
import com.seshop.marketing.application.InstagramService;
import com.seshop.shared.api.ApiResponse;
import com.seshop.shared.security.AuthenticatedUser;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/marketing")
public class InstagramController {

    private final InstagramService instagramService;

    public InstagramController(InstagramService instagramService) {
        this.instagramService = instagramService;
    }

    @PostMapping("/instagram/connect")
    public ApiResponse<Map<String, String>> connect(@AuthenticationPrincipal AuthenticatedUser user) {
        String authorizationUrl = instagramService.startConnection(user.userId());
        return ApiResponse.success(Map.of("authorizationUrl", authorizationUrl));
    }

    @PostMapping("/instagram/reconnect")
    public ApiResponse<Map<String, String>> reconnect(@AuthenticationPrincipal AuthenticatedUser user) {
        String authorizationUrl = instagramService.startConnection(user.userId());
        return ApiResponse.success(Map.of("authorizationUrl", authorizationUrl));
    }

    @GetMapping("/instagram/status")
    public ApiResponse<InstagramConnectionDto> getStatus(@AuthenticationPrincipal AuthenticatedUser user) {
        return ApiResponse.success(instagramService.getConnectionStatus(user.userId()));
    }

    @PostMapping("/drafts")
    public ApiResponse<InstagramDraftDto> createDraft(@AuthenticationPrincipal AuthenticatedUser user, @RequestBody InstagramDraftDto request) {
        return ApiResponse.success(instagramService.createDraft(user.userId(), request));
    }

    @GetMapping("/drafts")
    public ApiResponse<List<InstagramDraftDto>> listDrafts() {
        return ApiResponse.success(instagramService.listDrafts());
    }

    @PutMapping("/drafts/{draftId}")
    public ApiResponse<InstagramDraftDto> updateDraft(@PathVariable long draftId, @RequestBody InstagramDraftDto request) {
        return ApiResponse.success(instagramService.updateDraft(draftId, request));
    }

    @PostMapping("/drafts/{draftId}/submit-review")
    public ApiResponse<InstagramDraftDto> submitReview(@PathVariable long draftId) {
        return ApiResponse.success(instagramService.submitReview(draftId));
    }

    @PostMapping("/drafts/{draftId}/approve")
    public ApiResponse<InstagramDraftDto> approveDraft(@PathVariable long draftId) {
        return ApiResponse.success(instagramService.approveDraft(draftId));
    }

    @PostMapping("/drafts/{draftId}/publish")
    public ApiResponse<InstagramPublishResultDto> publishDraft(@PathVariable long draftId) {
        return ApiResponse.success(instagramService.publishDraft(draftId));
    }
}
