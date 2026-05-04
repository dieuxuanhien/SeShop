package com.seshop.inventory.api.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import java.util.List;

public class CycleCountItemsRequest {

    @NotEmpty(message = "Items list cannot be empty")
    private List<CountedItemDto> items;

    public static class CountedItemDto {
        @NotNull(message = "Variant ID is required")
        private Long variantId;

        @NotNull(message = "Counted quantity is required")
        @PositiveOrZero(message = "Counted quantity cannot be negative")
        private Integer countedQty;

        private String reasonCode;

        public Long getVariantId() { return variantId; }
        public void setVariantId(Long variantId) { this.variantId = variantId; }
        public Integer getCountedQty() { return countedQty; }
        public void setCountedQty(Integer countedQty) { this.countedQty = countedQty; }
        public String getReasonCode() { return reasonCode; }
        public void setReasonCode(String reasonCode) { this.reasonCode = reasonCode; }
    }

    // Getters and Setters
    public List<CountedItemDto> getItems() { return items; }
    public void setItems(List<CountedItemDto> items) { this.items = items; }
}
