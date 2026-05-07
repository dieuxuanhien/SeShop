package com.seshop.marketing.infrastructure;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.seshop.shared.exception.BusinessException;
import java.util.List;
import java.util.Map;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;

@Component
public class GeminiClient {

    private final GeminiProperties properties;
    private final ObjectMapper objectMapper;

    public GeminiClient(GeminiProperties properties, ObjectMapper objectMapper) {
        this.properties = properties;
        this.objectMapper = objectMapper;
    }

    public String generateRecommendationText(String prompt) {
        if (!properties.isEnabled()) {
            throw new BusinessException("GEN_001", "Gemini integration is disabled");
        }
        if (!StringUtils.hasText(properties.getApiKey())) {
            throw new BusinessException("GEN_001", "Gemini API key is not configured");
        }

        Map<String, Object> requestBody = Map.of(
                "contents", List.of(
                        Map.of("parts", List.of(Map.of("text", prompt)))
                )
        );

        String response = RestClient.builder()
                .baseUrl(properties.getBaseUrl())
                .build()
                .post()
                .uri(uriBuilder -> uriBuilder
                        .path("/models/{model}:generateContent")
                        .queryParam("key", properties.getApiKey())
                        .build(properties.getModel()))
                .contentType(MediaType.APPLICATION_JSON)
                .body(requestBody)
                .retrieve()
                .body(String.class);

        try {
            Map<String, Object> payload = objectMapper.readValue(response, new TypeReference<>() {});
            List<Map<String, Object>> candidates = (List<Map<String, Object>>) payload.get("candidates");
            if (candidates == null || candidates.isEmpty()) {
                throw new BusinessException("GEN_001", "Gemini returned no candidates");
            }
            Map<String, Object> content = (Map<String, Object>) candidates.get(0).get("content");
            List<Map<String, Object>> parts = (List<Map<String, Object>>) content.get("parts");
            if (parts == null || parts.isEmpty()) {
                throw new BusinessException("GEN_001", "Gemini returned empty content");
            }
            return (String) parts.get(0).getOrDefault("text", "No recommendation available");
        } catch (BusinessException exception) {
            throw exception;
        } catch (Exception exception) {
            throw new BusinessException("GEN_001", "Cannot parse Gemini response");
        }
    }
}
