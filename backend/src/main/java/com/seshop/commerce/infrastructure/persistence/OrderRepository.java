package com.seshop.commerce.infrastructure.persistence;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<OrderEntity, Long> {
    Optional<OrderEntity> findByOrderNumber(String orderNumber);
    Page<OrderEntity> findByCustomerId(Long customerId, Pageable pageable);

    @Query("""
            SELECT DISTINCT o
            FROM OrderEntity o
            JOIN o.items item
            JOIN OrderAllocationEntity allocation ON allocation.orderItem = item
            WHERE allocation.location.id IN :locationIds
            """)
    Page<OrderEntity> findByAllocatedLocationIds(
            @Param("locationIds") Collection<Long> locationIds,
            Pageable pageable);
}
