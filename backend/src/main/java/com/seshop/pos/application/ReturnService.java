package com.seshop.pos.application;

import com.seshop.pos.api.dto.ProcessReturnRequest;
import com.seshop.pos.api.dto.ReturnDto;
import com.seshop.pos.infrastructure.persistence.PosReturnEntity;
import com.seshop.pos.infrastructure.persistence.PosReturnRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class ReturnService {

    private final PosReturnRepository returnRepository;

    public ReturnService(PosReturnRepository returnRepository) {
        this.returnRepository = returnRepository;
    }

    public ReturnDto processReturn(ProcessReturnRequest request, Long staffId) {
        // Typically, we would validate that the original order exists and its items are returnable
        // and update inventory balances accordingly. This is a simplified mock process.

        PosReturnEntity posReturn = new PosReturnEntity();
        posReturn.setOriginalOrderId(request.getOriginalOrderId());
        posReturn.setRefundAmount(request.getRefundAmount());
        posReturn.setReason(request.getReason());
        posReturn.setProcessedBy(staffId);

        PosReturnEntity saved = returnRepository.save(posReturn);
        return mapToDto(saved);
    }

    private ReturnDto mapToDto(PosReturnEntity entity) {
        ReturnDto dto = new ReturnDto();
        dto.setId(entity.getId());
        dto.setOriginalOrderId(entity.getOriginalOrderId());
        dto.setProcessedBy(entity.getProcessedBy());
        dto.setRefundAmount(entity.getRefundAmount());
        dto.setReason(entity.getReason());
        dto.setProcessedAt(entity.getProcessedAt());
        return dto;
    }
}
