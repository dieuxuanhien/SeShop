package com.seshop.marketing.api;

import com.seshop.marketing.api.dto.AiRecommendationRequest;
import com.seshop.marketing.api.dto.AiRecommendationResponse;
import com.seshop.marketing.application.AiAssistantService;
import com.seshop.shared.api.ApiResponse;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/assistant")
public class AiAssistantController {

    private final AiAssistantService aiAssistantService;

    public AiAssistantController(AiAssistantService aiAssistantService) {
        this.aiAssistantService = aiAssistantService;
    }

    @PostMapping("/recommendations")
    public ApiResponse<AiRecommendationResponse> getRecommendations(@RequestBody AiRecommendationRequest request) {
        return ApiResponse.success(aiAssistantService.getRecommendations(request));
    }
}
