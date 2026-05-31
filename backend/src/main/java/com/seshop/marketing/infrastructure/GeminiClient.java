package com.seshop.marketing.infrastructure;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.seshop.shared.exception.BusinessException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;

@Component
public class GeminiClient {

    private static final Logger log = LoggerFactory.getLogger(GeminiClient.class);

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

    public GeminiRecommendationResult generateStructuredRecommendation(
            String systemPrompt, List<Map<String, String>> conversationHistory) {
        if (!properties.isEnabled()) {
            throw new BusinessException("GEN_001", "Gemini integration is disabled");
        }
        if (!StringUtils.hasText(properties.getApiKey())) {
            throw new BusinessException("GEN_001", "Gemini API key is not configured");
        }

        Map<String, Object> requestBody = buildStructuredRequestBody(systemPrompt, conversationHistory);

        // First attempt
        try {
            return executeStructuredCall(requestBody);
        } catch (Exception firstException) {
            log.warn("First structured Gemini call failed, retrying in 1 second", firstException);
        }

        // Retry after 1 second
        try {
            Thread.sleep(1000);
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
            throw new BusinessException("GEN_001", "Interrupted during retry wait");
        }

        try {
            return executeStructuredCall(requestBody);
        } catch (Exception retryException) {
            log.error("Retry structured Gemini call also failed", retryException);
            throw new BusinessException("GEN_001", "Gemini structured call failed after retry: " + retryException.getMessage());
        }
    }

    private Map<String, Object> buildStructuredRequestBody(
            String systemPrompt, List<Map<String, String>> conversationHistory) {

        Map<String, Object> body = new LinkedHashMap<>();

        // System instruction
        Map<String, Object> systemInstruction = new LinkedHashMap<>();
        systemInstruction.put("parts", List.of(Map.of("text", systemPrompt)));
        body.put("systemInstruction", systemInstruction);

        // Conversation contents
        List<Map<String, Object>> contents = new ArrayList<>();
        for (Map<String, String> turn : conversationHistory) {
            Map<String, Object> entry = new LinkedHashMap<>();
            entry.put("role", turn.get("role"));
            entry.put("parts", List.of(Map.of("text", turn.get("content"))));
            contents.add(entry);
        }
        body.put("contents", contents);

        // Generation config with structured output schema
        Map<String, Object> generationConfig = new LinkedHashMap<>();
        generationConfig.put("responseMimeType", "application/json");
        generationConfig.put("responseSchema", buildResponseSchema());
        body.put("generationConfig", generationConfig);

        return body;
    }

    private Map<String, Object> buildResponseSchema() {
        // ProductPick object schema
        Map<String, Object> variantIdProp = new LinkedHashMap<>();
        variantIdProp.put("type", "INTEGER");

        Map<String, Object> reasonProp = new LinkedHashMap<>();
        reasonProp.put("type", "STRING");

        Map<String, Object> pickProperties = new LinkedHashMap<>();
        pickProperties.put("variantId", variantIdProp);
        pickProperties.put("reason", reasonProp);

        Map<String, Object> pickObject = new LinkedHashMap<>();
        pickObject.put("type", "OBJECT");
        pickObject.put("properties", pickProperties);
        pickObject.put("required", List.of("variantId", "reason"));

        // Picks array schema
        Map<String, Object> picksArray = new LinkedHashMap<>();
        picksArray.put("type", "ARRAY");
        picksArray.put("items", pickObject);

        // Answer property
        Map<String, Object> answerProp = new LinkedHashMap<>();
        answerProp.put("type", "STRING");

        // Root schema
        Map<String, Object> rootProperties = new LinkedHashMap<>();
        rootProperties.put("answer", answerProp);
        rootProperties.put("picks", picksArray);

        Map<String, Object> schema = new LinkedHashMap<>();
        schema.put("type", "OBJECT");
        schema.put("properties", rootProperties);
        schema.put("required", List.of("answer", "picks"));

        return schema;
    }

    @SuppressWarnings("unchecked")
    private GeminiRecommendationResult executeStructuredCall(Map<String, Object> requestBody) {
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
            String jsonText = (String) parts.get(0).getOrDefault("text", "{}");
            return objectMapper.readValue(jsonText, GeminiRecommendationResult.class);
        } catch (BusinessException exception) {
            throw exception;
        } catch (Exception exception) {
            throw new BusinessException("GEN_001", "Cannot parse Gemini structured response: " + exception.getMessage());
        }
    }
}

