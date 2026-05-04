package com.seshop.inventory.api;

import com.seshop.inventory.api.dto.CreateTransferRequest;
import com.seshop.inventory.api.dto.InventoryAdjustmentRequest;
import com.seshop.inventory.api.dto.ReceiveTransferRequest;
import com.seshop.inventory.application.InventoryService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/staff/inventory")
public class StaffInventoryController {

    private final InventoryService inventoryService;

    public StaffInventoryController(InventoryService inventoryService) {
        this.inventoryService = inventoryService;
    }

    @PostMapping("/adjustments")
    public ResponseEntity<Map<String, Object>> adjustInventory(@Valid @RequestBody InventoryAdjustmentRequest request) {
        inventoryService.adjustInventory(request);
        
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Inventory adjusted successfully");
        
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/transfers")
    public ResponseEntity<Map<String, Object>> createTransfer(@Valid @RequestBody CreateTransferRequest request) {
        // In a real application, createdBy would come from the authenticated user context
        Long createdBy = 1L; 
        Long transferId = inventoryService.createTransfer(request, createdBy);
        
        Map<String, Object> data = new HashMap<>();
        data.put("transferId", transferId);

        Map<String, Object> response = new HashMap<>();
        response.put("data", data);
        
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/transfers/{transferId}/approve")
    public ResponseEntity<Void> approveTransfer(@PathVariable Long transferId) {
        inventoryService.approveTransfer(transferId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/transfers/{transferId}/receive")
    public ResponseEntity<Void> receiveTransfer(
            @PathVariable Long transferId, 
            @Valid @RequestBody ReceiveTransferRequest request) {
        inventoryService.receiveTransfer(transferId, request);
        return ResponseEntity.ok().build();
    }
}
