package com.seshop.pos.application;

import com.seshop.catalog.infrastructure.persistence.ProductVariantEntity;
import com.seshop.catalog.infrastructure.persistence.ProductVariantRepository;
import com.seshop.pos.api.dto.ProcessPosSaleRequest;
import com.seshop.pos.api.dto.ProcessPosSaleResponse;
import com.seshop.pos.api.dto.ReceiptDto;
import com.seshop.pos.infrastructure.persistence.PosReceiptEntity;
import com.seshop.pos.infrastructure.persistence.PosReceiptRepository;
import com.seshop.pos.infrastructure.persistence.PosShiftEntity;
import com.seshop.pos.infrastructure.persistence.PosShiftRepository;
import com.seshop.pos.infrastructure.persistence.PosTransactionEntity;
import com.seshop.pos.infrastructure.persistence.PosTransactionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;

@Service
@Transactional
public class ReceiptService {

    private final PosReceiptRepository receiptRepository;
    private final PosTransactionRepository transactionRepository;
    private final PosShiftRepository shiftRepository;
    private final ProductVariantRepository productVariantRepository;

    public ReceiptService(PosReceiptRepository receiptRepository,
                          PosTransactionRepository transactionRepository,
                          PosShiftRepository shiftRepository,
                          ProductVariantRepository productVariantRepository) {
        this.receiptRepository = receiptRepository;
        this.transactionRepository = transactionRepository;
        this.shiftRepository = shiftRepository;
        this.productVariantRepository = productVariantRepository;
    }

    @Transactional(readOnly = true)
    public ReceiptDto getReceipt(String receiptNumber) {
        PosReceiptEntity receipt = receiptRepository.findByReceiptNumber(receiptNumber)
                .orElseThrow(() -> new IllegalArgumentException("Receipt not found"));
        
        return mapToDto(receipt);
    }

    @Transactional(readOnly = true)
    public ReceiptDto getReceipt(Long receiptId) {
        PosReceiptEntity receipt = receiptRepository.findById(receiptId)
                .orElseThrow(() -> new IllegalArgumentException("Receipt not found"));
        return mapToDto(receipt);
    }

    public ProcessPosSaleResponse createReceipt(ProcessPosSaleRequest request, Long staffId) {
        PosShiftEntity shift = request.getShiftId() != null
                ? shiftRepository.findById(request.getShiftId()).orElseThrow(() -> new IllegalArgumentException("Shift not found"))
                : shiftRepository.findByStaffIdAndStatus(staffId, "OPEN").orElseThrow(() -> new IllegalArgumentException("No active shift found"));

        BigDecimal total = request.getItems().stream()
                .map(item -> {
                    ProductVariantEntity variant = productVariantRepository.findById(item.getVariantId())
                            .orElseThrow(() -> new IllegalArgumentException("Variant not found"));
                    return variant.getPrice().multiply(BigDecimal.valueOf(item.getQty()));
                })
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        if ("CASH".equalsIgnoreCase(request.getPaymentMethod()) && request.getAmountPaid().compareTo(total) < 0) {
            throw new IllegalArgumentException("Amount paid is less than receipt total");
        }

        PosTransactionEntity transaction = new PosTransactionEntity();
        transaction.setShift(shift);
        transaction.setOrderId(0L);
        transaction.setTransactionType(request.getPaymentMethod().toUpperCase());
        transaction.setAmount(total);
        transaction.setStatus("COMPLETED");
        PosTransactionEntity savedTransaction = transactionRepository.save(transaction);

        PosReceiptEntity receipt = new PosReceiptEntity();
        receipt.setTransaction(savedTransaction);
        receipt.setReceiptNumber("POS-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        receipt.setReceiptContent("Receipt total: " + total);
        PosReceiptEntity savedReceipt = receiptRepository.save(receipt);

        ProcessPosSaleResponse response = new ProcessPosSaleResponse();
        response.setReceiptId(savedReceipt.getId());
        response.setReceiptNumber(savedReceipt.getReceiptNumber());
        response.setChangeDue("CASH".equalsIgnoreCase(request.getPaymentMethod())
                ? request.getAmountPaid().subtract(total)
                : BigDecimal.ZERO);
        return response;
    }

    private ReceiptDto mapToDto(PosReceiptEntity entity) {
        ReceiptDto dto = new ReceiptDto();
        dto.setId(entity.getId());
        dto.setTransactionId(entity.getTransaction().getId());
        dto.setReceiptNumber(entity.getReceiptNumber());
        dto.setReceiptContent(entity.getReceiptContent());
        dto.setIssuedAt(entity.getIssuedAt());
        return dto;
    }
}
