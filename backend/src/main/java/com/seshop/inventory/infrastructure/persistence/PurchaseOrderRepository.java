package com.seshop.inventory.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.Collection;
import java.util.Optional;

@Repository
public interface PurchaseOrderRepository extends JpaRepository<PurchaseOrderEntity, Long> {
    Optional<PurchaseOrderEntity> findByPoNumber(String poNumber);
    Page<PurchaseOrderEntity> findByDestinationLocationIdIn(Collection<Long> locationIds, Pageable pageable);
}
