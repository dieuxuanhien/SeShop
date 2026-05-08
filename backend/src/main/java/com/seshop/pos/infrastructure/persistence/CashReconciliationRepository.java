package com.seshop.pos.infrastructure.persistence;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CashReconciliationRepository extends JpaRepository<CashReconciliationEntity, Long> {
    Optional<CashReconciliationEntity> findByShift_Id(Long shiftId);
}
