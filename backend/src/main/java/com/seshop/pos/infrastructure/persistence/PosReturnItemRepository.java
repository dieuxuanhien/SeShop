package com.seshop.pos.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface PosReturnItemRepository extends JpaRepository<PosReturnItemEntity, Long> {

    @Query("""
            SELECT COALESCE(SUM(i.qty), 0)
            FROM PosReturnItemEntity i
            WHERE i.posReturn.originalReceiptId = :receiptId
              AND i.variantId = :variantId
            """)
    Long sumReturnedQtyByReceiptIdAndVariantId(
            @Param("receiptId") Long receiptId,
            @Param("variantId") Long variantId);
}
