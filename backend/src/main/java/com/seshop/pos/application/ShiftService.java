package com.seshop.pos.application;

import com.seshop.pos.api.dto.CloseShiftRequest;
import com.seshop.pos.api.dto.OpenShiftRequest;
import com.seshop.pos.api.dto.ShiftDto;
import com.seshop.pos.infrastructure.persistence.CashReconciliationEntity;
import com.seshop.pos.infrastructure.persistence.CashReconciliationRepository;
import com.seshop.pos.infrastructure.persistence.PosReceiptRepository;
import com.seshop.pos.infrastructure.persistence.PosShiftEntity;
import com.seshop.pos.infrastructure.persistence.PosShiftRepository;
import com.seshop.shared.exception.BusinessException;
import com.seshop.shared.exception.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Service
@Transactional
public class ShiftService {

    private final PosShiftRepository shiftRepository;
    private final PosReceiptRepository receiptRepository;
    private final CashReconciliationRepository reconciliationRepository;

    public ShiftService(PosShiftRepository shiftRepository,
                        PosReceiptRepository receiptRepository,
                        CashReconciliationRepository reconciliationRepository) {
        this.shiftRepository = shiftRepository;
        this.receiptRepository = receiptRepository;
        this.reconciliationRepository = reconciliationRepository;
    }

    public ShiftDto openShift(Long staffId, OpenShiftRequest request) {
        shiftRepository.findByStaffIdAndStatus(staffId, "OPEN")
                .ifPresent(s -> {
                    throw new BusinessException("POS_002", "An open shift already exists for this staff member");
                });

        PosShiftEntity shift = new PosShiftEntity();
        shift.setStaffId(staffId);
        shift.setLocationId(request.getLocationId());
        shift.setStartingCash(request.getStartingCash());
        shift.setStatus("OPEN");

        PosShiftEntity saved = shiftRepository.save(shift);
        return mapToDto(saved);
    }

    public ShiftDto closeShift(Long shiftId, CloseShiftRequest request) {
        PosShiftEntity shift = shiftRepository.findById(shiftId)
                .orElseThrow(() -> new ResourceNotFoundException("POS_002", "Shift not found"));

        if (!"OPEN".equals(shift.getStatus())) {
            throw new BusinessException("POS_001", "Shift is not open");
        }

        BigDecimal actualCash = request.resolvedEndingCash();
        if (actualCash == null) {
            throw new BusinessException("POS_001", "Ending cash is required");
        }
        BigDecimal expectedCash = request.getExpectedCash() != null ? request.getExpectedCash() : expectedCash(shift.getId());

        shift.setEndTime(OffsetDateTime.now());
        shift.setStatus("CLOSED");

        CashReconciliationEntity reconciliation = reconciliationRepository.findByShift_Id(shiftId)
                .orElseGet(CashReconciliationEntity::new);
        reconciliation.setShift(shift);
        reconciliation.setExpectedCash(expectedCash);
        reconciliation.setActualCash(actualCash);
        reconciliation.setVarianceAmount(actualCash.subtract(expectedCash));
        reconciliation.setReason(request.getReason());
        reconciliation.setApprovedBy(shift.getStaffId());
        reconciliation.setApprovedAt(OffsetDateTime.now());
        reconciliationRepository.save(reconciliation);

        PosShiftEntity saved = shiftRepository.save(shift);
        ShiftDto dto = mapToDto(saved);
        dto.setEndingCash(actualCash);
        dto.setExpectedCash(expectedCash);
        return dto;
    }

    public ShiftDto getShift(Long shiftId) {
        PosShiftEntity shift = shiftRepository.findById(shiftId)
                .orElseThrow(() -> new ResourceNotFoundException("POS_002", "Shift not found"));
        return mapToDto(shift);
    }

    public ShiftDto getCurrentShift(Long staffId) {
        PosShiftEntity shift = shiftRepository.findByStaffIdAndStatus(staffId, "OPEN")
                .orElseThrow(() -> new BusinessException("POS_002", "No active shift found"));
        return mapToDto(shift);
    }

    private ShiftDto mapToDto(PosShiftEntity entity) {
        var receipts = receiptRepository.findByShift_Id(entity.getId());
        BigDecimal cardTotal = receiptRepository.sumTotalByShiftIdAndPaymentMethod(entity.getId(), "CARD");
        BigDecimal expectedCash = expectedCash(entity.getId());
        BigDecimal actualCash = reconciliationRepository.findByShift_Id(entity.getId())
                .map(CashReconciliationEntity::getActualCash)
                .orElse(entity.getEndingCash());

        ShiftDto dto = new ShiftDto();
        dto.setId(entity.getId());
        dto.setStaffId(entity.getStaffId());
        dto.setLocationId(entity.getLocationId());
        dto.setStartTime(entity.getStartTime());
        dto.setEndTime(entity.getEndTime());
        dto.setStartingCash(BigDecimal.ZERO);
        dto.setEndingCash(actualCash);
        dto.setStatus(entity.getStatus());
        dto.setRegisterName("Location " + entity.getLocationId());
        dto.setOpenedAt(entity.getStartTime());
        dto.setTransactionCount(receipts.size());
        dto.setCardPaymentsTotal(cardTotal);
        dto.setExpectedCash(expectedCash);
        return dto;
    }

    private BigDecimal expectedCash(Long shiftId) {
        return receiptRepository.sumTotalByShiftIdAndPaymentMethod(shiftId, "CASH");
    }
}
