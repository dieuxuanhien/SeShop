package com.seshop.pos.api;

import com.seshop.pos.api.dto.ReceiptDto;
import com.seshop.pos.application.ReceiptService;
import org.springframework.http.ResponseEntity;
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
}
