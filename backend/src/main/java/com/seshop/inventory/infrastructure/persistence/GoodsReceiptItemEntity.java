package com.seshop.inventory.infrastructure.persistence;

import jakarta.persistence.*;

@Entity
@Table(name = "goods_receipt_items")
public class GoodsReceiptItemEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "goods_receipt_id", nullable = false)
    private GoodsReceiptEntity goodsReceipt;

    @Column(name = "variant_id", nullable = false)
    private Long variantId;

    @Column(name = "received_qty", nullable = false)
    private Integer receivedQty;

    @Column(name = "damaged_qty", nullable = false)
    private Integer damagedQty = 0;

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public GoodsReceiptEntity getGoodsReceipt() { return goodsReceipt; }
    public void setGoodsReceipt(GoodsReceiptEntity goodsReceipt) { this.goodsReceipt = goodsReceipt; }
    public Long getVariantId() { return variantId; }
    public void setVariantId(Long variantId) { this.variantId = variantId; }
    public Integer getReceivedQty() { return receivedQty; }
    public void setReceivedQty(Integer receivedQty) { this.receivedQty = receivedQty; }
    public Integer getDamagedQty() { return damagedQty; }
    public void setDamagedQty(Integer damagedQty) { this.damagedQty = damagedQty; }
}
