package com.seshop.inventory.api;

import com.seshop.inventory.api.dto.CreateTransferRequest;
import com.seshop.inventory.api.dto.InventoryAdjustmentRequest;
import com.seshop.inventory.api.dto.InventoryAdjustmentResponse;
import com.seshop.inventory.api.dto.LocationDto;
import com.seshop.inventory.api.dto.ReceiveTransferRequest;
import com.seshop.inventory.application.InventoryService;
import com.seshop.shared.api.ApiResponse;
import com.seshop.shared.security.AuthenticatedUser;
import com.seshop.shared.security.LocationAccessService;
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
    private static final String INVENTORY_ADJUST_OVERRIDE = "inventory.adjust.override";
    private static final String INVENTORY_TRANSFER = "inventory.transfer";

    private final InventoryService inventoryService;
    private final PermissionValidator permissionValidator;
    private final LocationAccessService locationAccessService;

    public StaffInventoryController(
            InventoryService inventoryService,
            PermissionValidator permissionValidator,
            LocationAccessService locationAccessService) {
        this.inventoryService = inventoryService;
        this.permissionValidator = permissionValidator;
        this.locationAccessService = locationAccessService;
    }

    @GetMapping("/balances")
    public ApiResponse<?> listBalances(
            @AuthenticationPrincipal AuthenticatedUser user,
            @RequestParam(required = false) Long variantId,
            @RequestParam(required = false) Long locationId,
            @RequestParam(required = false) String skuCode,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        permissionValidator.requireAny(INVENTORY_ADJUST, INVENTORY_TRANSFER);
        return ApiResponse.success(inventoryService.listBalances(
                variantId,
                locationId,
                skuCode,
                page,
                size,
                locationAccessService.scopeFor(user)));
    }

    @GetMapping("/locations")
    public ApiResponse<?> listStaffLocations(@AuthenticationPrincipal AuthenticatedUser user) {
        return ApiResponse.success(inventoryService.getStaffLocations(locationAccessService.scopeFor(user)));
    }

    @PostMapping("/adjustments")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<InventoryAdjustmentResponse> adjustInventory(
            @AuthenticationPrincipal AuthenticatedUser user,
            @Valid @RequestBody InventoryAdjustmentRequest request) {
        permissionValidator.require(INVENTORY_ADJUST);
        return ApiResponse.success(inventoryService.adjustInventory(
                request,
                permissionValidator.hasPermission(INVENTORY_ADJUST_OVERRIDE),
                locationAccessService.scopeFor(user)));
    }

    @PostMapping("/transfers")
    public ResponseEntity<Map<String, Object>> createTransfer(
            @AuthenticationPrincipal AuthenticatedUser user,
            @Valid @RequestBody CreateTransferRequest request) {
        permissionValidator.require(INVENTORY_TRANSFER);
        Long transferId = inventoryService.createTransfer(request, user.userId(), locationAccessService.scopeFor(user));
        
        Map<String, Object> data = new HashMap<>();
        data.put("transferId", transferId);

        Map<String, Object> response = new HashMap<>();
        response.put("data", data);
        
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/transfers")
    public ApiResponse<?> listTransfers(
            @AuthenticationPrincipal AuthenticatedUser user,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        permissionValidator.require(INVENTORY_TRANSFER);
        return ApiResponse.success(inventoryService.listTransfers(page, size, locationAccessService.scopeFor(user)));
    }

    @PostMapping("/transfers/{transferId}/approve")
    public ResponseEntity<Void> approveTransfer(
            @AuthenticationPrincipal AuthenticatedUser user,
            @PathVariable Long transferId) {
        permissionValidator.require(INVENTORY_TRANSFER);
        inventoryService.approveTransfer(transferId, locationAccessService.scopeFor(user));
        return ResponseEntity.ok().build();
    }

    @PostMapping("/transfers/{transferId}/receive")
    public ResponseEntity<Void> receiveTransfer(
            @AuthenticationPrincipal AuthenticatedUser user,
            @PathVariable Long transferId, 
            @Valid @RequestBody ReceiveTransferRequest request) {
        permissionValidator.require(INVENTORY_TRANSFER);
        inventoryService.receiveTransfer(transferId, request, locationAccessService.scopeFor(user));
        return ResponseEntity.ok().build();
    }

    @PostMapping("/transfers/{transferId}/cancel")
    public ResponseEntity<Void> cancelTransfer(
            @AuthenticationPrincipal AuthenticatedUser user,
            @PathVariable Long transferId) {
        permissionValidator.require(INVENTORY_TRANSFER);
        inventoryService.cancelTransfer(transferId, locationAccessService.scopeFor(user));
        return ResponseEntity.ok().build();
    }
}
