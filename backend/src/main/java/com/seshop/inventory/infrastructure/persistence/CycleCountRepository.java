package com.seshop.inventory.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CycleCountRepository extends JpaRepository<CycleCountEntity, Long> {
}
