package com.seshop.refund.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReturnRequestRepository extends JpaRepository<ReturnRequestEntity, Long> {
    List<ReturnRequestEntity> findByOrderId(Long orderId);
}
