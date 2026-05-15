package com.seshop.inventory.api;

import com.seshop.inventory.api.dto.CreateTransferRequest;
import com.seshop.inventory.api.dto.InventoryAdjustmentRequest;
import com.seshop.inventory.api.dto.InventoryAdjustmentResponse;
import com.seshop.inventory.api.dto.ReceiveTransferRequest;
import com.seshop.inventory.application.InventoryService;
import com.seshop.shared.api.ApiResponse;
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
@RequestMapping("/api/v1/staff/inventory")
public class StaffInventoryController {

    private static final String INVENTORY_ADJUST = "inventory.adjust";
    private static final String INVENTORY_TRANSFER = "inventory.transfer";

    private final InventoryService inventoryService;
    private final PermissionValidator permissionValidator;

    public StaffInventoryController(InventoryService inventoryService, PermissionValidator permissionValidator) {
        this.inventoryService = inventoryService;
        this.permissionValidator = permissionValidator;
    }

    @GetMapping("/balances")
    public ApiResponse<?> listBalances(
            @RequestParam(required = false) Long variantId,
            @RequestParam(required = false) Long locationId,
            @RequestParam(required = false) String skuCode,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        permissionValidator.requireAny(INVENTORY_ADJUST, INVENTORY_TRANSFER);
        return ApiResponse.success(inventoryService.listBalances(variantId, locationId, skuCode, page, size));
    }

    @PostMapping("/adjustments")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<InventoryAdjustmentResponse> adjustInventory(@Valid @RequestBody InventoryAdjustmentRequest request) {
        permissionValidator.require(INVENTORY_ADJUST);
        return ApiResponse.success(inventoryService.adjustInventory(request));
    }

    @PostMapping("/transfers")
    public ResponseEntity<Map<String, Object>> createTransfer(
            @AuthenticationPrincipal AuthenticatedUser user,
            @Valid @RequestBody CreateTransferRequest request) {
        permissionValidator.require(INVENTORY_TRANSFER);
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
        permissionValidator.require(INVENTORY_TRANSFER);
        return ApiResponse.success(inventoryService.listTransfers(page, size));
    }

    @PostMapping("/transfers/{transferId}/approve")
    public ResponseEntity<Void> approveTransfer(@PathVariable Long transferId) {
        permissionValidator.require(INVENTORY_TRANSFER);
        inventoryService.approveTransfer(transferId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/transfers/{transferId}/receive")
    public ResponseEntity<Void> receiveTransfer(
            @PathVariable Long transferId, 
            @Valid @RequestBody ReceiveTransferRequest request) {
        permissionValidator.require(INVENTORY_TRANSFER);
        inventoryService.receiveTransfer(transferId, request);
        return ResponseEntity.ok().build();
    }
}
