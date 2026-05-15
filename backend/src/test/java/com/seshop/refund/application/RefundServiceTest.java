package com.seshop.refund.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.then;

import com.seshop.audit.application.AuditService;
import com.seshop.audit.domain.AuditAction;
import com.seshop.refund.api.dto.CreateRefundRequest;
import com.seshop.refund.api.dto.CreateReturnRequest;
import com.seshop.refund.api.dto.RefundDto;
import com.seshop.refund.api.dto.ReturnDto;
import com.seshop.refund.api.dto.ReturnItemRequest;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class RefundServiceTest {

    @Mock
    private AuditService auditService;

    @Test
    void createReturnWritesRequestedAuditEvent() {
        RefundService service = new RefundService(auditService);

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
                .containsEntry("reason", "Wrong size")
                .containsEntry("status", "PENDING");
        assertThat((List<?>) metadataCaptor.getValue().get("items")).hasSize(1);
    }

    @Test
    void approveReturnWritesApprovedAuditEvent() {
        RefundService service = new RefundService(auditService);
        service.createReturn(returnRequest());

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
        RefundService service = new RefundService(auditService);
        service.createReturn(returnRequest());

        RefundDto dto = service.createRefund(refundRequest());

        assertThat(dto.getRefundId()).isEqualTo(1L);
        assertThat(dto.getStatus()).isEqualTo("PENDING");
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
                .containsEntry("status", "PENDING");
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
}
