package com.seshop.inventory.api.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import java.time.OffsetDateTime;
import java.util.List;

public class GoodsReceiptRequest {

    @NotNull(message = "Purchase order ID is required")
    private Long purchaseOrderId;

    @NotNull(message = "Received time is required")
    private OffsetDateTime receivedAt;

    @NotEmpty(message = "Items list cannot be empty")
    private List<GoodsReceiptItemDto> items;

    public static class GoodsReceiptItemDto {
        @NotNull(message = "Variant ID is required")
        private Long variantId;

        @NotNull(message = "Received quantity is required")
        @Positive(message = "Received quantity must be positive")
        private Integer receivedQty;

        @PositiveOrZero(message = "Damaged quantity must be zero or positive")
        private Integer damagedQty;

        public Long getVariantId() { return variantId; }
        public void setVariantId(Long variantId) { this.variantId = variantId; }
        public Integer getReceivedQty() { return receivedQty; }
        public void setReceivedQty(Integer receivedQty) { this.receivedQty = receivedQty; }
        public Integer getDamagedQty() { return damagedQty; }
        public void setDamagedQty(Integer damagedQty) { this.damagedQty = damagedQty; }
    }

    public Long getPurchaseOrderId() { return purchaseOrderId; }
    public void setPurchaseOrderId(Long purchaseOrderId) { this.purchaseOrderId = purchaseOrderId; }
    public OffsetDateTime getReceivedAt() { return receivedAt; }
    public void setReceivedAt(OffsetDateTime receivedAt) { this.receivedAt = receivedAt; }
    public List<GoodsReceiptItemDto> getItems() { return items; }
    public void setItems(List<GoodsReceiptItemDto> items) { this.items = items; }
}
