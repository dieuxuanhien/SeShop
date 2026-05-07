package com.seshop.pos.api.dto;

import jakarta.validation.constraints.PositiveOrZero;
import java.math.BigDecimal;

public class CloseShiftRequest {

    @PositiveOrZero(message = "Ending cash cannot be negative")
    private BigDecimal endingCash;

    @PositiveOrZero(message = "Actual cash cannot be negative")
    private BigDecimal actualCash;
    private String reason;

    // Getters and Setters
    public BigDecimal getEndingCash() { return endingCash; }
    public void setEndingCash(BigDecimal endingCash) { this.endingCash = endingCash; }
    public BigDecimal getActualCash() { return actualCash; }
    public void setActualCash(BigDecimal actualCash) { this.actualCash = actualCash; }
    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }

    public BigDecimal resolvedEndingCash() {
        return endingCash != null ? endingCash : actualCash;
    }
}
