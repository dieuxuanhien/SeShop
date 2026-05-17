package com.seshop.refund.application;

import com.seshop.audit.application.AuditService;
import com.seshop.audit.domain.AuditAction;
import com.seshop.commerce.infrastructure.persistence.OrderEntity;
import com.seshop.commerce.infrastructure.persistence.OrderItemEntity;
import com.seshop.commerce.infrastructure.persistence.OrderItemRepository;
import com.seshop.commerce.infrastructure.persistence.OrderRepository;
import com.seshop.commerce.infrastructure.persistence.PaymentEntity;
import com.seshop.commerce.infrastructure.persistence.PaymentRepository;
import com.seshop.refund.api.dto.CreateRefundRequest;
import com.seshop.refund.api.dto.CreateReturnRequest;
import com.seshop.refund.api.dto.RefundDto;
import com.seshop.refund.api.dto.ReturnItemRequest;
import com.seshop.refund.api.dto.ReturnDto;
import com.seshop.refund.infrastructure.persistence.RefundEntity;
import com.seshop.refund.infrastructure.persistence.RefundRepository;
import com.seshop.refund.infrastructure.persistence.ReturnItemEntity;
import com.seshop.refund.infrastructure.persistence.ReturnItemRepository;
import com.seshop.refund.infrastructure.persistence.ReturnRequestEntity;
import com.seshop.refund.infrastructure.persistence.ReturnRequestRepository;
import com.seshop.shared.exception.ResourceNotFoundException;
import com.seshop.shared.exception.BusinessException;
import com.seshop.shared.security.AuthenticatedUser;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class RefundService {

    private static final String STATUS_APPROVED = "APPROVED";
    private static final String STATUS_COMPLETED = "COMPLETED";
    private static final String STATUS_DELIVERED = "DELIVERED";
    private static final String STATUS_PAID = "PAID";
    private static final String STATUS_PENDING = "PENDING";

    private final ReturnRequestRepository returnRequestRepository;
    private final ReturnItemRepository returnItemRepository;
    private final RefundRepository refundRepository;
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final PaymentRepository paymentRepository;
    private final AuditService auditService;

    public RefundService(ReturnRequestRepository returnRequestRepository,
                         ReturnItemRepository returnItemRepository,
                         RefundRepository refundRepository,
                         OrderRepository orderRepository,
                         OrderItemRepository orderItemRepository,
                         PaymentRepository paymentRepository,
                         AuditService auditService) {
        this.returnRequestRepository = returnRequestRepository;
        this.returnItemRepository = returnItemRepository;
        this.refundRepository = refundRepository;
        this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository;
        this.paymentRepository = paymentRepository;
        this.auditService = auditService;
    }

    public ReturnDto createReturn(CreateReturnRequest request) {
        OrderEntity order = orderRepository.findById(request.getOrderId())
                .orElseThrow(() -> new ResourceNotFoundException("ORD_002", "Order not found"));
        validateReturnableOrder(order);
        validateReturnItems(order, request.getItems());

        ReturnRequestEntity returnRequest = new ReturnRequestEntity();
        returnRequest.setOrderId(order.getId());
        returnRequest.setCustomerUserId(order.getCustomerId());
        returnRequest.setReason(request.getReason());
        returnRequest.setStatus(STATUS_PENDING);
        returnRequest.setRequestedAt(OffsetDateTime.now());
        ReturnRequestEntity savedReturn = returnRequestRepository.save(returnRequest);

        List<ReturnItemRequest> items = request.getItems() == null ? List.of() : request.getItems();
        List<ReturnItemEntity> returnItems = items.stream()
                .map(item -> returnItem(savedReturn, item))
                .toList();
        returnItemRepository.saveAll(returnItems);
        savedReturn.getItems().addAll(returnItems);

        Map<String, Object> metadata = new LinkedHashMap<>();
        metadata.put("returnId", savedReturn.getId());
        metadata.put("orderId", request.getOrderId());
        metadata.put("customerUserId", order.getCustomerId());
        metadata.put("reason", request.getReason());
        metadata.put("status", savedReturn.getStatus());
        metadata.put("items", items.stream().map(this::auditItem).toList());
        auditService.write(AuditAction.RETURN_REQUESTED, "Return", savedReturn.getId().toString(), metadata);
        return mapToReturnDto(savedReturn);
    }

    public ReturnDto approveReturn(Long returnId) {
        ReturnRequestEntity returnRequest = returnRequestRepository.findById(returnId)
                .orElseThrow(() -> new ResourceNotFoundException("REF_001", "Return request not found"));
        if (!STATUS_PENDING.equals(returnRequest.getStatus())) {
            throw new BusinessException("REF_002", "Only pending return requests can be approved");
        }

        String previousStatus = returnRequest.getStatus();
        returnRequest.setStatus(STATUS_APPROVED);
        returnRequest.setApprovedAt(OffsetDateTime.now());
        returnRequest.setApprovedBy(currentActorUserId().orElse(null));
        ReturnRequestEntity savedReturn = returnRequestRepository.save(returnRequest);

        Map<String, Object> metadata = new LinkedHashMap<>();
        metadata.put("returnId", savedReturn.getId());
        metadata.put("orderId", savedReturn.getOrderId());
        metadata.put("previousStatus", previousStatus);
        metadata.put("status", savedReturn.getStatus());
        metadata.put("reason", savedReturn.getReason());
        metadata.put("approvedBy", savedReturn.getApprovedBy());
        auditService.write(AuditAction.RETURN_APPROVED, "Return", savedReturn.getId().toString(), metadata);
        return mapToReturnDto(savedReturn);
    }

    public RefundDto createRefund(CreateRefundRequest request) {
        ReturnRequestEntity returnRequest = returnRequestRepository.findById(request.getReturnRequestId())
                .orElseThrow(() -> new ResourceNotFoundException("REF_001", "Return request does not exist"));
        if (!returnRequest.getOrderId().equals(request.getOrderId())) {
            throw new BusinessException("REF_002", "Return request does not belong to this order");
        }
        if (!STATUS_APPROVED.equals(returnRequest.getStatus())) {
            throw new BusinessException("REF_002", "Return request must be approved before refund");
        }
        if (refundRepository.existsByReturnRequestId(returnRequest.getId())) {
            throw new BusinessException("REF_002", "Return request has already been refunded");
        }

        OrderEntity order = orderRepository.findById(request.getOrderId())
                .orElseThrow(() -> new ResourceNotFoundException("ORD_002", "Order not found"));
        validateReturnableOrder(order);

        PaymentEntity payment = paymentRepository.findById(request.getPaymentId())
                .orElseThrow(() -> new ResourceNotFoundException("PAY_001", "Payment not found"));
        validateRefundPayment(order, payment);

        BigDecimal refundableAmount = refundableAmount(returnRequest.getId());
        if (request.getAmount().compareTo(refundableAmount) > 0) {
            throw new BusinessException("REF_002", "Refund amount exceeds returned item value");
        }

        RefundEntity refund = new RefundEntity();
        refund.setOrderId(request.getOrderId());
        refund.setPaymentId(request.getPaymentId());
        refund.setReturnRequestId(request.getReturnRequestId());
        refund.setAmount(request.getAmount());
        refund.setStatus(STATUS_COMPLETED);
        RefundEntity savedRefund = refundRepository.save(refund);

        Map<String, Object> metadata = new LinkedHashMap<>();
        metadata.put("refundId", savedRefund.getId());
        metadata.put("returnRequestId", request.getReturnRequestId());
        metadata.put("orderId", request.getOrderId());
        metadata.put("paymentId", request.getPaymentId());
        metadata.put("amount", request.getAmount());
        metadata.put("refundableAmount", refundableAmount);
        metadata.put("status", savedRefund.getStatus());
        auditService.write(AuditAction.REFUND_PROCESSED, "Refund", savedRefund.getId().toString(), metadata);
        return mapToRefundDto(savedRefund);
    }

    @Transactional(readOnly = true)
    public RefundDto getRefund(Long refundId) {
        RefundEntity refund = refundRepository.findById(refundId)
                .orElseThrow(() -> new ResourceNotFoundException("REF_001", "Refund not found"));
        return mapToRefundDto(refund);
    }

    private void validateReturnableOrder(OrderEntity order) {
        if (!STATUS_DELIVERED.equals(order.getStatus())) {
            throw new BusinessException("REF_002", "Only delivered orders can be returned");
        }
        if (!STATUS_PAID.equals(order.getPaymentStatus())) {
            throw new BusinessException("REF_002", "Only paid orders can be returned");
        }
    }

    private void validateReturnItems(OrderEntity order, List<ReturnItemRequest> items) {
        if (items == null || items.isEmpty()) {
            throw new BusinessException("REF_002", "At least one return item is required");
        }

        Set<Long> seenOrderItemIds = new HashSet<>();
        for (ReturnItemRequest item : items) {
            if (item.getQty() == null || item.getQty() <= 0) {
                throw new BusinessException("REF_002", "Return item quantity must be positive");
            }
            if (!seenOrderItemIds.add(item.getOrderItemId())) {
                throw new BusinessException("REF_002", "Duplicate return item");
            }

            OrderItemEntity orderItem = orderItemRepository.findById(item.getOrderItemId())
                    .orElseThrow(() -> new BusinessException("REF_002", "Return item does not exist"));
            if (orderItem.getOrder() == null || !order.getId().equals(orderItem.getOrder().getId())) {
                throw new BusinessException("REF_002", "Return item does not belong to this order");
            }
            if (item.getQty() > orderItem.getQty()) {
                throw new BusinessException("REF_002", "Return item quantity exceeds purchased quantity");
            }
        }
    }

    private void validateRefundPayment(OrderEntity order, PaymentEntity payment) {
        if (payment.getOrder() == null || !order.getId().equals(payment.getOrder().getId())) {
            throw new BusinessException("REF_002", "Payment does not belong to this order");
        }
        if (!STATUS_PAID.equals(payment.getStatus()) && !STATUS_COMPLETED.equals(payment.getStatus())) {
            throw new BusinessException("REF_002", "Only completed payments can be refunded");
        }
    }

    private BigDecimal refundableAmount(Long returnRequestId) {
        List<ReturnItemEntity> returnItems = returnItemRepository.findByReturnRequest_Id(returnRequestId);
        if (returnItems.isEmpty()) {
            throw new BusinessException("REF_002", "Return request has no items");
        }

        BigDecimal total = BigDecimal.ZERO;
        for (ReturnItemEntity returnItem : returnItems) {
            OrderItemEntity orderItem = orderItemRepository.findById(returnItem.getOrderItemId())
                    .orElseThrow(() -> new BusinessException("REF_002", "Return item does not exist"));
            total = total.add(orderItem.getUnitPrice().multiply(BigDecimal.valueOf(returnItem.getQty())));
        }
        return total;
    }

    private ReturnItemEntity returnItem(ReturnRequestEntity returnRequest, ReturnItemRequest item) {
        ReturnItemEntity entity = new ReturnItemEntity();
        entity.setReturnRequest(returnRequest);
        entity.setOrderItemId(item.getOrderItemId());
        entity.setQty(item.getQty());
        return entity;
    }

    private ReturnDto mapToReturnDto(ReturnRequestEntity entity) {
        ReturnDto dto = new ReturnDto();
        dto.setReturnId(entity.getId());
        dto.setOrderId(entity.getOrderId());
        dto.setReason(entity.getReason());
        dto.setStatus(entity.getStatus());
        dto.setCreatedAt(entity.getRequestedAt());
        return dto;
    }

    private RefundDto mapToRefundDto(RefundEntity entity) {
        RefundDto dto = new RefundDto();
        dto.setRefundId(entity.getId());
        dto.setOrderId(entity.getOrderId());
        dto.setAmount(entity.getAmount());
        dto.setStatus(entity.getStatus());
        dto.setCreatedAt(entity.getCreatedAt());
        return dto;
    }

    private Optional<Long> currentActorUserId() {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof AuthenticatedUser user)) {
            return Optional.empty();
        }
        return Optional.ofNullable(user.userId());
    }

    private Map<String, Object> auditItem(ReturnItemRequest item) {
        Map<String, Object> metadata = new LinkedHashMap<>();
        metadata.put("orderItemId", item.getOrderItemId());
        metadata.put("qty", item.getQty());
        return metadata;
    }
}
