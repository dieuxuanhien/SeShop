package com.seshop.pos.api;

import com.seshop.pos.api.dto.CloseShiftRequest;
import com.seshop.pos.api.dto.OpenShiftRequest;
import com.seshop.pos.api.dto.ShiftDto;
import com.seshop.pos.application.ShiftService;
import com.seshop.shared.security.AuthenticatedUser;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
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

    @PostMapping({"", "/open"})
    public ResponseEntity<Map<String, Object>> openShift(
            @AuthenticationPrincipal AuthenticatedUser user,
            @Valid @RequestBody OpenShiftRequest request) {
        ShiftDto shift = shiftService.openShift(user.userId(), request);

        Map<String, Object> response = new HashMap<>();
        response.put("data", shift);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * GET /api/v1/pos/shifts/current
     * 
     * Fetch the currently open shift for the authenticated staff member.
     * Used by ShiftClose.tsx to populate expectedCash and shift metadata on load.
     * 
     * Returns: ShiftData with shiftId, registerName, openedAt, transactionCount, 
     *          cardPaymentsTotal, and expectedCash (sum of cash payments)
     */
    @GetMapping("/current")
    public ResponseEntity<Map<String, Object>> getCurrentShift(@AuthenticationPrincipal AuthenticatedUser user) {
        ShiftDto shift = shiftService.getCurrentShift(user.userId());

        Map<String, Object> response = new HashMap<>();
        response.put("data", Map.of(
            "shiftId", shift.getId(),
            "registerName", shift.getRegisterName(),
            "openedAt", shift.getOpenedAt(),
            "transactionCount", shift.getTransactionCount(),
            "cardPaymentsTotal", shift.getCardPaymentsTotal(),
            "expectedCash", shift.getExpectedCash()
        ));

        return ResponseEntity.ok(response);
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
