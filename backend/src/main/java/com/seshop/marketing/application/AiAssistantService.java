package com.seshop.marketing.application;

import com.seshop.catalog.infrastructure.persistence.ProductVariantEntity;
import com.seshop.catalog.infrastructure.persistence.ProductVariantRepository;
import com.seshop.commerce.infrastructure.persistence.CartRepository;
import com.seshop.commerce.infrastructure.persistence.OrderRepository;
import com.seshop.marketing.api.dto.AiRecommendationRequest;
import com.seshop.marketing.api.dto.AiRecommendationResponse;
import com.seshop.inventory.infrastructure.persistence.InventoryBalanceRepository;
import com.seshop.marketing.infrastructure.GeminiClient;
import com.seshop.marketing.infrastructure.GeminiRecommendationResult;
import com.seshop.shared.exception.SeShopValidationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class AiAssistantService {

    private static final Logger log = LoggerFactory.getLogger(AiAssistantService.class);
    private static final int MAX_RECOMMENDATIONS = 6;
    private static final int MAX_ORDER_HISTORY = 10;
    private static final int MAX_CONVERSATION_TURNS = 10;

    private final GeminiClient geminiClient;
    private final ProductVariantRepository productVariantRepository;
    private final InventoryBalanceRepository inventoryBalanceRepository;
    private final ProductCatalogBuilder productCatalogBuilder;
    private final OrderRepository orderRepository;
    private final CartRepository cartRepository;
    private final TransactionTemplate transactionTemplate;

    public AiAssistantService(
            GeminiClient geminiClient,
            ProductVariantRepository productVariantRepository,
            InventoryBalanceRepository inventoryBalanceRepository,
            ProductCatalogBuilder productCatalogBuilder,
            OrderRepository orderRepository,
            CartRepository cartRepository,
            TransactionTemplate transactionTemplate) {
        this.geminiClient = geminiClient;
        this.productVariantRepository = productVariantRepository;
        this.inventoryBalanceRepository = inventoryBalanceRepository;
        this.productCatalogBuilder = productCatalogBuilder;
        this.orderRepository = orderRepository;
        this.cartRepository = cartRepository;
        this.transactionTemplate = transactionTemplate;
    }

    public AiRecommendationResponse getRecommendations(AiRecommendationRequest request) {
        if (request == null || request.getMessage() == null || request.getMessage().isBlank()) {
            throw new SeShopValidationException("Message is required");
        }

        // 1. Build the system prompt with catalog + user context
        String systemPrompt = buildSystemPrompt(request);

        // 2. Build conversation history for multi-turn
        List<Map<String, String>> conversationHistory = buildConversationHistory(request);

        // 3. Call Gemini with structured output
        GeminiRecommendationResult result;
        try {
            result = geminiClient.generateStructuredRecommendation(systemPrompt, conversationHistory);
        } catch (Exception e) {
            log.warn("Gemini structured call failed, falling back", e);
            return buildFallbackResponse(request.getMessage());
        }

        // 4. Fetch the recommended products by their variant IDs
        AiRecommendationResponse response = new AiRecommendationResponse();
        response.setAnswer(result.answer());

        if (result.picks() != null && !result.picks().isEmpty()) {
            List<Long> variantIds = result.picks().stream()
                    .map(GeminiRecommendationResult.ProductPick::variantId)
                    .filter(Objects::nonNull)
                    .distinct()
                    .limit(MAX_RECOMMENDATIONS)
                    .collect(Collectors.toList());

            List<AiRecommendationResponse.RecommendedItem> items = transactionTemplate.execute(status -> {
                List<ProductVariantEntity> variants = productVariantRepository.findAllById(variantIds);
                Map<Long, ProductVariantEntity> variantMap = variants.stream()
                        .collect(Collectors.toMap(ProductVariantEntity::getId, v -> v));

                // Build reason map from Gemini picks
                Map<Long, String> reasonMap = result.picks().stream()
                        .filter(p -> p.variantId() != null && p.reason() != null)
                        .collect(Collectors.toMap(
                                GeminiRecommendationResult.ProductPick::variantId,
                                GeminiRecommendationResult.ProductPick::reason,
                                (a, b) -> a));

                return variantIds.stream()
                        .map(variantMap::get)
                        .filter(Objects::nonNull)
                        .map(variant -> buildRecommendedItem(variant,
                                reasonMap.getOrDefault(variant.getId(), "Recommended for you based on your preferences.")))
                        .collect(Collectors.toList());
            });

            response.setItems(items);
        } else {
            response.setItems(Collections.emptyList());
        }

        return response;
    }

    private String buildSystemPrompt(AiRecommendationRequest request) {
        StringBuilder sb = new StringBuilder();
        sb.append("You are SeShop's expert AI styling assistant for an apparel e-commerce store.\n");
        sb.append("Your job is to understand the customer's request and recommend the BEST matching products from our catalog.\n\n");

        sb.append("RULES:\n");
        sb.append("1. ONLY recommend products that exist in the catalog below. Never invent products.\n");
        sb.append("2. Select 1-6 products that BEST match the customer's request.\n");
        sb.append("3. Use the exact variantId (VID) from the catalog. Do NOT make up IDs.\n");
        sb.append("4. Write a helpful, conversational 'answer' explaining your recommendations.\n");
        sb.append("5. For each product pick, write a specific 'reason' explaining why it matches the customer's needs.\n");
        sb.append("6. Consider price, style, occasion, color preferences, and size when matching.\n");
        sb.append("7. If no products match, return an empty picks array and explain in the answer.\n");
        sb.append("8. If the user asks a general fashion question (not product-specific), still try to suggest relevant products.\n\n");

        // Inject catalog
        String catalog = productCatalogBuilder.buildCatalogSummary();
        sb.append("=== PRODUCT CATALOG ===\n");
        sb.append(catalog);
        sb.append("\n=== END CATALOG ===\n\n");

        // Inject user context if available
        if (request.getCustomerId() != null) {
            String userContext = transactionTemplate.execute(status -> buildUserContext(request.getCustomerId()));
            if (userContext != null && !userContext.isEmpty()) {
                sb.append("=== CUSTOMER PROFILE ===\n");
                sb.append(userContext);
                sb.append("\n=== END PROFILE ===\n\n");
            }
        }

        return sb.toString();
    }

    private String buildUserContext(Long customerId) {
        StringBuilder sb = new StringBuilder();

        // Fetch recent order history
        try {
            var orders = orderRepository.findByCustomerId(customerId, PageRequest.of(0, MAX_ORDER_HISTORY));
            if (orders.hasContent()) {
                sb.append("Recent purchases:\n");
                orders.getContent().forEach(order -> {
                    order.getItems().forEach(item -> {
                        sb.append("- ").append(item.getProductName())
                          .append(" (SKU: ").append(item.getSku()).append(")")
                          .append(" qty:").append(item.getQty())
                          .append("\n");
                    });
                });
                sb.append("\n");
            }
        } catch (Exception e) {
            log.debug("Could not fetch order history for customer {}", customerId, e);
        }

        // Fetch current cart
        try {
            var cartOpt = cartRepository.findByCustomerIdAndStatus(customerId, "ACTIVE");
            if (cartOpt.isPresent()) {
                var cart = cartOpt.get();
                if (cart.getItems() != null && !cart.getItems().isEmpty()) {
                    sb.append("Currently in cart:\n");
                    cart.getItems().forEach(item -> {
                        sb.append("- Variant ID: ").append(item.getVariantId())
                          .append(" qty:").append(item.getQty())
                          .append("\n");
                    });
                }
            }
        } catch (Exception e) {
            log.debug("Could not fetch cart for customer {}", customerId, e);
        }

        return sb.toString();
    }

    private List<Map<String, String>> buildConversationHistory(AiRecommendationRequest request) {
        List<Map<String, String>> history = new ArrayList<>();

        // Add previous conversation turns if available
        if (request.getConversationHistory() != null) {
            int startIdx = Math.max(0, request.getConversationHistory().size() - MAX_CONVERSATION_TURNS * 2);
            for (int i = startIdx; i < request.getConversationHistory().size(); i++) {
                var turn = request.getConversationHistory().get(i);
                if (turn.getRole() != null && turn.getContent() != null) {
                    history.add(Map.of("role", turn.getRole(), "content", turn.getContent()));
                }
            }
        }

        // Add current user message
        history.add(Map.of("role", "user", "content", request.getMessage()));

        return history;
    }

    private AiRecommendationResponse buildFallbackResponse(String message) {
        AiRecommendationResponse response = new AiRecommendationResponse();
        response.setAnswer("I'm having trouble processing your request right now. Here are some popular items from our collection that you might like.");

        // Fallback: get a few active products
        try {
            List<AiRecommendationResponse.RecommendedItem> items = productVariantRepository
                    .findActivePublishedWithProductAndImages().stream()
                    .limit(3)
                    .map(variant -> buildRecommendedItem(variant, "One of our popular in-stock items."))
                    .collect(Collectors.toList());
            response.setItems(items);
        } catch (Exception e) {
            log.warn("Fallback product fetch also failed", e);
            response.setItems(Collections.emptyList());
        }

        return response;
    }

    private AiRecommendationResponse.RecommendedItem buildRecommendedItem(ProductVariantEntity variant, String reason) {
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

        item.setReason(reason);
        return item;
    }
}
