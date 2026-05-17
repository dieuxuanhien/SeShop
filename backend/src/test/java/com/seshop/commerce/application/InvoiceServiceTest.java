package com.seshop.commerce.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

import com.seshop.audit.application.AuditService;
import com.seshop.audit.domain.AuditAction;
import com.seshop.commerce.api.dto.CreateInvoiceAdjustmentRequest;
import com.seshop.commerce.api.dto.CreateTaxInvoiceRequest;
import com.seshop.commerce.infrastructure.persistence.InvoiceAdjustmentNoteEntity;
import com.seshop.commerce.infrastructure.persistence.InvoiceAdjustmentNoteRepository;
import com.seshop.commerce.infrastructure.persistence.OrderEntity;
import com.seshop.commerce.infrastructure.persistence.OrderRepository;
import com.seshop.commerce.infrastructure.persistence.TaxInvoiceEntity;
import com.seshop.commerce.infrastructure.persistence.TaxInvoiceRepository;
import com.seshop.shared.exception.BusinessException;
import com.seshop.shared.security.AuthenticatedUser;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

@ExtendWith(MockitoExtension.class)
class InvoiceServiceTest {

    @Mock
    private AuditService auditService;

    @Mock
    private TaxInvoiceRepository invoiceRepository;

    @Mock
    private InvoiceAdjustmentNoteRepository adjustmentNoteRepository;

    @Mock
    private OrderRepository orderRepository;

    @AfterEach
    void tearDownSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void createTaxInvoiceWritesIssuedAuditEvent() {
        InvoiceService service = service();
        CreateTaxInvoiceRequest request = new CreateTaxInvoiceRequest();
        request.setOrderId(900L);
        given(orderRepository.findById(900L)).willReturn(Optional.of(order()));
        given(invoiceRepository.findByOrderId(900L)).willReturn(Optional.empty());
        given(invoiceRepository.save(any(TaxInvoiceEntity.class))).willAnswer(invocation -> {
            TaxInvoiceEntity entity = invocation.getArgument(0);
            entity.setId(1L);
            entity.setIssuedAt(OffsetDateTime.now());
            return entity;
        });

        Map<String, Object> response = service.createTaxInvoice(request);

        assertThat(response).containsEntry("invoiceId", 1L)
                .containsEntry("orderId", 900L)
                .containsEntry("subtotalAmount", new BigDecimal("1000000.00"))
                .containsEntry("taxAmount", new BigDecimal("100000.00"))
                .containsEntry("totalAmount", new BigDecimal("1100000.00"))
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
                .containsEntry("customerUserId", 77L)
                .containsEntry("taxRate", new BigDecimal("0.10"))
                .containsEntry("status", "ISSUED");
    }

    @Test
    void createTaxInvoiceRejectsDuplicateOrderInvoice() {
        InvoiceService service = service();
        CreateTaxInvoiceRequest request = new CreateTaxInvoiceRequest();
        request.setOrderId(900L);
        given(orderRepository.findById(900L)).willReturn(Optional.of(order()));
        given(invoiceRepository.findByOrderId(900L)).willReturn(Optional.of(invoice()));

        assertThatThrownBy(() -> service.createTaxInvoice(request))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Tax invoice already exists for this order");
    }

    @Test
    void createAdjustmentWritesAdjustedAuditEvent() {
        authenticate();
        InvoiceService service = service();
        TaxInvoiceEntity invoice = invoice();
        given(invoiceRepository.findById(1L)).willReturn(Optional.of(invoice));
        given(adjustmentNoteRepository.findByOriginalInvoice_Id(1L)).willReturn(List.of());
        given(adjustmentNoteRepository.save(any(InvoiceAdjustmentNoteEntity.class))).willAnswer(invocation -> {
            InvoiceAdjustmentNoteEntity entity = invocation.getArgument(0);
            entity.setId(10L);
            entity.setCreatedAt(OffsetDateTime.now());
            return entity;
        });

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
                .containsEntry("createdBy", 42L)
                .containsEntry("status", "ADJUSTED");
    }

    private InvoiceService service() {
        return new InvoiceService(invoiceRepository, adjustmentNoteRepository, orderRepository, auditService);
    }

    @SuppressWarnings("unchecked")
    private ArgumentCaptor<Map<String, Object>> metadataCaptor() {
        return ArgumentCaptor.forClass(Map.class);
    }

    private OrderEntity order() {
        OrderEntity order = new OrderEntity();
        order.setId(900L);
        order.setCustomerId(77L);
        order.setSubtotalAmount(new BigDecimal("1000000.00"));
        order.setTaxAmount(new BigDecimal("100000.00"));
        order.setTotalAmount(new BigDecimal("1100000.00"));
        return order;
    }

    private TaxInvoiceEntity invoice() {
        TaxInvoiceEntity invoice = new TaxInvoiceEntity();
        invoice.setId(1L);
        invoice.setInvoiceNumber("INV-2026-05-17-900-ABC12345");
        invoice.setOrderId(900L);
        invoice.setCustomerUserId(77L);
        invoice.setSubtotalAmount(new BigDecimal("1000000.00"));
        invoice.setTaxAmount(new BigDecimal("100000.00"));
        invoice.setTotalAmount(new BigDecimal("1100000.00"));
        invoice.setTaxRate(new BigDecimal("0.10"));
        invoice.setIssuedAt(OffsetDateTime.now());
        return invoice;
    }

    private void authenticate() {
        AuthenticatedUser user = new AuthenticatedUser(42L, "finance.user", "STAFF", List.of("invoice.manage"));
        SecurityContextHolder.getContext()
                .setAuthentication(new UsernamePasswordAuthenticationToken(user, null, List.of()));
    }
}
