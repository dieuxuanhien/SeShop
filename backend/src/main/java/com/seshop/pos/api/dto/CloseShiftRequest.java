package com.seshop.pos.api.dto;

import jakarta.validation.constraints.PositiveOrZero;
import java.math.BigDecimal;

public class CloseShiftRequest {

    @PositiveOrZero(message = "Ending cash cannot be negative")
    private BigDecimal endingCash;

    @PositiveOrZero(message = "Actual cash cannot be negative")
    private BigDecimal actualCash;

    @PositiveOrZero(message = "Expected cash cannot be negative")
    private BigDecimal expectedCash;

    /** Reason is required when the cash variance exceeds the allowed threshold. */
    private String reason;

    /**
     * The user ID of the manager/supervisor approving this shift close.
     * Must be a different user from the cashier who opened the shift.
     * Optional — if omitted the service will enforce approver rules at the business
     * layer.
     */
    private Long approverId;

    // Getters and Setters
    public BigDecimal getEndingCash() {
        return endingCash;
    }

    public void setEndingCash(BigDecimal endingCash) {
        this.endingCash = endingCash;
    }

    public BigDecimal getActualCash() {
        return actualCash;
    }

    public void setActualCash(BigDecimal actualCash) {
        this.actualCash = actualCash;
    }

    public BigDecimal getExpectedCash() {
        return expectedCash;
    }

    public void setExpectedCash(BigDecimal expectedCash) {
        this.expectedCash = expectedCash;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public Long getApproverId() {
        return approverId;
    }

    public void setApproverId(Long approverId) {
        this.approverId = approverId;
    }

    public BigDecimal resolvedEndingCash() {
        return endingCash != null ? endingCash : actualCash;
    }
}
