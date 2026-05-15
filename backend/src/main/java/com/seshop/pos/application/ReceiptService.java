package com.seshop.pos.application;

import com.seshop.audit.application.AuditService;
import com.seshop.audit.domain.AuditAction;
import com.seshop.catalog.infrastructure.persistence.ProductVariantEntity;
import com.seshop.catalog.infrastructure.persistence.ProductVariantRepository;
import com.seshop.inventory.infrastructure.persistence.InventoryBalanceEntity;
import com.seshop.inventory.infrastructure.persistence.InventoryBalanceRepository;
import com.seshop.pos.api.dto.ProcessPosSaleRequest;
import com.seshop.pos.api.dto.ProcessPosSaleResponse;
import com.seshop.pos.api.dto.ReceiptDto;
import com.seshop.pos.infrastructure.persistence.PosReceiptEntity;
import com.seshop.pos.infrastructure.persistence.PosReceiptItemEntity;
import com.seshop.pos.infrastructure.persistence.PosReceiptRepository;
import com.seshop.pos.infrastructure.persistence.PosShiftEntity;
import com.seshop.pos.infrastructure.persistence.PosShiftRepository;
import com.seshop.shared.api.PageResponse;
import com.seshop.shared.exception.BusinessException;
import com.seshop.shared.exception.ForbiddenOperationException;
import com.seshop.shared.exception.ResourceNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Service
@Transactional
public class ReceiptService {

    private final PosReceiptRepository receiptRepository;
    private final PosShiftRepository shiftRepository;
    private final ProductVariantRepository productVariantRepository;
    private final InventoryBalanceRepository balanceRepository;
    private final AuditService auditService;

    public ReceiptService(PosReceiptRepository receiptRepository,
                          PosShiftRepository shiftRepository,
                          ProductVariantRepository productVariantRepository,
                          InventoryBalanceRepository balanceRepository,
                          AuditService auditService) {
        this.receiptRepository = receiptRepository;
        this.shiftRepository = shiftRepository;
        this.productVariantRepository = productVariantRepository;
        this.balanceRepository = balanceRepository;
        this.auditService = auditService;
    }

    @Transactional(readOnly = true)
    public ReceiptDto getReceipt(String receiptNumber) {
        return getReceipt(parseReceiptId(receiptNumber));
    }

    @Transactional(readOnly = true)
    public ReceiptDto getReceipt(Long receiptId) {
        PosReceiptEntity receipt = receiptRepository.findById(receiptId)
                .orElseThrow(() -> new ResourceNotFoundException("POS_404", "Receipt not found"));
        return mapToDto(receipt);
    }

    @Transactional(readOnly = true)
    public PageResponse<ReceiptDto> listReceipts(int page, int size) {
        Page<PosReceiptEntity> receipts = receiptRepository.findAll(PageRequest.of(page, size));
        return new PageResponse<>(
                receipts.getContent().stream().map(this::mapToDto).toList(),
                receipts.getNumber(),
                receipts.getSize(),
                receipts.getTotalElements(),
                receipts.getTotalPages());
    }

    public ProcessPosSaleResponse createReceipt(ProcessPosSaleRequest request, Long staffId) {
        String paymentMethod = normalizePaymentMethod(request.getPaymentMethod());
        PosShiftEntity shift = request.getShiftId() != null
                ? shiftRepository.findById(request.getShiftId())
                        .orElseThrow(() -> new ResourceNotFoundException("POS_002", "Shift not found"))
                : shiftRepository.findByStaffIdAndStatus(staffId, "OPEN")
                        .orElseThrow(() -> new BusinessException("POS_002", "No active shift found"));

        if (!"OPEN".equals(shift.getStatus())) {
            throw new BusinessException("POS_002", "Active shift required");
        }
        if (!shift.getStaffId().equals(staffId)) {
            throw new ForbiddenOperationException("Shift belongs to another staff member");
        }

        PosReceiptEntity receipt = new PosReceiptEntity();
        receipt.setShift(shift);
        receipt.setCustomerUserId(request.getCustomerUserId());
        receipt.setPaymentMethod(paymentMethod);

        BigDecimal total = BigDecimal.ZERO;
        List<Map<String, Object>> auditedItems = new ArrayList<>();
        for (ProcessPosSaleRequest.Item item : request.getItems()) {
            ProductVariantEntity variant = productVariantRepository.findById(item.getVariantId())
                    .orElseThrow(() -> new ResourceNotFoundException("CAT_404", "Variant not found"));
            BigDecimal lineTotal = variant.getPrice().multiply(BigDecimal.valueOf(item.getQty()));
            total = total.add(lineTotal);

            InventoryBalanceEntity balance = balanceRepository
                    .findForUpdateByVariantIdAndLocationId(item.getVariantId(), shift.getLocationId())
                    .orElseThrow(() -> new BusinessException("INV_001", "Insufficient stock at POS location"));
            int availableQty = balance.getOnHandQty() - balance.getReservedQty();
            if (availableQty < item.getQty()) {
                throw new BusinessException("INV_001", "Insufficient stock at POS location");
            }
            int beforeOnHandQty = balance.getOnHandQty();
            balance.setOnHandQty(balance.getOnHandQty() - item.getQty());
            balanceRepository.save(balance);
            auditedItems.add(Map.of(
                    "variantId", item.getVariantId(),
                    "qty", item.getQty(),
                    "unitPrice", variant.getPrice(),
                    "locationId", shift.getLocationId(),
                    "beforeOnHandQty", beforeOnHandQty,
                    "afterOnHandQty", balance.getOnHandQty(),
                    "reservedQty", balance.getReservedQty()
            ));

            PosReceiptItemEntity receiptItem = new PosReceiptItemEntity();
            receiptItem.setReceipt(receipt);
            receiptItem.setVariantId(item.getVariantId());
            receiptItem.setQty(item.getQty());
            receiptItem.setUnitPrice(variant.getPrice());
            receipt.getItems().add(receiptItem);
        }

        if ("CASH".equals(paymentMethod) && request.getAmountPaid().compareTo(total) < 0) {
            throw new BusinessException("POS_003", "Amount paid is less than receipt total");
        }

        receipt.setTotalAmount(total);
        PosReceiptEntity savedReceipt = receiptRepository.save(receipt);
        String receiptNumber = formatReceiptNumber(savedReceipt.getId());

        ProcessPosSaleResponse response = new ProcessPosSaleResponse();
        response.setReceiptId(savedReceipt.getId());
        response.setReceiptNumber(receiptNumber);
        response.setChangeDue("CASH".equals(paymentMethod)
                ? request.getAmountPaid().subtract(total)
                : BigDecimal.ZERO);
        Map<String, Object> metadata = new LinkedHashMap<>();
        metadata.put("receiptNumber", receiptNumber);
        metadata.put("staffId", staffId);
        metadata.put("customerUserId", request.getCustomerUserId());
        metadata.put("shiftId", shift.getId());
        metadata.put("locationId", shift.getLocationId());
        metadata.put("paymentMethod", paymentMethod);
        metadata.put("totalAmount", total);
        metadata.put("amountPaid", request.getAmountPaid());
        metadata.put("changeDue", response.getChangeDue());
        metadata.put("items", auditedItems);
        auditService.write(AuditAction.POS_SALE_COMPLETED, "PosReceipt", savedReceipt.getId().toString(), metadata);
        return response;
    }

    private ReceiptDto mapToDto(PosReceiptEntity entity) {
        ReceiptDto dto = new ReceiptDto();
        dto.setId(entity.getId());
        dto.setReceiptNumber(formatReceiptNumber(entity.getId()));
        dto.setReceiptContent("Receipt total: " + entity.getTotalAmount() + " (" + entity.getPaymentMethod() + ")");
        dto.setIssuedAt(entity.getCreatedAt());
        return dto;
    }

    private String normalizePaymentMethod(String paymentMethod) {
        String normalized = paymentMethod.trim().toUpperCase(Locale.ROOT);
        if (!"CASH".equals(normalized) && !"CARD".equals(normalized)) {
            throw new BusinessException("PAY_002", "Unsupported payment method");
        }
        return normalized;
    }

    private Long parseReceiptId(String receiptNumber) {
        if (receiptNumber == null || receiptNumber.isBlank()) {
            throw new ResourceNotFoundException("POS_404", "Receipt not found");
        }
        String normalized = receiptNumber.trim().toUpperCase(Locale.ROOT);
        if (normalized.startsWith("POS-")) {
            normalized = normalized.substring(4);
        }
        try {
            return Long.parseLong(normalized);
        } catch (NumberFormatException exception) {
            throw new ResourceNotFoundException("POS_404", "Receipt not found");
        }
    }

    private String formatReceiptNumber(Long receiptId) {
        return String.format(Locale.ROOT, "POS-%08d", receiptId);
    }
}
