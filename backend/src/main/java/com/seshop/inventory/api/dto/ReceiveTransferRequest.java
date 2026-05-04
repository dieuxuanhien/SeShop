package com.seshop.inventory.api.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import java.util.List;

public class ReceiveTransferRequest {

    @NotEmpty(message = "Received items list cannot be empty")
    private List<ReceivedItemDto> receivedItems;

    public static class ReceivedItemDto {
        @NotNull(message = "Variant ID is required")
        private Long variantId;

        @NotNull(message = "Received quantity is required")
        @PositiveOrZero(message = "Received quantity cannot be negative")
        private Integer receivedQty;

        @NotNull(message = "Damaged quantity is required")
        @PositiveOrZero(message = "Damaged quantity cannot be negative")
        private Integer damagedQty;

        public Long getVariantId() { return variantId; }
        public void setVariantId(Long variantId) { this.variantId = variantId; }
        public Integer getReceivedQty() { return receivedQty; }
        public void setReceivedQty(Integer receivedQty) { this.receivedQty = receivedQty; }
        public Integer getDamagedQty() { return damagedQty; }
        public void setDamagedQty(Integer damagedQty) { this.damagedQty = damagedQty; }
    }

    // Getters and Setters
    public List<ReceivedItemDto> getReceivedItems() { return receivedItems; }
    public void setReceivedItems(List<ReceivedItemDto> receivedItems) { this.receivedItems = receivedItems; }
}
