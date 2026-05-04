package com.seshop.pos.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PosTransactionRepository extends JpaRepository<PosTransactionEntity, Long> {
    List<PosTransactionEntity> findByShiftId(Long shiftId);
}
