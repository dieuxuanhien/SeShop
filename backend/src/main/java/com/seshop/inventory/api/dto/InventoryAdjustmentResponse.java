package com.seshop.inventory.api.dto;

public class InventoryAdjustmentResponse {

    private Long inventoryBalanceId;
    private Integer onHandQty;
    private Integer reservedQty;
    private Integer availableQty;

    public Long getInventoryBalanceId() {
        return inventoryBalanceId;
    }

    public void setInventoryBalanceId(Long inventoryBalanceId) {
        this.inventoryBalanceId = inventoryBalanceId;
    }

    public Integer getOnHandQty() {
        return onHandQty;
    }

    public void setOnHandQty(Integer onHandQty) {
        this.onHandQty = onHandQty;
    }

    public Integer getReservedQty() {
        return reservedQty;
    }

    public void setReservedQty(Integer reservedQty) {
        this.reservedQty = reservedQty;
    }

    public Integer getAvailableQty() {
        return availableQty;
    }

    public void setAvailableQty(Integer availableQty) {
        this.availableQty = availableQty;
    }
}
