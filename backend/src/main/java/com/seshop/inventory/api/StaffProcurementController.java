package com.seshop.inventory.api;

import com.seshop.inventory.api.dto.CreatePurchaseOrderRequest;
import com.seshop.inventory.api.dto.GoodsReceiptRequest;
import com.seshop.inventory.api.dto.GoodsReceiptResponse;
import com.seshop.inventory.api.dto.PurchaseOrderResponse;
import com.seshop.inventory.application.ProcurementService;
import com.seshop.shared.api.ApiResponse;
import com.seshop.shared.security.AuthenticatedUser;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/staff")
public class StaffProcurementController {

    private final ProcurementService procurementService;

    public StaffProcurementController(ProcurementService procurementService) {
        this.procurementService = procurementService;
    }

    @PostMapping("/purchase-orders")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<PurchaseOrderResponse> createPurchaseOrder(
            @AuthenticationPrincipal AuthenticatedUser user,
            @Valid @RequestBody CreatePurchaseOrderRequest request) {
        return ApiResponse.success(procurementService.createPurchaseOrder(request, user.userId()));
    }

    @PostMapping("/goods-receipts")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<GoodsReceiptResponse> createGoodsReceipt(
            @AuthenticationPrincipal AuthenticatedUser user,
            @Valid @RequestBody GoodsReceiptRequest request) {
        return ApiResponse.success(procurementService.createGoodsReceipt(request, user.userId()));
    }
}
