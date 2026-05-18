package com.seshop.inventory.infrastructure.persistence;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import jakarta.persistence.LockModeType;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface InventoryBalanceRepository extends JpaRepository<InventoryBalanceEntity, Long> {
    Optional<InventoryBalanceEntity> findByVariantIdAndLocationId(Long variantId, Long locationId);
    List<InventoryBalanceEntity> findByVariantId(Long variantId);
    Page<InventoryBalanceEntity> findByVariantIdOrLocationId(Long variantId, Long locationId, Pageable pageable);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT b FROM InventoryBalanceEntity b WHERE b.variantId = :variantId AND b.location.id = :locationId")
    Optional<InventoryBalanceEntity> findForUpdateByVariantIdAndLocationId(
            @Param("variantId") Long variantId,
            @Param("locationId") Long locationId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT b FROM InventoryBalanceEntity b WHERE b.variantId = :variantId ORDER BY b.id")
    List<InventoryBalanceEntity> findForUpdateByVariantIdOrderById(@Param("variantId") Long variantId);

    @Query("SELECT b FROM InventoryBalanceEntity b " +
            "WHERE (:variantId IS NULL OR b.variantId = :variantId) " +
            "AND (:locationId IS NULL OR b.location.id = :locationId)")
    Page<InventoryBalanceEntity> search(
            @Param("variantId") Long variantId,
            @Param("locationId") Long locationId,
            Pageable pageable);

    @Query("SELECT b FROM InventoryBalanceEntity b " +
            "WHERE (:variantId IS NULL OR b.variantId = :variantId) " +
            "AND (:locationId IS NULL OR b.location.id = :locationId) " +
            "AND b.location.id IN :locationIds")
    Page<InventoryBalanceEntity> searchScoped(
            @Param("variantId") Long variantId,
            @Param("locationId") Long locationId,
            @Param("locationIds") Collection<Long> locationIds,
            Pageable pageable);
}
