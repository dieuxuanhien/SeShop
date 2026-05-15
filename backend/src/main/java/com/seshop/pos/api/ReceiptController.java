package com.seshop.pos.api;

import com.seshop.pos.api.dto.ProcessPosSaleRequest;
import com.seshop.pos.api.dto.ProcessPosSaleResponse;
import com.seshop.pos.api.dto.ReceiptDto;
import com.seshop.pos.application.ReceiptService;
import com.seshop.shared.api.ApiResponse;
import com.seshop.shared.api.PageResponse;
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
@RequestMapping("/api/v1/pos/receipts")
public class ReceiptController {

    private static final String POS_SELL = "pos.sell";

    private final ReceiptService receiptService;
    private final PermissionValidator permissionValidator;

    public ReceiptController(ReceiptService receiptService, PermissionValidator permissionValidator) {
        this.receiptService = receiptService;
        this.permissionValidator = permissionValidator;
    }

    @GetMapping
    public ApiResponse<PageResponse<ReceiptDto>> listReceipts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        permissionValidator.require(POS_SELL);
        return ApiResponse.success(receiptService.listReceipts(page, size));
    }

    @GetMapping(params = "receiptNumber")
    public ResponseEntity<Map<String, Object>> getReceiptByNumber(@RequestParam String receiptNumber) {
        permissionValidator.require(POS_SELL);
        ReceiptDto receipt = receiptService.getReceipt(receiptNumber);

        Map<String, Object> response = new HashMap<>();
        response.put("data", receipt);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{receiptId}")
    public ApiResponse<ReceiptDto> getReceipt(@PathVariable Long receiptId) {
        permissionValidator.require(POS_SELL);
        return ApiResponse.success(receiptService.getReceipt(receiptId));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<ProcessPosSaleResponse> createReceipt(
            @AuthenticationPrincipal AuthenticatedUser user,
            @Valid @RequestBody ProcessPosSaleRequest request) {
        permissionValidator.require(POS_SELL);
        return ApiResponse.success(receiptService.createReceipt(request, user.userId()));
    }
}
