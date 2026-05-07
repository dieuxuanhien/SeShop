package com.seshop.refund.application;

import com.seshop.refund.api.dto.CreateRefundRequest;
import com.seshop.refund.api.dto.CreateReturnRequest;
import com.seshop.refund.api.dto.RefundDto;
import com.seshop.refund.api.dto.ReturnDto;
import com.seshop.shared.exception.BusinessException;
import java.time.OffsetDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import org.springframework.stereotype.Service;

@Service
public class RefundService {

    private final AtomicLong returnIdGenerator = new AtomicLong(1);
    private final AtomicLong refundIdGenerator = new AtomicLong(1);
    private final Map<Long, ReturnDto> returns = new ConcurrentHashMap<>();
    private final Map<Long, RefundDto> refunds = new ConcurrentHashMap<>();

    public ReturnDto createReturn(CreateReturnRequest request) {
        ReturnDto dto = new ReturnDto();
        dto.setReturnId(returnIdGenerator.getAndIncrement());
        dto.setOrderId(request.getOrderId());
        dto.setReason(request.getReason());
        dto.setStatus("PENDING");
        dto.setCreatedAt(OffsetDateTime.now());
        returns.put(dto.getReturnId(), dto);
        return dto;
    }

    public ReturnDto approveReturn(Long returnId) {
        ReturnDto dto = returns.get(returnId);
        if (dto == null) {
            throw new BusinessException("REF_001", "Return request not found");
        }
        dto.setStatus("APPROVED");
        return dto;
    }

    public RefundDto createRefund(CreateRefundRequest request) {
        if (!returns.containsKey(request.getReturnRequestId())) {
            throw new BusinessException("REF_001", "Return request does not exist");
        }

        RefundDto dto = new RefundDto();
        dto.setRefundId(refundIdGenerator.getAndIncrement());
        dto.setOrderId(request.getOrderId());
        dto.setAmount(request.getAmount());
        dto.setStatus("PENDING");
        dto.setCreatedAt(OffsetDateTime.now());
        refunds.put(dto.getRefundId(), dto);
        return dto;
    }

    public RefundDto getRefund(Long refundId) {
        RefundDto dto = refunds.get(refundId);
        if (dto == null) {
            throw new BusinessException("REF_001", "Refund not found");
        }
        return dto;
    }
}
