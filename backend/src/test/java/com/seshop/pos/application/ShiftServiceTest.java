package com.seshop.pos.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

import com.seshop.audit.application.AuditService;
import com.seshop.audit.domain.AuditAction;
import com.seshop.pos.api.dto.CloseShiftRequest;
import com.seshop.pos.api.dto.ShiftDto;
import com.seshop.pos.infrastructure.persistence.CashReconciliationEntity;
import com.seshop.pos.infrastructure.persistence.CashReconciliationRepository;
import com.seshop.pos.infrastructure.persistence.PosReceiptEntity;
import com.seshop.pos.infrastructure.persistence.PosReceiptRepository;
import com.seshop.pos.infrastructure.persistence.PosShiftEntity;
import com.seshop.pos.infrastructure.persistence.PosShiftRepository;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ShiftServiceTest {

    @Mock
    private PosShiftRepository shiftRepository;

    @Mock
    private PosReceiptRepository receiptRepository;

    @Mock
    private CashReconciliationRepository reconciliationRepository;

    @Mock
    private AuditService auditService;

    @Test
    void closeShiftCreatesCashReconciliationFromReceiptTotals() {
        ShiftService service = new ShiftService(shiftRepository, receiptRepository, reconciliationRepository, auditService);

        PosShiftEntity shift = new PosShiftEntity();
        shift.setId(501L);
        shift.setStaffId(42L);
        shift.setLocationId(11L);
        shift.setStatus("OPEN");

        PosReceiptEntity receipt = new PosReceiptEntity();
        receipt.setId(9001L);

        given(shiftRepository.findById(501L)).willReturn(Optional.of(shift));
        given(receiptRepository.sumTotalByShiftIdAndPaymentMethod(501L, "CASH"))
                .willReturn(new BigDecimal("803000.00"));
        given(receiptRepository.sumTotalByShiftIdAndPaymentMethod(501L, "CARD"))
                .willReturn(new BigDecimal("100000.00"));
        given(receiptRepository.findByShift_Id(501L)).willReturn(List.of(receipt));
        given(reconciliationRepository.findByShift_Id(501L)).willReturn(Optional.empty());
        given(shiftRepository.save(shift)).willReturn(shift);

        CloseShiftRequest request = new CloseShiftRequest();
        request.setActualCash(new BigDecimal("800000.00"));
        request.setReason("End of day close");

        ShiftDto dto = service.closeShift(501L, request);

        assertThat(dto.getStatus()).isEqualTo("CLOSED");
        assertThat(dto.getEndingCash()).isEqualByComparingTo("800000.00");
        assertThat(dto.getExpectedCash()).isEqualByComparingTo("803000.00");
        assertThat(dto.getCardPaymentsTotal()).isEqualByComparingTo("100000.00");
        assertThat(dto.getTransactionCount()).isEqualTo(1);

        ArgumentCaptor<CashReconciliationEntity> reconciliationCaptor =
                ArgumentCaptor.forClass(CashReconciliationEntity.class);
        then(reconciliationRepository).should().save(reconciliationCaptor.capture());
        CashReconciliationEntity reconciliation = reconciliationCaptor.getValue();
        assertThat(reconciliation.getShift()).isSameAs(shift);
        assertThat(reconciliation.getExpectedCash()).isEqualByComparingTo("803000.00");
        assertThat(reconciliation.getActualCash()).isEqualByComparingTo("800000.00");
        assertThat(reconciliation.getVarianceAmount()).isEqualByComparingTo("-3000.00");
        assertThat(reconciliation.getApprovedBy()).isEqualTo(42L);
        ArgumentCaptor<Map<String, Object>> metadataCaptor = metadataCaptor();
        then(auditService).should().write(
                eq(AuditAction.POS_SHIFT_CLOSED),
                eq("PosShift"),
                eq("501"),
                metadataCaptor.capture()
        );
        assertThat(metadataCaptor.getValue())
                .containsEntry("shiftId", 501L)
                .containsEntry("staffId", 42L)
                .containsEntry("locationId", 11L)
                .containsEntry("expectedCash", new BigDecimal("803000.00"))
                .containsEntry("actualCash", new BigDecimal("800000.00"))
                .containsEntry("varianceAmount", new BigDecimal("-3000.00"))
                .containsEntry("reason", "End of day close")
                .containsEntry("approvedBy", 42L);
    }

    @SuppressWarnings("unchecked")
    private ArgumentCaptor<Map<String, Object>> metadataCaptor() {
        return ArgumentCaptor.forClass(Map.class);
    }
}
