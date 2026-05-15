package com.seshop.commerce.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.then;

import com.seshop.audit.application.AuditService;
import com.seshop.audit.domain.AuditAction;
import com.seshop.commerce.api.dto.CreateInvoiceAdjustmentRequest;
import com.seshop.commerce.api.dto.CreateTaxInvoiceRequest;
import java.math.BigDecimal;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class InvoiceServiceTest {

    @Mock
    private AuditService auditService;

    @Test
    void createTaxInvoiceWritesIssuedAuditEvent() {
        InvoiceService service = new InvoiceService(auditService);
        CreateTaxInvoiceRequest request = new CreateTaxInvoiceRequest();
        request.setOrderId(900L);

        Map<String, Object> response = service.createTaxInvoice(request);

        assertThat(response).containsEntry("invoiceId", 1L)
                .containsEntry("orderId", 900L)
                .containsEntry("status", "ISSUED");
        ArgumentCaptor<Map<String, Object>> metadataCaptor = metadataCaptor();
        then(auditService).should().write(
                eq(AuditAction.INVOICE_ISSUED),
                eq("TaxInvoice"),
                eq("1"),
                metadataCaptor.capture()
        );
        assertThat(metadataCaptor.getValue())
                .containsEntry("invoiceId", 1L)
                .containsEntry("orderId", 900L)
                .containsEntry("status", "ISSUED");
    }

    @Test
    void createAdjustmentWritesAdjustedAuditEvent() {
        InvoiceService service = new InvoiceService(auditService);
        CreateTaxInvoiceRequest invoiceRequest = new CreateTaxInvoiceRequest();
        invoiceRequest.setOrderId(900L);
        service.createTaxInvoice(invoiceRequest);

        CreateInvoiceAdjustmentRequest adjustmentRequest = new CreateInvoiceAdjustmentRequest();
        adjustmentRequest.setDeltaAmount(new BigDecimal("50000.00"));
        adjustmentRequest.setReason("Tax correction");

        Map<String, Object> response = service.createAdjustment(1L, adjustmentRequest);

        assertThat(response).containsEntry("invoiceId", 1L)
                .containsEntry("adjustmentAmount", new BigDecimal("50000.00"))
                .containsEntry("reason", "Tax correction")
                .containsEntry("status", "ADJUSTED");
        ArgumentCaptor<Map<String, Object>> metadataCaptor = metadataCaptor();
        then(auditService).should().write(
                eq(AuditAction.INVOICE_ADJUSTED),
                eq("TaxInvoice"),
                eq("1"),
                metadataCaptor.capture()
        );
        assertThat(metadataCaptor.getValue())
                .containsEntry("invoiceId", 1L)
                .containsEntry("orderId", 900L)
                .containsEntry("previousAdjustmentAmount", BigDecimal.ZERO)
                .containsEntry("deltaAmount", new BigDecimal("50000.00"))
                .containsEntry("adjustmentAmount", new BigDecimal("50000.00"))
                .containsEntry("reason", "Tax correction")
                .containsEntry("status", "ADJUSTED");
    }

    @SuppressWarnings("unchecked")
    private ArgumentCaptor<Map<String, Object>> metadataCaptor() {
        return ArgumentCaptor.forClass(Map.class);
    }
}
