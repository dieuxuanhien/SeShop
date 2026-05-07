package com.seshop.commerce.api;

import com.seshop.commerce.api.dto.OrderDto;
import com.seshop.commerce.application.OrderService;
import com.seshop.shared.security.AuthenticatedUser;
import jakarta.validation.constraints.Min;
import java.util.HashMap;
import java.util.Map;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/orders")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @GetMapping("/me")
    public ResponseEntity<Map<String, Object>> listMyOrders(
            @AuthenticationPrincipal AuthenticatedUser user,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) int size
    ) {
        Page<OrderDto> orders = orderService.listOrdersForCustomer(user.userId(), page, size);
        Map<String, Object> response = new HashMap<>();
        response.put("data", Map.of(
                "items", orders.getContent(),
                "page", orders.getNumber(),
                "size", orders.getSize(),
                "totalElements", orders.getTotalElements(),
                "totalPages", orders.getTotalPages()
        ));
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{orderId}")
    public ResponseEntity<Map<String, Object>> getOrder(
            @AuthenticationPrincipal AuthenticatedUser user,
            @PathVariable Long orderId
    ) {
        OrderDto order = orderService.getOrderForCustomer(user.userId(), orderId);
        Map<String, Object> response = new HashMap<>();
        response.put("data", order);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{orderId}/track-shipment")
    public ResponseEntity<Map<String, Object>> trackShipment(@PathVariable Long orderId) {
        String status = orderService.refreshShipmentStatus(orderId);
        Map<String, Object> response = new HashMap<>();
        response.put("data", Map.of("status", status, "trackingNumbers", orderService.getTrackingNumbers(orderId)));
        return ResponseEntity.ok(response);
    }
}
