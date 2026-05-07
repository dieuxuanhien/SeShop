package com.seshop.pos.application;

import com.seshop.pos.api.dto.CloseShiftRequest;
import com.seshop.pos.api.dto.OpenShiftRequest;
import com.seshop.pos.api.dto.ShiftDto;
import com.seshop.pos.infrastructure.persistence.PosShiftEntity;
import com.seshop.pos.infrastructure.persistence.PosShiftRepository;
import com.seshop.pos.infrastructure.persistence.PosTransactionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Service
@Transactional
public class ShiftService {

    private final PosShiftRepository shiftRepository;
    private final PosTransactionRepository transactionRepository;

    public ShiftService(PosShiftRepository shiftRepository, PosTransactionRepository transactionRepository) {
        this.shiftRepository = shiftRepository;
        this.transactionRepository = transactionRepository;
    }

    public ShiftDto openShift(Long staffId, OpenShiftRequest request) {
        // Check if there is already an open shift for this staff
        shiftRepository.findByStaffIdAndStatus(staffId, "OPEN")
                .ifPresent(s -> {
                    throw new IllegalStateException("An open shift already exists for this staff member");
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
                .orElseThrow(() -> new IllegalArgumentException("Shift not found"));

        if (!"OPEN".equals(shift.getStatus())) {
            throw new IllegalStateException("Shift is not open");
        }

        BigDecimal endingCash = request.resolvedEndingCash();
        if (endingCash == null) {
            throw new IllegalArgumentException("Ending cash is required");
        }
        shift.setEndingCash(endingCash);
        shift.setEndTime(OffsetDateTime.now());
        shift.setStatus("CLOSED");

        PosShiftEntity saved = shiftRepository.save(shift);
        return mapToDto(saved);
    }

    public ShiftDto getShift(Long shiftId) {
        PosShiftEntity shift = shiftRepository.findById(shiftId)
                .orElseThrow(() -> new IllegalArgumentException("Shift not found"));
        return mapToDto(shift);
    }

    public ShiftDto getCurrentShift(Long staffId) {
        PosShiftEntity shift = shiftRepository.findByStaffIdAndStatus(staffId, "OPEN")
                .orElseThrow(() -> new IllegalArgumentException("No active shift found"));
        return mapToDto(shift);
    }

    private ShiftDto mapToDto(PosShiftEntity entity) {
        var transactions = transactionRepository.findByShiftId(entity.getId());
        BigDecimal cardTotal = transactions.stream()
                .filter(transaction -> "CARD".equalsIgnoreCase(transaction.getTransactionType()))
                .map(transaction -> transaction.getAmount() == null ? BigDecimal.ZERO : transaction.getAmount())
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal cashSales = transactions.stream()
                .filter(transaction -> "CASH".equalsIgnoreCase(transaction.getTransactionType()))
                .map(transaction -> transaction.getAmount() == null ? BigDecimal.ZERO : transaction.getAmount())
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        ShiftDto dto = new ShiftDto();
        dto.setId(entity.getId());
        dto.setStaffId(entity.getStaffId());
        dto.setLocationId(entity.getLocationId());
        dto.setStartTime(entity.getStartTime());
        dto.setEndTime(entity.getEndTime());
        dto.setStartingCash(entity.getStartingCash());
        dto.setEndingCash(entity.getEndingCash());
        dto.setStatus(entity.getStatus());
        dto.setRegisterName("Location " + entity.getLocationId());
        dto.setOpenedAt(entity.getStartTime());
        dto.setTransactionCount(transactions.size());
        dto.setCardPaymentsTotal(cardTotal);
        dto.setExpectedCash((entity.getStartingCash() == null ? BigDecimal.ZERO : entity.getStartingCash()).add(cashSales));
        return dto;
    }
}
