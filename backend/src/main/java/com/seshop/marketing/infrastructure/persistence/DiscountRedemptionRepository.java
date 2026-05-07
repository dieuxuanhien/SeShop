package com.seshop.marketing.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DiscountRedemptionRepository extends JpaRepository<DiscountRedemptionEntity, Long> {
    Optional<DiscountRedemptionEntity> findByDiscountCodeIdAndOrderId(Long discountCodeId, Long orderId);

    long countByDiscountCodeId(Long discountCodeId);
}
