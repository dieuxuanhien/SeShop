package com.seshop.refund.api;

import com.seshop.refund.api.dto.CreateRefundRequest;
import com.seshop.refund.api.dto.CreateReturnRequest;
import com.seshop.refund.api.dto.RefundDto;
import com.seshop.refund.api.dto.ReturnDto;
import com.seshop.refund.application.RefundService;
import com.seshop.shared.security.PermissionValidator;
import jakarta.validation.Valid;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
public class RefundController {

    private static final String REFUND_PROCESS = "refund.process";

    private final RefundService refundService;
    private final PermissionValidator permissionValidator;

    public RefundController(RefundService refundService, PermissionValidator permissionValidator) {
        this.refundService = refundService;
        this.permissionValidator = permissionValidator;
    }

    @PostMapping("/returns")
    public ResponseEntity<Map<String, Object>> createReturn(@Valid @RequestBody CreateReturnRequest request) {
        permissionValidator.require(REFUND_PROCESS);
        ReturnDto dto = refundService.createReturn(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("data", dto));
    }

    @PostMapping("/returns/{returnId}/approve")
    public ResponseEntity<Map<String, Object>> approveReturn(@PathVariable Long returnId) {
        permissionValidator.require(REFUND_PROCESS);
        ReturnDto dto = refundService.approveReturn(returnId);
        return ResponseEntity.ok(Map.of("data", dto));
    }

    @PostMapping("/refunds")
    public ResponseEntity<Map<String, Object>> createRefund(@Valid @RequestBody CreateRefundRequest request) {
        permissionValidator.require(REFUND_PROCESS);
        RefundDto dto = refundService.createRefund(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("data", dto));
    }

    @GetMapping("/refunds/{refundId}")
    public ResponseEntity<Map<String, Object>> getRefund(@PathVariable Long refundId) {
        permissionValidator.require(REFUND_PROCESS);
        RefundDto dto = refundService.getRefund(refundId);
        return ResponseEntity.ok(Map.of("data", dto));
    }
}
