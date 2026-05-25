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
        private String productName;
        private String skuCode;
        private java.util.Map<String, String> attributes;
        private java.math.BigDecimal price;
        private String imageUrl;
        private String description;
        private Integer stockAvailable;
        private String reason;

        public Long getProductId() { return productId; }
        public void setProductId(Long productId) { this.productId = productId; }
        public Long getVariantId() { return variantId; }
        public void setVariantId(Long variantId) { this.variantId = variantId; }
        public String getProductName() { return productName; }
        public void setProductName(String productName) { this.productName = productName; }
        public String getSkuCode() { return skuCode; }
        public void setSkuCode(String skuCode) { this.skuCode = skuCode; }
        public java.util.Map<String, String> getAttributes() { return attributes; }
        public void setAttributes(java.util.Map<String, String> attributes) { this.attributes = attributes; }
        public java.math.BigDecimal getPrice() { return price; }
        public void setPrice(java.math.BigDecimal price) { this.price = price; }
        public String getImageUrl() { return imageUrl; }
        public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        public Integer getStockAvailable() { return stockAvailable; }
        public void setStockAvailable(Integer stockAvailable) { this.stockAvailable = stockAvailable; }
        public String getReason() { return reason; }
        public void setReason(String reason) { this.reason = reason; }
    }
}
