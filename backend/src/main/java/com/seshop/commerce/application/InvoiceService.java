package com.seshop.commerce.application;

import com.seshop.commerce.api.dto.CreateInvoiceAdjustmentRequest;
import com.seshop.commerce.api.dto.CreateTaxInvoiceRequest;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import org.springframework.stereotype.Service;

@Service
public class InvoiceService {

    private final AtomicLong invoiceIdGenerator = new AtomicLong(1);
    private final Map<Long, InvoiceRecord> invoices = new ConcurrentHashMap<>();

    public Map<String, Object> createTaxInvoice(CreateTaxInvoiceRequest request) {
        long invoiceId = invoiceIdGenerator.getAndIncrement();
        String invoiceNumber = "INV-" + OffsetDateTime.now().toLocalDate() + "-" + invoiceId;

        InvoiceRecord record = new InvoiceRecord(invoiceId, request.getOrderId(), invoiceNumber, BigDecimal.ZERO);
        invoices.put(invoiceId, record);

        return Map.of(
                "invoiceId", invoiceId,
                "invoiceNumber", invoiceNumber,
                "orderId", request.getOrderId(),
                "status", "ISSUED"
        );
    }

    public Map<String, Object> createAdjustment(Long invoiceId, CreateInvoiceAdjustmentRequest request) {
        InvoiceRecord record = invoices.get(invoiceId);
        if (record == null) {
            throw new IllegalArgumentException("Invoice not found");
        }

        BigDecimal newAdjustment = record.adjustmentAmount().add(request.getDeltaAmount());
        InvoiceRecord updated = new InvoiceRecord(record.invoiceId(), record.orderId(), record.invoiceNumber(), newAdjustment);
        invoices.put(invoiceId, updated);

        return Map.of(
                "invoiceId", invoiceId,
                "invoiceNumber", updated.invoiceNumber(),
                "adjustmentAmount", newAdjustment,
                "reason", request.getReason(),
                "status", "ADJUSTED"
        );
    }

    private record InvoiceRecord(Long invoiceId, Long orderId, String invoiceNumber, BigDecimal adjustmentAmount) {
    }
}
