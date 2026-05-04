package com.seshop.pos.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PosShiftRepository extends JpaRepository<PosShiftEntity, Long> {
    Optional<PosShiftEntity> findByStaffIdAndStatus(Long staffId, String status);
}
