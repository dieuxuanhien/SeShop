package com.seshop.pos.api.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import java.math.BigDecimal;

public class OpenShiftRequest {

    @NotNull(message = "Location ID is required")
    private Long locationId;

    @NotNull(message = "Starting cash is required")
    @PositiveOrZero(message = "Starting cash cannot be negative")
    private BigDecimal startingCash;

    // Getters and Setters
    public Long getLocationId() { return locationId; }
    public void setLocationId(Long locationId) { this.locationId = locationId; }

    public BigDecimal getStartingCash() { return startingCash; }
    public void setStartingCash(BigDecimal startingCash) { this.startingCash = startingCash; }
}
