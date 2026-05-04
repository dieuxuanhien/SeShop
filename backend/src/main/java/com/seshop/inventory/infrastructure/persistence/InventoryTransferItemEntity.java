package com.seshop.inventory.infrastructure.persistence;

import jakarta.persistence.*;

@Entity
@Table(name = "inventory_transfer_items", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"transfer_id", "variant_id"})
})
public class InventoryTransferItemEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "transfer_id", nullable = false)
    private InventoryTransferEntity transfer;

    @Column(name = "variant_id", nullable = false)
    private Long variantId;

    @Column(nullable = false)
    private Integer qty;

    @Column(name = "received_qty")
    private Integer receivedQty;

    @Column(name = "damaged_qty")
    private Integer damagedQty;

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public InventoryTransferEntity getTransfer() { return transfer; }
    public void setTransfer(InventoryTransferEntity transfer) { this.transfer = transfer; }

    public Long getVariantId() { return variantId; }
    public void setVariantId(Long variantId) { this.variantId = variantId; }

    public Integer getQty() { return qty; }
    public void setQty(Integer qty) { this.qty = qty; }

    public Integer getReceivedQty() { return receivedQty; }
    public void setReceivedQty(Integer receivedQty) { this.receivedQty = receivedQty; }

    public Integer getDamagedQty() { return damagedQty; }
    public void setDamagedQty(Integer damagedQty) { this.damagedQty = damagedQty; }
}
