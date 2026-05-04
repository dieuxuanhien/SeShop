package com.seshop.pos.api.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import java.math.BigDecimal;

public class CloseShiftRequest {

    @NotNull(message = "Ending cash is required")
    @PositiveOrZero(message = "Ending cash cannot be negative")
    private BigDecimal endingCash;

    // Getters and Setters
    public BigDecimal getEndingCash() { return endingCash; }
    public void setEndingCash(BigDecimal endingCash) { this.endingCash = endingCash; }
}
