package com.seshop.marketing.api;

import com.seshop.marketing.api.dto.InstagramConnectionDto;
import com.seshop.marketing.application.InstagramService;
import com.seshop.shared.api.ApiResponse;
import com.seshop.shared.exception.SeShopValidationException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/webhooks/instagram")
public class InstagramWebhookController {

    private final InstagramService instagramService;

    public InstagramWebhookController(InstagramService instagramService) {
        this.instagramService = instagramService;
    }

    @GetMapping("/callback")
    public ApiResponse<InstagramConnectionDto> callback(
            @RequestParam("code") String code,
            @RequestParam("state") String state
    ) {
        long userId;
        try {
            userId = Long.parseLong(state);
        } catch (NumberFormatException exception) {
            throw new SeShopValidationException("Invalid OAuth state");
        }
        return ApiResponse.success(instagramService.completeConnection(userId, code));
    }
}
