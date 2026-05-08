package com.seshop.inventory.api;

import com.seshop.inventory.api.dto.CreateTransferRequest;
import com.seshop.inventory.api.dto.InventoryAdjustmentRequest;
import com.seshop.inventory.api.dto.InventoryAdjustmentResponse;
import com.seshop.inventory.api.dto.ReceiveTransferRequest;
import com.seshop.inventory.application.InventoryService;
import com.seshop.shared.api.ApiResponse;
import com.seshop.shared.security.AuthenticatedUser;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
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

    @GetMapping("/balances")
    public ApiResponse<?> listBalances(
            @RequestParam(required = false) Long variantId,
            @RequestParam(required = false) Long locationId,
            @RequestParam(required = false) String skuCode,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ApiResponse.success(inventoryService.listBalances(variantId, locationId, skuCode, page, size));
    }

    @PostMapping("/adjustments")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<InventoryAdjustmentResponse> adjustInventory(@Valid @RequestBody InventoryAdjustmentRequest request) {
        return ApiResponse.success(inventoryService.adjustInventory(request));
    }

    @PostMapping("/transfers")
    public ResponseEntity<Map<String, Object>> createTransfer(
            @AuthenticationPrincipal AuthenticatedUser user,
            @Valid @RequestBody CreateTransferRequest request) {
        Long transferId = inventoryService.createTransfer(request, user.userId());
        
        Map<String, Object> data = new HashMap<>();
        data.put("transferId", transferId);

        Map<String, Object> response = new HashMap<>();
        response.put("data", data);
        
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/transfers")
    public ApiResponse<?> listTransfers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ApiResponse.success(inventoryService.listTransfers(page, size));
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
