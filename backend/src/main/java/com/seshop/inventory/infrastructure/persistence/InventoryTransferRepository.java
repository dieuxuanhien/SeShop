package com.seshop.inventory.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.Collection;

@Repository
public interface InventoryTransferRepository extends JpaRepository<InventoryTransferEntity, Long> {
    @Query("SELECT t FROM InventoryTransferEntity t " +
            "WHERE t.sourceLocation.id IN :locationIds OR t.destinationLocation.id IN :locationIds")
    Page<InventoryTransferEntity> findByLocationScope(
            @Param("locationIds") Collection<Long> locationIds,
            Pageable pageable);
}
