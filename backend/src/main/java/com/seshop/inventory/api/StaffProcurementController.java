package com.seshop.inventory.api;

import com.seshop.inventory.api.dto.CreatePurchaseOrderRequest;
import com.seshop.inventory.api.dto.GoodsReceiptRequest;
import com.seshop.inventory.api.dto.GoodsReceiptResponse;
import com.seshop.inventory.api.dto.PurchaseOrderResponse;
import com.seshop.inventory.application.ProcurementService;
import com.seshop.shared.api.ApiResponse;
import com.seshop.shared.security.AuthenticatedUser;
import com.seshop.shared.security.LocationAccessService;
import com.seshop.shared.security.PermissionValidator;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/staff")
public class StaffProcurementController {

    private static final String INVENTORY_TRANSFER = "inventory.transfer";

    private final ProcurementService procurementService;
    private final PermissionValidator permissionValidator;
    private final LocationAccessService locationAccessService;

    public StaffProcurementController(
            ProcurementService procurementService,
            PermissionValidator permissionValidator,
            LocationAccessService locationAccessService) {
        this.procurementService = procurementService;
        this.permissionValidator = permissionValidator;
        this.locationAccessService = locationAccessService;
    }

    @PostMapping("/purchase-orders")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<PurchaseOrderResponse> createPurchaseOrder(
            @AuthenticationPrincipal AuthenticatedUser user,
            @Valid @RequestBody CreatePurchaseOrderRequest request) {
        permissionValidator.require(INVENTORY_TRANSFER);
        locationAccessService.requireLocationAccess(user, request.getDestinationLocationId());
        return ApiResponse.success(procurementService.createPurchaseOrder(request, user.userId()));
    }

    @PostMapping("/goods-receipts")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<GoodsReceiptResponse> createGoodsReceipt(
            @AuthenticationPrincipal AuthenticatedUser user,
            @Valid @RequestBody GoodsReceiptRequest request) {
        permissionValidator.require(INVENTORY_TRANSFER);
        Long destinationLocationId = procurementService.destinationLocationIdForPurchaseOrder(request.getPurchaseOrderId());
        locationAccessService.requireLocationAccess(user, destinationLocationId);
        return ApiResponse.success(procurementService.createGoodsReceipt(request, user.userId()));
    }

    @GetMapping("/purchase-orders")
    public ApiResponse<java.util.Map<String, Object>> listPurchaseOrders(
            @AuthenticationPrincipal AuthenticatedUser user,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        permissionValidator.require(INVENTORY_TRANSFER);
        var orders = procurementService.listPurchaseOrders(page, size, locationAccessService.scopeFor(user));
        return ApiResponse.success(java.util.Map.of(
                "items", orders.getContent(),
                "page", orders.getNumber(),
                "size", orders.getSize(),
                "totalElements", orders.getTotalElements(),
                "totalPages", orders.getTotalPages()
        ));
    }
}
