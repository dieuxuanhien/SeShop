package com.seshop.pos.api.dto;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

public class ReturnDto {

    private Long id;
    private Long originalOrderId;
    private Long originalReceiptId;
    private Long processedBy;
    private BigDecimal refundAmount;
    private String reason;
    private OffsetDateTime processedAt;
    private List<Item> items;

    public static class Item {

        private Long variantId;
        private Integer qty;
        private String disposition;
        private BigDecimal refundAmount;

        public Long getVariantId() { return variantId; }
        public void setVariantId(Long variantId) { this.variantId = variantId; }

        public Integer getQty() { return qty; }
        public void setQty(Integer qty) { this.qty = qty; }

        public String getDisposition() { return disposition; }
        public void setDisposition(String disposition) { this.disposition = disposition; }

        public BigDecimal getRefundAmount() { return refundAmount; }
        public void setRefundAmount(BigDecimal refundAmount) { this.refundAmount = refundAmount; }
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getOriginalOrderId() { return originalOrderId; }
    public void setOriginalOrderId(Long originalOrderId) { this.originalOrderId = originalOrderId; }

    public Long getOriginalReceiptId() { return originalReceiptId; }
    public void setOriginalReceiptId(Long originalReceiptId) { this.originalReceiptId = originalReceiptId; }

    public Long getProcessedBy() { return processedBy; }
    public void setProcessedBy(Long processedBy) { this.processedBy = processedBy; }

    public BigDecimal getRefundAmount() { return refundAmount; }
    public void setRefundAmount(BigDecimal refundAmount) { this.refundAmount = refundAmount; }

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }

    public OffsetDateTime getProcessedAt() { return processedAt; }
    public void setProcessedAt(OffsetDateTime processedAt) { this.processedAt = processedAt; }

    public List<Item> getItems() { return items; }
    public void setItems(List<Item> items) { this.items = items; }
}
