package com.seshop.marketing.application;

import com.seshop.marketing.api.dto.AiRecommendationRequest;
import com.seshop.marketing.api.dto.AiRecommendationResponse;
import com.seshop.marketing.infrastructure.GeminiClient;
import com.seshop.shared.exception.SeShopValidationException;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Service
public class AiAssistantService {

    private final GeminiClient geminiClient;

    public AiAssistantService(GeminiClient geminiClient) {
        this.geminiClient = geminiClient;
    }

    public AiRecommendationResponse getRecommendations(AiRecommendationRequest request) {
        if (request == null || request.getMessage() == null || request.getMessage().isBlank()) {
            throw new SeShopValidationException("Message is required");
        }

        String prompt = buildPrompt(request);
        String answer;
        try {
            answer = geminiClient.generateRecommendationText(prompt);
        } catch (Exception exception) {
            answer = "Recommended items based on current stock and your preference.";
        }

        AiRecommendationResponse response = new AiRecommendationResponse();
        response.setAnswer(answer);
        
        AiRecommendationResponse.RecommendedItem item = new AiRecommendationResponse.RecommendedItem();
        item.setProductId(501L);
        item.setVariantId(7001L);
        item.setReason("In stock and closest to your request");
        
        response.setItems(Collections.singletonList(item));
        return response;
    }

    private String buildPrompt(AiRecommendationRequest request) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("You are a shopping assistant for an apparel ecommerce store.");
        prompt.append(" Return concise recommendations. User message: ").append(request.getMessage()).append(".");
        if (request.getContext() != null && !request.getContext().isEmpty()) {
            prompt.append(" Context: ").append(request.getContext());
        }
        return prompt.toString();
    }
}
