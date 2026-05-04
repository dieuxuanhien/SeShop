package com.seshop.pos.application;

import com.seshop.pos.api.dto.ReceiptDto;
import com.seshop.pos.infrastructure.persistence.PosReceiptEntity;
import com.seshop.pos.infrastructure.persistence.PosReceiptRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class ReceiptService {

    private final PosReceiptRepository receiptRepository;

    public ReceiptService(PosReceiptRepository receiptRepository) {
        this.receiptRepository = receiptRepository;
    }

    public ReceiptDto getReceipt(String receiptNumber) {
        PosReceiptEntity receipt = receiptRepository.findByReceiptNumber(receiptNumber)
                .orElseThrow(() -> new IllegalArgumentException("Receipt not found"));
        
        return mapToDto(receipt);
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
