package com.seshop.pos.api.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import java.util.List;

public class ProcessReturnRequest {

    @NotNull(message = "Original receipt ID is required")
    private Long originalOrderId;

    @NotNull(message = "Refund amount is required")
    @Positive(message = "Refund amount must be positive")
    private BigDecimal refundAmount;

    @NotBlank(message = "Reason is required")
    private String reason;

    @Valid
    @NotEmpty(message = "At least one return item is required")
    private List<Item> items;

    public static class Item {

        @NotNull(message = "Variant ID is required")
        private Long variantId;

        @NotNull(message = "Quantity is required")
        @Positive(message = "Quantity must be positive")
        private Integer qty;

        @NotBlank(message = "Disposition is required")
        private String disposition;

        public Long getVariantId() { return variantId; }
        public void setVariantId(Long variantId) { this.variantId = variantId; }

        public Integer getQty() { return qty; }
        public void setQty(Integer qty) { this.qty = qty; }

        public String getDisposition() { return disposition; }
        public void setDisposition(String disposition) { this.disposition = disposition; }
    }

    // Getters and Setters
    public Long getOriginalOrderId() { return originalOrderId; }
    public void setOriginalOrderId(Long originalOrderId) { this.originalOrderId = originalOrderId; }

    public BigDecimal getRefundAmount() { return refundAmount; }
    public void setRefundAmount(BigDecimal refundAmount) { this.refundAmount = refundAmount; }

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }

    public List<Item> getItems() { return items; }
    public void setItems(List<Item> items) { this.items = items; }
}
