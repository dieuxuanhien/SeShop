package com.seshop.pos.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PosReturnRepository extends JpaRepository<PosReturnEntity, Long> {
}
