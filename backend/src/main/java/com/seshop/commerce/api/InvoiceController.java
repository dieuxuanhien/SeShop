package com.seshop.commerce.api;

import com.seshop.commerce.api.dto.CreateInvoiceAdjustmentRequest;
import com.seshop.commerce.api.dto.CreateTaxInvoiceRequest;
import com.seshop.commerce.application.InvoiceService;
import jakarta.validation.Valid;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/invoices")
public class InvoiceController {

    private final InvoiceService invoiceService;

    public InvoiceController(InvoiceService invoiceService) {
        this.invoiceService = invoiceService;
    }

    @PostMapping("/tax")
    public ResponseEntity<Map<String, Object>> createTaxInvoice(@Valid @RequestBody CreateTaxInvoiceRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("data", invoiceService.createTaxInvoice(request)));
    }

    @PostMapping("/{invoiceId}/adjustments")
    public ResponseEntity<Map<String, Object>> createAdjustment(
            @PathVariable Long invoiceId,
            @Valid @RequestBody CreateInvoiceAdjustmentRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("data", invoiceService.createAdjustment(invoiceId, request)));
    }
}
