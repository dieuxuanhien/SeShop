package com.seshop.pos.api;

import com.seshop.pos.api.dto.CloseShiftRequest;
import com.seshop.pos.api.dto.OpenShiftRequest;
import com.seshop.pos.api.dto.ShiftDto;
import com.seshop.pos.application.ShiftService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/pos/shifts")
public class ShiftController {

    private final ShiftService shiftService;

    public ShiftController(ShiftService shiftService) {
        this.shiftService = shiftService;
    }

    @PostMapping
    public ResponseEntity<Map<String, Object>> openShift(@Valid @RequestBody OpenShiftRequest request) {
        // In a real application, staffId would come from the authenticated user context
        Long staffId = 1L;
        ShiftDto shift = shiftService.openShift(staffId, request);

        Map<String, Object> response = new HashMap<>();
        response.put("data", shift);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{shiftId}")
    public ResponseEntity<Map<String, Object>> getShift(@PathVariable Long shiftId) {
        ShiftDto shift = shiftService.getShift(shiftId);

        Map<String, Object> response = new HashMap<>();
        response.put("data", shift);

        return ResponseEntity.ok(response);
    }

    @PostMapping("/{shiftId}/close")
    public ResponseEntity<Map<String, Object>> closeShift(
            @PathVariable Long shiftId, 
            @Valid @RequestBody CloseShiftRequest request) {
        ShiftDto shift = shiftService.closeShift(shiftId, request);

        Map<String, Object> response = new HashMap<>();
        response.put("data", shift);

        return ResponseEntity.ok(response);
    }
}
