package com.seshop.pos.application;

import com.seshop.audit.application.AuditService;
import com.seshop.audit.domain.AuditAction;
import com.seshop.inventory.infrastructure.persistence.InventoryBalanceEntity;
import com.seshop.inventory.infrastructure.persistence.InventoryBalanceRepository;
import com.seshop.pos.api.dto.ProcessReturnRequest;
import com.seshop.pos.api.dto.ReturnDto;
import com.seshop.pos.infrastructure.persistence.PosReceiptEntity;
import com.seshop.pos.infrastructure.persistence.PosReceiptItemEntity;
import com.seshop.pos.infrastructure.persistence.PosReceiptRepository;
import com.seshop.pos.infrastructure.persistence.PosReturnEntity;
import com.seshop.pos.infrastructure.persistence.PosReturnItemEntity;
import com.seshop.pos.infrastructure.persistence.PosReturnItemRepository;
import com.seshop.pos.infrastructure.persistence.PosReturnRepository;
import com.seshop.shared.exception.BusinessException;
import com.seshop.shared.exception.ForbiddenOperationException;
import com.seshop.shared.exception.ResourceNotFoundException;
import com.seshop.shared.security.LocationScope;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class ReturnService {

    private static final String DISPOSITION_DISPOSE = "DISPOSE";
    private static final String DISPOSITION_REFURBISH = "REFURBISH";
    private static final String DISPOSITION_RESTOCK = "RESTOCK";

    private final PosReturnRepository returnRepository;
    private final PosReturnItemRepository returnItemRepository;
    private final PosReceiptRepository receiptRepository;
    private final InventoryBalanceRepository balanceRepository;
    private final AuditService auditService;

    public ReturnService(PosReturnRepository returnRepository,
                         PosReturnItemRepository returnItemRepository,
                         PosReceiptRepository receiptRepository,
                         InventoryBalanceRepository balanceRepository,
                         AuditService auditService) {
        this.returnRepository = returnRepository;
        this.returnItemRepository = returnItemRepository;
        this.receiptRepository = receiptRepository;
        this.balanceRepository = balanceRepository;
        this.auditService = auditService;
    }

    public ReturnDto processReturn(ProcessReturnRequest request, Long staffId) {
        return processReturn(request, staffId, LocationScope.all());
    }

    public ReturnDto processReturn(ProcessReturnRequest request, Long staffId, LocationScope locationScope) {
        PosReceiptEntity receipt = receiptRepository.findById(request.getOriginalOrderId())
                .orElseThrow(() -> new ResourceNotFoundException("POS_404", "Original receipt not found"));
        if (receipt.getShift() == null || receipt.getShift().getLocationId() == null) {
            throw new BusinessException("POS_RET_001", "Original receipt has no return location");
        }
        if (!locationScope.allows(receipt.getShift().getLocationId())) {
            throw new ForbiddenOperationException("Missing location access: " + receipt.getShift().getLocationId());
        }

        PosReturnEntity posReturn = new PosReturnEntity();
        posReturn.setOriginalReceiptId(receipt.getId());
        posReturn.setRefundAmount(request.getRefundAmount());
        posReturn.setReason(request.getReason());
        posReturn.setProcessedBy(staffId);

        BigDecimal refundableAmount = BigDecimal.ZERO;
        Map<Long, ReceiptLine> receiptLinesByVariant = receiptLinesByVariant(receipt);
        List<Map<String, Object>> auditedItems = new ArrayList<>();
        Map<Long, Integer> requestedQtyByVariant = new HashMap<>();

        for (ProcessReturnRequest.Item requestedItem : request.getItems()) {
            String disposition = normalizeDisposition(requestedItem.getDisposition());
            Long variantId = requestedItem.getVariantId();
            int returnQty = requestedItem.getQty();
            if (requestedQtyByVariant.putIfAbsent(variantId, returnQty) != null) {
                throw new BusinessException("POS_RET_001", "Duplicate return item variant");
            }

            ReceiptLine receiptLine = receiptLinesByVariant.get(variantId);
            if (receiptLine == null) {
                throw new BusinessException("POS_RET_001", "Return item does not belong to original receipt");
            }

            long previouslyReturnedQty = returnItemRepository
                    .sumReturnedQtyByReceiptIdAndVariantId(receipt.getId(), variantId);
            if (previouslyReturnedQty + returnQty > receiptLine.qty()) {
                throw new BusinessException("POS_RET_001", "Return item quantity exceeds refundable quantity");
            }

            BigDecimal lineRefund = receiptLine.unitPrice()
                    .multiply(BigDecimal.valueOf(returnQty))
                    .setScale(2, RoundingMode.HALF_UP);
            refundableAmount = refundableAmount.add(lineRefund);

            Map<String, Object> auditedItem = new LinkedHashMap<>();
            auditedItem.put("variantId", variantId);
            auditedItem.put("qty", returnQty);
            auditedItem.put("previouslyReturnedQty", previouslyReturnedQty);
            auditedItem.put("disposition", disposition);
            auditedItem.put("lineRefundAmount", lineRefund);

            if (DISPOSITION_RESTOCK.equals(disposition)) {
                restock(receipt, variantId, returnQty, auditedItem);
            }

            PosReturnItemEntity returnItem = new PosReturnItemEntity();
            returnItem.setPosReturn(posReturn);
            returnItem.setVariantId(variantId);
            returnItem.setQty(returnQty);
            returnItem.setDisposition(disposition);
            returnItem.setRefundAmount(lineRefund);
            posReturn.getItems().add(returnItem);
            auditedItems.add(auditedItem);
        }

        if (request.getRefundAmount().compareTo(refundableAmount) != 0) {
            throw new BusinessException("POS_RET_001", "Refund amount must equal returned item value");
        }

        PosReturnEntity saved = returnRepository.save(posReturn);
        Map<String, Object> metadata = new LinkedHashMap<>();
        metadata.put("posReturnId", saved.getId());
        metadata.put("originalReceiptId", receipt.getId());
        metadata.put("processedBy", staffId);
        metadata.put("refundAmount", request.getRefundAmount());
        metadata.put("refundableAmount", refundableAmount);
        metadata.put("reason", request.getReason());
        metadata.put("items", auditedItems);
        auditService.write(AuditAction.POS_RETURN_PROCESSED, "PosReturn", saved.getId().toString(), metadata);
        return mapToDto(saved);
    }

    private ReturnDto mapToDto(PosReturnEntity entity) {
        ReturnDto dto = new ReturnDto();
        dto.setId(entity.getId());
        dto.setOriginalOrderId(entity.getOriginalReceiptId());
        dto.setOriginalReceiptId(entity.getOriginalReceiptId());
        dto.setProcessedBy(entity.getProcessedBy());
        dto.setRefundAmount(entity.getRefundAmount());
        dto.setReason(entity.getReason());
        dto.setProcessedAt(entity.getProcessedAt());
        dto.setItems(entity.getItems().stream().map(this::mapItemToDto).toList());
        return dto;
    }

    private ReturnDto.Item mapItemToDto(PosReturnItemEntity entity) {
        ReturnDto.Item dto = new ReturnDto.Item();
        dto.setVariantId(entity.getVariantId());
        dto.setQty(entity.getQty());
        dto.setDisposition(entity.getDisposition());
        dto.setRefundAmount(entity.getRefundAmount());
        return dto;
    }

    private Map<Long, ReceiptLine> receiptLinesByVariant(PosReceiptEntity receipt) {
        Map<Long, ReceiptLine> byVariant = new HashMap<>();
        for (PosReceiptItemEntity item : receipt.getItems()) {
            ReceiptLine existing = byVariant.get(item.getVariantId());
            if (existing == null) {
                byVariant.put(item.getVariantId(), new ReceiptLine(item.getQty(), item.getUnitPrice()));
            } else if (existing.unitPrice().compareTo(item.getUnitPrice()) == 0) {
                byVariant.put(item.getVariantId(), new ReceiptLine(existing.qty() + item.getQty(), existing.unitPrice()));
            } else {
                throw new BusinessException("POS_RET_001", "Original receipt has mixed prices for the same variant");
            }
        }
        return byVariant;
    }

    private void restock(PosReceiptEntity receipt, Long variantId, int returnQty, Map<String, Object> auditedItem) {
        Long locationId = receipt.getShift().getLocationId();
        InventoryBalanceEntity balance = balanceRepository
                .findForUpdateByVariantIdAndLocationId(variantId, locationId)
                .orElseThrow(() -> new BusinessException("INV_001", "Inventory balance not found for return location"));
        int beforeOnHandQty = balance.getOnHandQty();
        balance.setOnHandQty(balance.getOnHandQty() + returnQty);
        balanceRepository.save(balance);
        auditedItem.put("locationId", locationId);
        auditedItem.put("beforeOnHandQty", beforeOnHandQty);
        auditedItem.put("afterOnHandQty", balance.getOnHandQty());
        auditedItem.put("reservedQty", balance.getReservedQty());
    }

    private String normalizeDisposition(String disposition) {
        String normalized = disposition.trim().toUpperCase(Locale.ROOT);
        if (!DISPOSITION_RESTOCK.equals(normalized)
                && !DISPOSITION_REFURBISH.equals(normalized)
                && !DISPOSITION_DISPOSE.equals(normalized)) {
            throw new BusinessException("POS_RET_001", "Invalid return disposition");
        }
        return normalized;
    }

    private record ReceiptLine(int qty, BigDecimal unitPrice) {
    }
}
