package com.seshop.inventory.api.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import java.util.List;

public class CreatePurchaseOrderRequest {

    @NotNull(message = "Supplier ID is required")
    private Long supplierId;

    @NotNull(message = "Destination location ID is required")
    private Long destinationLocationId;

    @NotEmpty(message = "Items list cannot be empty")
    private List<PurchaseOrderItemDto> items;

    public static class PurchaseOrderItemDto {
        @NotNull(message = "Variant ID is required")
        private Long variantId;

        @NotNull(message = "Ordered quantity is required")
        @Positive(message = "Ordered quantity must be positive")
        private Integer orderedQty;

        @NotNull(message = "Unit cost is required")
        @Positive(message = "Unit cost must be positive")
        private BigDecimal unitCost;

        public Long getVariantId() { return variantId; }
        public void setVariantId(Long variantId) { this.variantId = variantId; }
        public Integer getOrderedQty() { return orderedQty; }
        public void setOrderedQty(Integer orderedQty) { this.orderedQty = orderedQty; }
        public BigDecimal getUnitCost() { return unitCost; }
        public void setUnitCost(BigDecimal unitCost) { this.unitCost = unitCost; }
    }

    public Long getSupplierId() { return supplierId; }
    public void setSupplierId(Long supplierId) { this.supplierId = supplierId; }
    public Long getDestinationLocationId() { return destinationLocationId; }
    public void setDestinationLocationId(Long destinationLocationId) { this.destinationLocationId = destinationLocationId; }
    public List<PurchaseOrderItemDto> getItems() { return items; }
    public void setItems(List<PurchaseOrderItemDto> items) { this.items = items; }
}
