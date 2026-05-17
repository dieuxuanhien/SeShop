package com.seshop.refund.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

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
import com.seshop.refund.api.dto.ReturnDto;
import com.seshop.refund.api.dto.ReturnItemRequest;
import com.seshop.refund.infrastructure.persistence.RefundEntity;
import com.seshop.refund.infrastructure.persistence.RefundRepository;
import com.seshop.refund.infrastructure.persistence.ReturnItemEntity;
import com.seshop.refund.infrastructure.persistence.ReturnItemRepository;
import com.seshop.refund.infrastructure.persistence.ReturnRequestEntity;
import com.seshop.refund.infrastructure.persistence.ReturnRequestRepository;
import com.seshop.shared.exception.BusinessException;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class RefundServiceTest {

    @Mock
    private AuditService auditService;

    @Mock
    private ReturnRequestRepository returnRequestRepository;

    @Mock
    private ReturnItemRepository returnItemRepository;

    @Mock
    private RefundRepository refundRepository;

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private OrderItemRepository orderItemRepository;

    @Mock
    private PaymentRepository paymentRepository;

    @Test
    void createReturnWritesRequestedAuditEvent() {
        RefundService service = service();
        OrderEntity order = deliveredPaidOrder();
        OrderItemEntity item = orderItem(order, 1001L, 1, "250000.00");
        given(orderRepository.findById(900L)).willReturn(Optional.of(order));
        given(orderItemRepository.findById(1001L)).willReturn(Optional.of(item));
        given(returnRequestRepository.save(any(ReturnRequestEntity.class))).willAnswer(invocation -> {
            ReturnRequestEntity entity = invocation.getArgument(0);
            entity.setId(1L);
            return entity;
        });

        ReturnDto dto = service.createReturn(returnRequest());

        assertThat(dto.getReturnId()).isEqualTo(1L);
        assertThat(dto.getStatus()).isEqualTo("PENDING");
        ArgumentCaptor<Map<String, Object>> metadataCaptor = metadataCaptor();
        then(auditService).should().write(
                eq(AuditAction.RETURN_REQUESTED),
                eq("Return"),
                eq("1"),
                metadataCaptor.capture()
        );
        assertThat(metadataCaptor.getValue())
                .containsEntry("returnId", 1L)
                .containsEntry("orderId", 900L)
                .containsEntry("customerUserId", 77L)
                .containsEntry("reason", "Wrong size")
                .containsEntry("status", "PENDING");
        assertThat((List<?>) metadataCaptor.getValue().get("items")).hasSize(1);
    }

    @Test
    void createReturnRejectsOrderBeforeDelivery() {
        RefundService service = service();
        OrderEntity order = deliveredPaidOrder();
        order.setStatus("SHIPPED");
        given(orderRepository.findById(900L)).willReturn(Optional.of(order));

        assertThatThrownBy(() -> service.createReturn(returnRequest()))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Only delivered orders can be returned");
    }

    @Test
    void approveReturnWritesApprovedAuditEvent() {
        RefundService service = service();
        ReturnRequestEntity returnRequest = returnRequestEntity("PENDING");
        given(returnRequestRepository.findById(1L)).willReturn(Optional.of(returnRequest));
        given(returnRequestRepository.save(returnRequest)).willReturn(returnRequest);

        ReturnDto dto = service.approveReturn(1L);

        assertThat(dto.getStatus()).isEqualTo("APPROVED");
        ArgumentCaptor<Map<String, Object>> metadataCaptor = metadataCaptor();
        then(auditService).should().write(
                eq(AuditAction.RETURN_APPROVED),
                eq("Return"),
                eq("1"),
                metadataCaptor.capture()
        );
        assertThat(metadataCaptor.getValue())
                .containsEntry("returnId", 1L)
                .containsEntry("orderId", 900L)
                .containsEntry("previousStatus", "PENDING")
                .containsEntry("status", "APPROVED")
                .containsEntry("reason", "Wrong size");
    }

    @Test
    void createRefundWritesProcessedAuditEvent() {
        RefundService service = service();
        OrderEntity order = deliveredPaidOrder();
        OrderItemEntity item = orderItem(order, 1001L, 1, "250000.00");
        PaymentEntity payment = payment(order, "PAID");
        ReturnRequestEntity returnRequest = returnRequestEntity("APPROVED");
        ReturnItemEntity returnItem = returnItem(returnRequest, 1001L, 1);
        given(returnRequestRepository.findById(1L)).willReturn(Optional.of(returnRequest));
        given(refundRepository.existsByReturnRequestId(1L)).willReturn(false);
        given(orderRepository.findById(900L)).willReturn(Optional.of(order));
        given(paymentRepository.findById(300L)).willReturn(Optional.of(payment));
        given(returnItemRepository.findByReturnRequest_Id(1L)).willReturn(List.of(returnItem));
        given(orderItemRepository.findById(1001L)).willReturn(Optional.of(item));
        given(refundRepository.save(any(RefundEntity.class))).willAnswer(invocation -> {
            RefundEntity entity = invocation.getArgument(0);
            entity.setId(1L);
            entity.setCreatedAt(OffsetDateTime.now());
            return entity;
        });

        RefundDto dto = service.createRefund(refundRequest());

        assertThat(dto.getRefundId()).isEqualTo(1L);
        assertThat(dto.getStatus()).isEqualTo("COMPLETED");
        ArgumentCaptor<Map<String, Object>> metadataCaptor = metadataCaptor();
        then(auditService).should().write(
                eq(AuditAction.REFUND_PROCESSED),
                eq("Refund"),
                eq("1"),
                metadataCaptor.capture()
        );
        assertThat(metadataCaptor.getValue())
                .containsEntry("refundId", 1L)
                .containsEntry("returnRequestId", 1L)
                .containsEntry("orderId", 900L)
                .containsEntry("paymentId", 300L)
                .containsEntry("amount", new BigDecimal("250000.00"))
                .containsEntry("refundableAmount", new BigDecimal("250000.00"))
                .containsEntry("status", "COMPLETED");
    }

    @Test
    void createRefundRejectsAmountAboveReturnedItemValue() {
        RefundService service = service();
        OrderEntity order = deliveredPaidOrder();
        OrderItemEntity item = orderItem(order, 1001L, 1, "250000.00");
        PaymentEntity payment = payment(order, "PAID");
        ReturnRequestEntity returnRequest = returnRequestEntity("APPROVED");
        ReturnItemEntity returnItem = returnItem(returnRequest, 1001L, 1);
        CreateRefundRequest request = refundRequest();
        request.setAmount(new BigDecimal("250001.00"));
        given(returnRequestRepository.findById(1L)).willReturn(Optional.of(returnRequest));
        given(refundRepository.existsByReturnRequestId(1L)).willReturn(false);
        given(orderRepository.findById(900L)).willReturn(Optional.of(order));
        given(paymentRepository.findById(300L)).willReturn(Optional.of(payment));
        given(returnItemRepository.findByReturnRequest_Id(1L)).willReturn(List.of(returnItem));
        given(orderItemRepository.findById(1001L)).willReturn(Optional.of(item));

        assertThatThrownBy(() -> service.createRefund(request))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Refund amount exceeds returned item value");
    }

    private RefundService service() {
        return new RefundService(
                returnRequestRepository,
                returnItemRepository,
                refundRepository,
                orderRepository,
                orderItemRepository,
                paymentRepository,
                auditService
        );
    }

    @SuppressWarnings("unchecked")
    private ArgumentCaptor<Map<String, Object>> metadataCaptor() {
        return ArgumentCaptor.forClass(Map.class);
    }

    private CreateReturnRequest returnRequest() {
        ReturnItemRequest item = new ReturnItemRequest();
        item.setOrderItemId(1001L);
        item.setQty(1);

        CreateReturnRequest request = new CreateReturnRequest();
        request.setOrderId(900L);
        request.setReason("Wrong size");
        request.setItems(List.of(item));
        return request;
    }

    private CreateRefundRequest refundRequest() {
        CreateRefundRequest request = new CreateRefundRequest();
        request.setOrderId(900L);
        request.setPaymentId(300L);
        request.setReturnRequestId(1L);
        request.setAmount(new BigDecimal("250000.00"));
        return request;
    }

    private OrderEntity deliveredPaidOrder() {
        OrderEntity order = new OrderEntity();
        order.setId(900L);
        order.setCustomerId(77L);
        order.setStatus("DELIVERED");
        order.setPaymentStatus("PAID");
        order.setTotalAmount(new BigDecimal("250000.00"));
        return order;
    }

    private OrderItemEntity orderItem(OrderEntity order, Long id, int qty, String unitPrice) {
        OrderItemEntity item = new OrderItemEntity();
        item.setId(id);
        item.setOrder(order);
        item.setQty(qty);
        item.setUnitPrice(new BigDecimal(unitPrice));
        return item;
    }

    private PaymentEntity payment(OrderEntity order, String status) {
        PaymentEntity payment = new PaymentEntity();
        payment.setId(300L);
        payment.setOrder(order);
        payment.setStatus(status);
        return payment;
    }

    private ReturnRequestEntity returnRequestEntity(String status) {
        ReturnRequestEntity entity = new ReturnRequestEntity();
        entity.setId(1L);
        entity.setOrderId(900L);
        entity.setCustomerUserId(77L);
        entity.setReason("Wrong size");
        entity.setStatus(status);
        entity.setRequestedAt(OffsetDateTime.now());
        return entity;
    }

    private ReturnItemEntity returnItem(ReturnRequestEntity returnRequest, Long orderItemId, int qty) {
        ReturnItemEntity item = new ReturnItemEntity();
        item.setId(51L);
        item.setReturnRequest(returnRequest);
        item.setOrderItemId(orderItemId);
        item.setQty(qty);
        return item;
    }
}
