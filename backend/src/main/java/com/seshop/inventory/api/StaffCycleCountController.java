package com.seshop.inventory.api;

import com.seshop.inventory.api.dto.CreateCycleCountRequest;
import com.seshop.inventory.api.dto.CycleCountItemsRequest;
import com.seshop.inventory.application.CycleCountService;
import com.seshop.shared.security.AuthenticatedUser;
import com.seshop.shared.security.PermissionValidator;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/staff/cycle-counts")
public class StaffCycleCountController {

    private static final String INVENTORY_ADJUST = "inventory.adjust";

    private final CycleCountService cycleCountService;
    private final PermissionValidator permissionValidator;

    public StaffCycleCountController(CycleCountService cycleCountService, PermissionValidator permissionValidator) {
        this.cycleCountService = cycleCountService;
        this.permissionValidator = permissionValidator;
    }

    @PostMapping
    public ResponseEntity<Map<String, Object>> createCycleCount(
            @AuthenticationPrincipal AuthenticatedUser user,
            @Valid @RequestBody CreateCycleCountRequest request) {
        permissionValidator.require(INVENTORY_ADJUST);
        Long cycleCountId = cycleCountService.createCycleCount(request, user.userId());

        Map<String, Object> data = new HashMap<>();
        data.put("cycleCountId", cycleCountId);

        Map<String, Object> response = new HashMap<>();
        response.put("data", data);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/{cycleCountId}/items")
    public ResponseEntity<Void> submitItems(
            @PathVariable Long cycleCountId,
            @Valid @RequestBody CycleCountItemsRequest request) {
        permissionValidator.require(INVENTORY_ADJUST);
        cycleCountService.submitItems(cycleCountId, request);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{cycleCountId}/approve")
    public ResponseEntity<Void> approveCycleCount(
            @AuthenticationPrincipal AuthenticatedUser user,
            @PathVariable Long cycleCountId) {
        permissionValidator.require(INVENTORY_ADJUST);
        cycleCountService.approveCycleCount(cycleCountId, user.userId());
        return ResponseEntity.ok().build();
    }
}
