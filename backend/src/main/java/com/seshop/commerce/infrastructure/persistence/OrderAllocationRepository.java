package com.seshop.commerce.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Collection;

@Repository
public interface OrderAllocationRepository extends JpaRepository<OrderAllocationEntity, Long> {

    @Query("SELECT a FROM OrderAllocationEntity a " +
            "JOIN FETCH a.orderItem oi " +
            "JOIN FETCH a.location " +
            "WHERE oi.order.id = :orderId AND a.status = :status")
    List<OrderAllocationEntity> findByOrderIdAndStatus(
            @Param("orderId") Long orderId,
            @Param("status") String status);

    @Query("SELECT COUNT(a) > 0 FROM OrderAllocationEntity a " +
            "JOIN a.orderItem oi " +
            "WHERE oi.order.id = :orderId AND a.location.id IN :locationIds")
    boolean existsByOrderIdAndLocationIds(
            @Param("orderId") Long orderId,
            @Param("locationIds") Collection<Long> locationIds);
}
