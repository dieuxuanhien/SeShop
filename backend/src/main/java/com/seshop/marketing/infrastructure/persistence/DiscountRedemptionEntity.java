package com.seshop.marketing.infrastructure.persistence;

import jakarta.persistence.*;
import java.time.OffsetDateTime;

@Entity
@Table(name = "discount_redemptions")
public class DiscountRedemptionEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "discount_code_id", nullable = false)
    private DiscountCodeEntity discountCode;

    @Column(name = "order_id", nullable = false)
    private Long orderId;

    @Column(name = "customer_user_id", nullable = false)
    private Long customerUserId;

    @Column(name = "redeemed_at", nullable = false, updatable = false)
    private OffsetDateTime redeemedAt;

    @PrePersist
    protected void onCreate() {
        if (redeemedAt == null) {
            redeemedAt = OffsetDateTime.now();
        }
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public DiscountCodeEntity getDiscountCode() { return discountCode; }
    public void setDiscountCode(DiscountCodeEntity discountCode) { this.discountCode = discountCode; }

    public Long getOrderId() { return orderId; }
    public void setOrderId(Long orderId) { this.orderId = orderId; }

    public Long getCustomerUserId() { return customerUserId; }
    public void setCustomerUserId(Long customerUserId) { this.customerUserId = customerUserId; }

    public OffsetDateTime getRedeemedAt() { return redeemedAt; }
    public void setRedeemedAt(OffsetDateTime redeemedAt) { this.redeemedAt = redeemedAt; }
}
