package com.seshop.pos.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
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
import com.seshop.shared.exception.BusinessException;
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

        // ── happy path ──────────────────────────────────────────────────────────

        @Test
        void closeShiftCreatesCashReconciliationFromReceiptTotals() {
                ShiftService service = new ShiftService(shiftRepository, receiptRepository, reconciliationRepository,
                                auditService);

                PosShiftEntity shift = openShift(501L, 42L, 11L);

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
                request.setApproverId(99L); // manager, different from cashier 42

                ShiftDto dto = service.closeShift(501L, request);

                assertThat(dto.getStatus()).isEqualTo("CLOSED");
                assertThat(dto.getEndingCash()).isEqualByComparingTo("800000.00");
                assertThat(dto.getExpectedCash()).isEqualByComparingTo("803000.00");
                assertThat(dto.getCardPaymentsTotal()).isEqualByComparingTo("100000.00");
                assertThat(dto.getTransactionCount()).isEqualTo(1);

                ArgumentCaptor<CashReconciliationEntity> reconciliationCaptor = ArgumentCaptor
                                .forClass(CashReconciliationEntity.class);
                then(reconciliationRepository).should().save(reconciliationCaptor.capture());
                CashReconciliationEntity reconciliation = reconciliationCaptor.getValue();
                assertThat(reconciliation.getShift()).isSameAs(shift);
                assertThat(reconciliation.getExpectedCash()).isEqualByComparingTo("803000.00");
                assertThat(reconciliation.getActualCash()).isEqualByComparingTo("800000.00");
                assertThat(reconciliation.getVarianceAmount()).isEqualByComparingTo("-3000.00");
                // approvedBy must be the manager, not the cashier
                assertThat(reconciliation.getApprovedBy()).isEqualTo(99L);

                ArgumentCaptor<Map<String, Object>> metadataCaptor = metadataCaptor();
                then(auditService).should().write(
                                eq(AuditAction.POS_SHIFT_CLOSED),
                                eq("PosShift"),
                                eq("501"),
                                metadataCaptor.capture());
                assertThat(metadataCaptor.getValue())
                                .containsEntry("shiftId", 501L)
                                .containsEntry("staffId", 42L)
                                .containsEntry("locationId", 11L)
                                .containsEntry("expectedCash", new BigDecimal("803000.00"))
                                .containsEntry("actualCash", new BigDecimal("800000.00"))
                                .containsEntry("varianceAmount", new BigDecimal("-3000.00"))
                                .containsEntry("reason", "End of day close")
                                .containsEntry("approvedBy", 99L);
        }

        // ── self-approval rejection ─────────────────────────────────────────────

        @Test
        void closeShiftRejectsSelfApproval() {
                ShiftService service = new ShiftService(shiftRepository, receiptRepository, reconciliationRepository,
                                auditService);

                PosShiftEntity shift = openShift(501L, 42L, 11L);
                given(shiftRepository.findById(501L)).willReturn(Optional.of(shift));

                CloseShiftRequest request = new CloseShiftRequest();
                request.setActualCash(new BigDecimal("800000.00"));
                request.setReason("Self close");
                request.setApproverId(42L); // same as cashier — must be rejected

                assertThatThrownBy(() -> service.closeShift(501L, request))
                                .isInstanceOf(BusinessException.class)
                                .hasMessageContaining("manager must approve");
        }

        // ── missing reason when variance exceeds threshold ──────────────────────

        @Test
        void closeShiftRequiresReasonWhenVarianceExceedsThreshold() {
                ShiftService service = new ShiftService(shiftRepository, receiptRepository, reconciliationRepository,
                                auditService);

                PosShiftEntity shift = openShift(501L, 42L, 11L);
                given(shiftRepository.findById(501L)).willReturn(Optional.of(shift));
                // expected cash = 500_000, actual = 400_000 → variance = 100_000 > 50_000
                // threshold
                given(receiptRepository.sumTotalByShiftIdAndPaymentMethod(501L, "CASH"))
                                .willReturn(new BigDecimal("500000.00"));

                CloseShiftRequest request = new CloseShiftRequest();
                request.setActualCash(new BigDecimal("400000.00"));
                // no reason provided
                request.setApproverId(99L);

                assertThatThrownBy(() -> service.closeShift(501L, request))
                                .isInstanceOf(BusinessException.class)
                                .hasMessageContaining("discrepancy reason is required");
        }

        // ── small variance without reason is allowed ────────────────────────────

        @Test
        void closeShiftAllowsSmallVarianceWithoutReason() {
                ShiftService service = new ShiftService(shiftRepository, receiptRepository, reconciliationRepository,
                                auditService);

                PosShiftEntity shift = openShift(501L, 42L, 11L);
                given(shiftRepository.findById(501L)).willReturn(Optional.of(shift));
                // variance = 10_000 < 50_000 threshold → no reason required
                given(receiptRepository.sumTotalByShiftIdAndPaymentMethod(501L, "CASH"))
                                .willReturn(new BigDecimal("200000.00"));
                given(receiptRepository.sumTotalByShiftIdAndPaymentMethod(501L, "CARD"))
                                .willReturn(BigDecimal.ZERO);
                given(receiptRepository.findByShift_Id(501L)).willReturn(List.of());
                given(reconciliationRepository.findByShift_Id(501L)).willReturn(Optional.empty());
                given(shiftRepository.save(shift)).willReturn(shift);

                CloseShiftRequest request = new CloseShiftRequest();
                request.setActualCash(new BigDecimal("190000.00")); // variance = 10_000
                // reason intentionally omitted
                request.setApproverId(99L);

                ShiftDto dto = service.closeShift(501L, request);
                assertThat(dto.getStatus()).isEqualTo("CLOSED");
        }

        // ── helpers ─────────────────────────────────────────────────────────────

        private PosShiftEntity openShift(Long shiftId, Long staffId, Long locationId) {
                PosShiftEntity shift = new PosShiftEntity();
                shift.setId(shiftId);
                shift.setStaffId(staffId);
                shift.setLocationId(locationId);
                shift.setStatus("OPEN");
                return shift;
        }

        @SuppressWarnings("unchecked")
        private ArgumentCaptor<Map<String, Object>> metadataCaptor() {
                return ArgumentCaptor.forClass(Map.class);
        }
}
