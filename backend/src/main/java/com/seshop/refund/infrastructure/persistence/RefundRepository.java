package com.seshop.refund.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RefundRepository extends JpaRepository<RefundEntity, Long> {
    boolean existsByReturnRequestId(Long returnRequestId);
    List<RefundEntity> findByOrderId(Long orderId);
}
