package com.seshop.inventory.api.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class CreateCycleCountRequest {

    @NotNull(message = "Location ID is required")
    private Long locationId;

    @Size(max = 255, message = "Reason must not exceed 255 characters")
    private String reason;

    // Getters and Setters
    public Long getLocationId() { return locationId; }
    public void setLocationId(Long locationId) { this.locationId = locationId; }

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
}
