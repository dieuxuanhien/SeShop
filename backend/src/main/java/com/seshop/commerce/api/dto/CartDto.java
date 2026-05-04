package com.seshop.commerce.api.dto;

import java.util.List;

public class CartDto {
    private Long id;
    private Long customerId;
    private String status;
    private List<CartItemDto> items;

    public static class CartItemDto {
        private Long id;
        private Long variantId;
        private Integer qty;

        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public Long getVariantId() { return variantId; }
        public void setVariantId(Long variantId) { this.variantId = variantId; }
        public Integer getQty() { return qty; }
        public void setQty(Integer qty) { this.qty = qty; }
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getCustomerId() { return customerId; }
    public void setCustomerId(Long customerId) { this.customerId = customerId; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public List<CartItemDto> getItems() { return items; }
    public void setItems(List<CartItemDto> items) { this.items = items; }
}
