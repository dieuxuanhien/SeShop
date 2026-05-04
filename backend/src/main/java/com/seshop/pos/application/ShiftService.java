package com.seshop.pos.application;

import com.seshop.pos.api.dto.CloseShiftRequest;
import com.seshop.pos.api.dto.OpenShiftRequest;
import com.seshop.pos.api.dto.ShiftDto;
import com.seshop.pos.infrastructure.persistence.PosShiftEntity;
import com.seshop.pos.infrastructure.persistence.PosShiftRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;

@Service
@Transactional
public class ShiftService {

    private final PosShiftRepository shiftRepository;

    public ShiftService(PosShiftRepository shiftRepository) {
        this.shiftRepository = shiftRepository;
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

        shift.setEndingCash(request.getEndingCash());
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

    private ShiftDto mapToDto(PosShiftEntity entity) {
        ShiftDto dto = new ShiftDto();
        dto.setId(entity.getId());
        dto.setStaffId(entity.getStaffId());
        dto.setLocationId(entity.getLocationId());
        dto.setStartTime(entity.getStartTime());
        dto.setEndTime(entity.getEndTime());
        dto.setStartingCash(entity.getStartingCash());
        dto.setEndingCash(entity.getEndingCash());
        dto.setStatus(entity.getStatus());
        return dto;
    }
}
