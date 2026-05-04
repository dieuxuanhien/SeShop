package com.seshop.inventory.api;

import com.seshop.inventory.api.dto.CreateCycleCountRequest;
import com.seshop.inventory.api.dto.CycleCountItemsRequest;
import com.seshop.inventory.application.CycleCountService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/staff/cycle-counts")
public class StaffCycleCountController {

    private final CycleCountService cycleCountService;

    public StaffCycleCountController(CycleCountService cycleCountService) {
        this.cycleCountService = cycleCountService;
    }

    @PostMapping
    public ResponseEntity<Map<String, Object>> createCycleCount(@Valid @RequestBody CreateCycleCountRequest request) {
        // In a real application, startedBy would come from the authenticated user context
        Long startedBy = 1L;
        Long cycleCountId = cycleCountService.createCycleCount(request, startedBy);

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
        cycleCountService.submitItems(cycleCountId, request);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{cycleCountId}/approve")
    public ResponseEntity<Void> approveCycleCount(@PathVariable Long cycleCountId) {
        // In a real application, approvedBy would come from the authenticated user context
        Long approvedBy = 1L;
        cycleCountService.approveCycleCount(cycleCountId, approvedBy);
        return ResponseEntity.ok().build();
    }
}
