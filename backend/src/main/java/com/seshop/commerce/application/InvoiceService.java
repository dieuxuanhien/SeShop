package com.seshop.commerce.application;

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
import com.seshop.shared.exception.ResourceNotFoundException;
import com.seshop.shared.security.AuthenticatedUser;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class InvoiceService {

    private static final BigDecimal DEFAULT_TAX_RATE = new BigDecimal("0.10");

    private final TaxInvoiceRepository invoiceRepository;
    private final InvoiceAdjustmentNoteRepository adjustmentNoteRepository;
    private final OrderRepository orderRepository;
    private final AuditService auditService;

    public InvoiceService(TaxInvoiceRepository invoiceRepository,
                          InvoiceAdjustmentNoteRepository adjustmentNoteRepository,
                          OrderRepository orderRepository,
                          AuditService auditService) {
        this.invoiceRepository = invoiceRepository;
        this.adjustmentNoteRepository = adjustmentNoteRepository;
        this.orderRepository = orderRepository;
        this.auditService = auditService;
    }

    public Map<String, Object> createTaxInvoice(CreateTaxInvoiceRequest request) {
        OrderEntity order = orderRepository.findById(request.getOrderId())
                .orElseThrow(() -> new ResourceNotFoundException("ORD_002", "Order not found"));
        invoiceRepository.findByOrderId(order.getId()).ifPresent(invoice -> {
            throw new BusinessException("INV_002", "Tax invoice already exists for this order");
        });

        TaxInvoiceEntity invoice = new TaxInvoiceEntity();
        invoice.setInvoiceNumber(generateInvoiceNumber(order.getId()));
        invoice.setOrderId(order.getId());
        invoice.setCustomerUserId(order.getCustomerId());
        invoice.setSubtotalAmount(order.getSubtotalAmount());
        invoice.setTaxAmount(order.getTaxAmount());
        invoice.setTotalAmount(order.getTotalAmount());
        invoice.setTaxRate(taxRateFor(order));
        TaxInvoiceEntity savedInvoice = invoiceRepository.save(invoice);

        Map<String, Object> metadata = new LinkedHashMap<>();
        metadata.put("invoiceId", savedInvoice.getId());
        metadata.put("invoiceNumber", savedInvoice.getInvoiceNumber());
        metadata.put("orderId", savedInvoice.getOrderId());
        metadata.put("customerUserId", savedInvoice.getCustomerUserId());
        metadata.put("subtotalAmount", savedInvoice.getSubtotalAmount());
        metadata.put("taxAmount", savedInvoice.getTaxAmount());
        metadata.put("totalAmount", savedInvoice.getTotalAmount());
        metadata.put("taxRate", savedInvoice.getTaxRate());
        metadata.put("status", "ISSUED");
        auditService.write(AuditAction.INVOICE_ISSUED, "TaxInvoice", Long.toString(savedInvoice.getId()), metadata);

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("invoiceId", savedInvoice.getId());
        response.put("invoiceNumber", savedInvoice.getInvoiceNumber());
        response.put("orderId", savedInvoice.getOrderId());
        response.put("subtotalAmount", savedInvoice.getSubtotalAmount());
        response.put("taxAmount", savedInvoice.getTaxAmount());
        response.put("totalAmount", savedInvoice.getTotalAmount());
        response.put("status", "ISSUED");
        return response;
    }

    public Map<String, Object> createAdjustment(Long invoiceId, CreateInvoiceAdjustmentRequest request) {
        TaxInvoiceEntity invoice = invoiceRepository.findById(invoiceId)
                .orElseThrow(() -> new ResourceNotFoundException("INV_001", "Invoice not found"));
        Long actorUserId = currentActorUserId();

        BigDecimal previousAdjustment = adjustmentNoteRepository.findByOriginalInvoice_Id(invoiceId).stream()
                .map(InvoiceAdjustmentNoteEntity::getDeltaAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal newAdjustment = previousAdjustment.add(request.getDeltaAmount());

        InvoiceAdjustmentNoteEntity note = new InvoiceAdjustmentNoteEntity();
        note.setOriginalInvoice(invoice);
        note.setAdjustmentNumber(generateAdjustmentNumber(invoiceId));
        note.setReason(request.getReason());
        note.setDeltaAmount(request.getDeltaAmount());
        note.setCreatedBy(actorUserId);
        InvoiceAdjustmentNoteEntity savedNote = adjustmentNoteRepository.save(note);

        Map<String, Object> metadata = new LinkedHashMap<>();
        metadata.put("invoiceId", invoiceId);
        metadata.put("invoiceNumber", invoice.getInvoiceNumber());
        metadata.put("adjustmentId", savedNote.getId());
        metadata.put("adjustmentNumber", savedNote.getAdjustmentNumber());
        metadata.put("orderId", invoice.getOrderId());
        metadata.put("previousAdjustmentAmount", previousAdjustment);
        metadata.put("deltaAmount", request.getDeltaAmount());
        metadata.put("adjustmentAmount", newAdjustment);
        metadata.put("reason", request.getReason());
        metadata.put("createdBy", actorUserId);
        metadata.put("status", "ADJUSTED");
        auditService.write(AuditAction.INVOICE_ADJUSTED, "TaxInvoice", Long.toString(invoiceId), metadata);

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("invoiceId", invoiceId);
        response.put("invoiceNumber", invoice.getInvoiceNumber());
        response.put("adjustmentId", savedNote.getId());
        response.put("adjustmentNumber", savedNote.getAdjustmentNumber());
        response.put("deltaAmount", request.getDeltaAmount());
        response.put("adjustmentAmount", newAdjustment);
        response.put("reason", request.getReason());
        response.put("status", "ADJUSTED");
        return response;
    }

    private BigDecimal taxRateFor(OrderEntity order) {
        if (order.getSubtotalAmount() == null || order.getSubtotalAmount().signum() == 0 || order.getTaxAmount() == null) {
            return DEFAULT_TAX_RATE;
        }
        return order.getTaxAmount().divide(order.getSubtotalAmount(), 2, RoundingMode.HALF_UP);
    }

    private String generateInvoiceNumber(Long orderId) {
        return "INV-" + LocalDate.now() + "-" + orderId + "-" + shortId();
    }

    private String generateAdjustmentNumber(Long invoiceId) {
        return "ADJ-" + LocalDate.now() + "-" + invoiceId + "-" + shortId();
    }

    private String shortId() {
        return UUID.randomUUID().toString().replace("-", "").substring(0, 8).toUpperCase();
    }

    private Long currentActorUserId() {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof AuthenticatedUser user)) {
            throw new BusinessException("AUTH_001", "Authenticated user is required");
        }
        return user.userId();
    }
}
