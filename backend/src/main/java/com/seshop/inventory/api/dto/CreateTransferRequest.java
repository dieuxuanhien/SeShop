package com.seshop.inventory.api.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.util.List;

public class CreateTransferRequest {

    @NotNull(message = "Source location ID is required")
    private Long sourceLocationId;

    @NotNull(message = "Destination location ID is required")
    private Long destinationLocationId;

    @NotEmpty(message = "Items list cannot be empty")
    private List<TransferItemDto> items;

    private String reason;

    public static class TransferItemDto {
        @NotNull(message = "Variant ID is required")
        private Long variantId;

        @NotNull(message = "Quantity is required")
        @Positive(message = "Quantity must be positive")
        private Integer qty;

        public Long getVariantId() { return variantId; }
        public void setVariantId(Long variantId) { this.variantId = variantId; }
        public Integer getQty() { return qty; }
        public void setQty(Integer qty) { this.qty = qty; }
    }

    // Getters and Setters
    public Long getSourceLocationId() { return sourceLocationId; }
    public void setSourceLocationId(Long sourceLocationId) { this.sourceLocationId = sourceLocationId; }

    public Long getDestinationLocationId() { return destinationLocationId; }
    public void setDestinationLocationId(Long destinationLocationId) { this.destinationLocationId = destinationLocationId; }

    public List<TransferItemDto> getItems() { return items; }
    public void setItems(List<TransferItemDto> items) { this.items = items; }

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
}
