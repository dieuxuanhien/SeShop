package com.seshop.marketing.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DiscountCodeRepository extends JpaRepository<DiscountCodeEntity, Long> {
    Optional<DiscountCodeEntity> findByCode(String code);
}
