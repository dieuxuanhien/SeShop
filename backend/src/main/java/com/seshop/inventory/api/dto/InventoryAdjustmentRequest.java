package com.seshop.inventory.api.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class InventoryAdjustmentRequest {

    @NotNull(message = "Variant ID is required")
    private Long variantId;

    @NotNull(message = "Location ID is required")
    private Long locationId;

    @NotNull(message = "Delta quantity is required")
    private Integer deltaQty;

    @Size(max = 50, message = "Reason code must not exceed 50 characters")
    private String reasonCode;

    private String notes;

    // Getters and Setters
    public Long getVariantId() { return variantId; }
    public void setVariantId(Long variantId) { this.variantId = variantId; }

    public Long getLocationId() { return locationId; }
    public void setLocationId(Long locationId) { this.locationId = locationId; }

    public Integer getDeltaQty() { return deltaQty; }
    public void setDeltaQty(Integer deltaQty) { this.deltaQty = deltaQty; }

    public String getReasonCode() { return reasonCode; }
    public void setReasonCode(String reasonCode) { this.reasonCode = reasonCode; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
}
