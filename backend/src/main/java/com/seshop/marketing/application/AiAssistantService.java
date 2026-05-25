package com.seshop.marketing.application;

import com.seshop.catalog.infrastructure.persistence.ProductVariantEntity;
import com.seshop.catalog.infrastructure.persistence.ProductVariantRepository;
import com.seshop.marketing.api.dto.AiRecommendationRequest;
import com.seshop.marketing.api.dto.AiRecommendationResponse;
import com.seshop.inventory.infrastructure.persistence.InventoryBalanceRepository;
import com.seshop.marketing.infrastructure.GeminiClient;
import com.seshop.shared.exception.SeShopValidationException;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Comparator;

@Service
public class AiAssistantService {

    private final GeminiClient geminiClient;
    private final ProductVariantRepository productVariantRepository;
    private final InventoryBalanceRepository inventoryBalanceRepository;

    public AiAssistantService(
            GeminiClient geminiClient, 
            ProductVariantRepository productVariantRepository,
            InventoryBalanceRepository inventoryBalanceRepository) {
        this.geminiClient = geminiClient;
        this.productVariantRepository = productVariantRepository;
        this.inventoryBalanceRepository = inventoryBalanceRepository;
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

        java.util.List<AiRecommendationResponse.RecommendedItem> items = productVariantRepository.findAll().stream()
                .filter(variant -> "ACTIVE".equals(variant.getStatus()))
                .filter(variant -> inventoryBalanceRepository.findByVariantId(variant.getId()).stream()
                        .mapToInt(b -> b.getOnHandQty() - b.getReservedQty())
                        .sum() > 0)
                .sorted(Comparator.comparing(ProductVariantEntity::getId))
                .limit(3)
                .map(variant -> {
                    AiRecommendationResponse.RecommendedItem item = new AiRecommendationResponse.RecommendedItem();
                    item.setProductId(variant.getProduct().getId());
                    item.setVariantId(variant.getId());
                    item.setProductName(variant.getProduct().getName());
                    item.setSkuCode(variant.getSkuCode());
                    item.setAttributes(variant.getAttributes());
                    item.setPrice(variant.getPrice());
                    item.setDescription(variant.getProduct().getDescription());
                    int stock = inventoryBalanceRepository.findByVariantId(variant.getId()).stream()
                            .mapToInt(b -> b.getOnHandQty() - b.getReservedQty())
                            .sum();
                    item.setStockAvailable(stock);
                    if (variant.getProduct().getImages() != null) {
                        variant.getProduct().getImages().stream()
                                .sorted(Comparator.comparingInt(img -> img.getSortOrder() == null ? 0 : img.getSortOrder()))
                                .findFirst()
                                .ifPresent(img -> item.setImageUrl(img.getUrl()));
                    }
                    item.setReason("Highly recommended based on your styling preferences and current availability.");
                    return item;
                })
                .collect(java.util.stream.Collectors.toList());

        response.setItems(items);
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
