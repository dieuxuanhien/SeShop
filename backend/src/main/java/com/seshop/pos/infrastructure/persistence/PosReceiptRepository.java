package com.seshop.pos.infrastructure.persistence;

import java.math.BigDecimal;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface PosReceiptRepository extends JpaRepository<PosReceiptEntity, Long> {

    List<PosReceiptEntity> findByShift_Id(Long shiftId);

    @Query("""
            SELECT COALESCE(SUM(r.totalAmount), 0)
            FROM PosReceiptEntity r
            WHERE r.shift.id = :shiftId
              AND UPPER(r.paymentMethod) = UPPER(:paymentMethod)
            """)
    BigDecimal sumTotalByShiftIdAndPaymentMethod(
            @Param("shiftId") Long shiftId,
            @Param("paymentMethod") String paymentMethod);
}
