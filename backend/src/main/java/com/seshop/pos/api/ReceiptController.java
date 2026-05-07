package com.seshop.pos.api;

import com.seshop.pos.api.dto.ProcessPosSaleRequest;
import com.seshop.pos.api.dto.ProcessPosSaleResponse;
import com.seshop.pos.api.dto.ReceiptDto;
import com.seshop.pos.application.ReceiptService;
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
@RequestMapping("/api/v1/pos/receipts")
public class ReceiptController {

    private final ReceiptService receiptService;

    public ReceiptController(ReceiptService receiptService) {
        this.receiptService = receiptService;
    }

    @GetMapping
    public ResponseEntity<Map<String, Object>> getReceiptByNumber(@RequestParam String receiptNumber) {
        ReceiptDto receipt = receiptService.getReceipt(receiptNumber);

        Map<String, Object> response = new HashMap<>();
        response.put("data", receipt);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{receiptId}")
    public ApiResponse<ReceiptDto> getReceipt(@PathVariable Long receiptId) {
        return ApiResponse.success(receiptService.getReceipt(receiptId));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<ProcessPosSaleResponse> createReceipt(
            @AuthenticationPrincipal AuthenticatedUser user,
            @Valid @RequestBody ProcessPosSaleRequest request) {
        return ApiResponse.success(receiptService.createReceipt(request, user.userId()));
    }
}
