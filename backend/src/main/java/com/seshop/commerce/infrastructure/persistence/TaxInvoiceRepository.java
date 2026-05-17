package com.seshop.commerce.infrastructure.persistence;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TaxInvoiceRepository extends JpaRepository<TaxInvoiceEntity, Long> {
    Optional<TaxInvoiceEntity> findByOrderId(Long orderId);
}
