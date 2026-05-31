package com.seshop.marketing.infrastructure;

import java.util.List;

public record GeminiRecommendationResult(
    String answer,
    List<ProductPick> picks
) {
    public record ProductPick(Long variantId, String reason) {}
}
