package com.seshop.commerce.api;

import com.seshop.commerce.api.dto.OrderDto;
import com.seshop.commerce.api.dto.ProcessOrderRequest;
import com.seshop.commerce.application.OrderService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/staff/orders")
public class StaffOrderController {

    private final OrderService orderService;

    public StaffOrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @GetMapping("/{orderId}")
    public ResponseEntity<Map<String, Object>> getOrder(@PathVariable Long orderId) {
        OrderDto order = orderService.getOrder(orderId);

        Map<String, Object> response = new HashMap<>();
        response.put("data", order);

        return ResponseEntity.ok(response);
    }

    @PostMapping("/{orderId}/process")
    public ResponseEntity<Map<String, Object>> processOrder(
            @PathVariable Long orderId, 
            @Valid @RequestBody ProcessOrderRequest request) {
        
        OrderDto order = orderService.processOrder(orderId, request);

        Map<String, Object> response = new HashMap<>();
        response.put("data", order);

        return ResponseEntity.ok(response);
    }
}
