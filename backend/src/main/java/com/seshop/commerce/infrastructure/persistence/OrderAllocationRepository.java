package com.seshop.commerce.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderAllocationRepository extends JpaRepository<OrderAllocationEntity, Long> {

    @Query("SELECT a FROM OrderAllocationEntity a " +
            "JOIN FETCH a.orderItem oi " +
            "JOIN FETCH a.location " +
            "WHERE oi.order.id = :orderId AND a.status = :status")
    List<OrderAllocationEntity> findByOrderIdAndStatus(
            @Param("orderId") Long orderId,
            @Param("status") String status);
}
