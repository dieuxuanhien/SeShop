package com.seshop.identity.infrastructure.persistence;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface StaffLocationAssignmentRepository extends JpaRepository<StaffLocationAssignmentEntity, Long> {
    List<StaffLocationAssignmentEntity> findByUserIdAndRevokedAtIsNull(Long userId);

    Optional<StaffLocationAssignmentEntity> findByUserIdAndLocationIdAndRevokedAtIsNull(Long userId, Long locationId);
}
