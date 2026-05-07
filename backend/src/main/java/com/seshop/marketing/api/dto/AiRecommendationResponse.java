package com.seshop.marketing.api.dto;

import java.util.List;

public class AiRecommendationResponse {
    private String answer;
    private List<RecommendedItem> items;

    public String getAnswer() { return answer; }
    public void setAnswer(String answer) { this.answer = answer; }
    public List<RecommendedItem> getItems() { return items; }
    public void setItems(List<RecommendedItem> items) { this.items = items; }

    public static class RecommendedItem {
        private Long productId;
        private Long variantId;
        private String reason;

        public Long getProductId() { return productId; }
        public void setProductId(Long productId) { this.productId = productId; }
        public Long getVariantId() { return variantId; }
        public void setVariantId(Long variantId) { this.variantId = variantId; }
        public String getReason() { return reason; }
        public void setReason(String reason) { this.reason = reason; }
    }
}
