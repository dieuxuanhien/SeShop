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
        private String color;
        private String size;
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
        public String getColor() { return color; }
        public void setColor(String color) { this.color = color; }
        public String getSize() { return size; }
        public void setSize(String size) { this.size = size; }
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
