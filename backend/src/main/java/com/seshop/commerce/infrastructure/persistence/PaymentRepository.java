package com.seshop.commerce.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PaymentRepository extends JpaRepository<PaymentEntity, Long> {
    List<PaymentEntity> findByOrderId(Long orderId);
    java.util.Optional<PaymentEntity> findByTransactionId(String transactionId);
}
