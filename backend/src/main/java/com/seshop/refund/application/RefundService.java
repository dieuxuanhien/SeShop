package com.seshop.refund.application;

import com.seshop.audit.application.AuditService;
import com.seshop.audit.domain.AuditAction;
import com.seshop.refund.api.dto.CreateRefundRequest;
import com.seshop.refund.api.dto.CreateReturnRequest;
import com.seshop.refund.api.dto.RefundDto;
import com.seshop.refund.api.dto.ReturnItemRequest;
import com.seshop.refund.api.dto.ReturnDto;
import com.seshop.shared.exception.BusinessException;
import java.time.OffsetDateTime;
import java.util.LinkedHashMap;
import java.util.List;
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
    private final AuditService auditService;

    public RefundService(AuditService auditService) {
        this.auditService = auditService;
    }

    public ReturnDto createReturn(CreateReturnRequest request) {
        ReturnDto dto = new ReturnDto();
        dto.setReturnId(returnIdGenerator.getAndIncrement());
        dto.setOrderId(request.getOrderId());
        dto.setReason(request.getReason());
        dto.setStatus("PENDING");
        dto.setCreatedAt(OffsetDateTime.now());
        returns.put(dto.getReturnId(), dto);
        Map<String, Object> metadata = new LinkedHashMap<>();
        metadata.put("returnId", dto.getReturnId());
        metadata.put("orderId", request.getOrderId());
        metadata.put("reason", request.getReason());
        metadata.put("status", dto.getStatus());
        List<ReturnItemRequest> items = request.getItems() == null ? List.of() : request.getItems();
        metadata.put("items", items.stream().map(this::auditItem).toList());
        auditService.write(AuditAction.RETURN_REQUESTED, "Return", dto.getReturnId().toString(), metadata);
        return dto;
    }

    public ReturnDto approveReturn(Long returnId) {
        ReturnDto dto = returns.get(returnId);
        if (dto == null) {
            throw new BusinessException("REF_001", "Return request not found");
        }
        String previousStatus = dto.getStatus();
        dto.setStatus("APPROVED");
        Map<String, Object> metadata = new LinkedHashMap<>();
        metadata.put("returnId", dto.getReturnId());
        metadata.put("orderId", dto.getOrderId());
        metadata.put("previousStatus", previousStatus);
        metadata.put("status", dto.getStatus());
        metadata.put("reason", dto.getReason());
        auditService.write(AuditAction.RETURN_APPROVED, "Return", dto.getReturnId().toString(), metadata);
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
        Map<String, Object> metadata = new LinkedHashMap<>();
        metadata.put("refundId", dto.getRefundId());
        metadata.put("returnRequestId", request.getReturnRequestId());
        metadata.put("orderId", request.getOrderId());
        metadata.put("paymentId", request.getPaymentId());
        metadata.put("amount", request.getAmount());
        metadata.put("status", dto.getStatus());
        auditService.write(AuditAction.REFUND_PROCESSED, "Refund", dto.getRefundId().toString(), metadata);
        return dto;
    }

    public RefundDto getRefund(Long refundId) {
        RefundDto dto = refunds.get(refundId);
        if (dto == null) {
            throw new BusinessException("REF_001", "Refund not found");
        }
        return dto;
    }

    private Map<String, Object> auditItem(ReturnItemRequest item) {
        Map<String, Object> metadata = new LinkedHashMap<>();
        metadata.put("orderItemId", item.getOrderItemId());
        metadata.put("qty", item.getQty());
        return metadata;
    }
}
