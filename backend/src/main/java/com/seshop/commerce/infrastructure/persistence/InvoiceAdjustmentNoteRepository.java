package com.seshop.commerce.infrastructure.persistence;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface InvoiceAdjustmentNoteRepository extends JpaRepository<InvoiceAdjustmentNoteEntity, Long> {
    List<InvoiceAdjustmentNoteEntity> findByOriginalInvoice_Id(Long invoiceId);
}
