package com.seshop.inventory.infrastructure.persistence;

import jakarta.persistence.*;

@Entity
@Table(name = "cycle_count_items", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"cycle_count_id", "variant_id"})
})
public class CycleCountItemEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cycle_count_id", nullable = false)
    private CycleCountEntity cycleCount;

    @Column(name = "variant_id", nullable = false)
    private Long variantId;

    @Column(name = "system_qty", nullable = false)
    private Integer systemQty;

    @Column(name = "counted_qty", nullable = false)
    private Integer countedQty;

    @Column(name = "reason_code", length = 50)
    private String reasonCode;

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public CycleCountEntity getCycleCount() { return cycleCount; }
    public void setCycleCount(CycleCountEntity cycleCount) { this.cycleCount = cycleCount; }

    public Long getVariantId() { return variantId; }
    public void setVariantId(Long variantId) { this.variantId = variantId; }

    public Integer getSystemQty() { return systemQty; }
    public void setSystemQty(Integer systemQty) { this.systemQty = systemQty; }

    public Integer getCountedQty() { return countedQty; }
    public void setCountedQty(Integer countedQty) { this.countedQty = countedQty; }

    public String getReasonCode() { return reasonCode; }
    public void setReasonCode(String reasonCode) { this.reasonCode = reasonCode; }
}
