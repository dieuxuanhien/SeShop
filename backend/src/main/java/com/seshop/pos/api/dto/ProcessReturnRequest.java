package com.seshop.pos.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import java.math.BigDecimal;

public class ProcessReturnRequest {

    @NotNull(message = "Original order ID is required")
    private Long originalOrderId;

    @NotNull(message = "Refund amount is required")
    @PositiveOrZero(message = "Refund amount cannot be negative")
    private BigDecimal refundAmount;

    @NotBlank(message = "Reason is required")
    private String reason;

    // Getters and Setters
    public Long getOriginalOrderId() { return originalOrderId; }
    public void setOriginalOrderId(Long originalOrderId) { this.originalOrderId = originalOrderId; }

    public BigDecimal getRefundAmount() { return refundAmount; }
    public void setRefundAmount(BigDecimal refundAmount) { this.refundAmount = refundAmount; }

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
}
