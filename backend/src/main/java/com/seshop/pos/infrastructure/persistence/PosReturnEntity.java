package com.seshop.pos.infrastructure.persistence;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "pos_returns")
public class PosReturnEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "original_order_id")
    private Long originalOrderId;

    @Column(name = "original_receipt_id", nullable = false)
    private Long originalReceiptId;

    @Column(name = "processed_by", nullable = false)
    private Long processedBy;

    @Column(name = "refund_amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal refundAmount;

    @Column(length = 255)
    private String reason;

    @Column(name = "processed_at", nullable = false, updatable = false)
    private OffsetDateTime processedAt;

    @OneToMany(mappedBy = "posReturn", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PosReturnItemEntity> items = new ArrayList<>();

    @PrePersist
    protected void onProcess() {
        processedAt = OffsetDateTime.now();
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

    public List<PosReturnItemEntity> getItems() { return items; }
    public void setItems(List<PosReturnItemEntity> items) { this.items = items; }
}
