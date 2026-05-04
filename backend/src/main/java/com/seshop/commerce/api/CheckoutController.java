package com.seshop.commerce.api;

import com.seshop.commerce.api.dto.CheckoutRequest;
import com.seshop.commerce.api.dto.OrderDto;
import com.seshop.commerce.application.OrderService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/checkout")
public class CheckoutController {

    private final OrderService orderService;

    public CheckoutController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping
    public ResponseEntity<Map<String, Object>> checkout(@Valid @RequestBody CheckoutRequest request) {
        // In a real application, customerId would come from the authenticated user context
        Long customerId = 1L; 
        OrderDto order = orderService.checkout(customerId, request);

        Map<String, Object> response = new HashMap<>();
        response.put("data", order);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
