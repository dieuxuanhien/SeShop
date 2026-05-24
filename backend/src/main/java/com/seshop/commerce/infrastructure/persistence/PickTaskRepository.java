package com.seshop.commerce.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PickTaskRepository extends JpaRepository<PickTaskEntity, Long> {
    List<PickTaskEntity> findByAllocationId(Long allocationId);
}
