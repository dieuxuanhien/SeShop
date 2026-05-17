package com.seshop.refund.infrastructure.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.OffsetDateTime;

@Entity
@Table(name = "return_items")
public class ReturnItemEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "return_request_id", nullable = false)
    private ReturnRequestEntity returnRequest;

    @Column(name = "order_item_id", nullable = false)
    private Long orderItemId;

    @Column(nullable = false)
    private Integer qty;

    @Column(length = 20)
    private String disposition;

    @Column(name = "inspected_by")
    private Long inspectedBy;

    @Column(name = "inspected_at")
    private OffsetDateTime inspectedAt;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public ReturnRequestEntity getReturnRequest() { return returnRequest; }
    public void setReturnRequest(ReturnRequestEntity returnRequest) { this.returnRequest = returnRequest; }

    public Long getOrderItemId() { return orderItemId; }
    public void setOrderItemId(Long orderItemId) { this.orderItemId = orderItemId; }

    public Integer getQty() { return qty; }
    public void setQty(Integer qty) { this.qty = qty; }

    public String getDisposition() { return disposition; }
    public void setDisposition(String disposition) { this.disposition = disposition; }

    public Long getInspectedBy() { return inspectedBy; }
    public void setInspectedBy(Long inspectedBy) { this.inspectedBy = inspectedBy; }

    public OffsetDateTime getInspectedAt() { return inspectedAt; }
    public void setInspectedAt(OffsetDateTime inspectedAt) { this.inspectedAt = inspectedAt; }
}
