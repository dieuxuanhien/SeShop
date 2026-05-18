package com.seshop.commerce.api;

import com.seshop.commerce.api.dto.OrderDto;
import com.seshop.commerce.api.dto.ProcessOrderRequest;
import com.seshop.commerce.api.dto.ShipOrderRequest;
import com.seshop.commerce.application.OrderService;
import com.seshop.shared.security.AuthenticatedUser;
import com.seshop.shared.security.LocationAccessService;
import com.seshop.shared.security.PermissionValidator;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/staff/orders")
public class StaffOrderController {

    private static final String ORDER_READ = "order.read";
    private static final String ORDER_SHIP = "order.ship";

    private final OrderService orderService;
    private final PermissionValidator permissionValidator;
    private final LocationAccessService locationAccessService;

    public StaffOrderController(
            OrderService orderService,
            PermissionValidator permissionValidator,
            LocationAccessService locationAccessService) {
        this.orderService = orderService;
        this.permissionValidator = permissionValidator;
        this.locationAccessService = locationAccessService;
    }

    @GetMapping
    public ResponseEntity<Map<String, Object>> listOrders(
            @AuthenticationPrincipal AuthenticatedUser user,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        permissionValidator.require(ORDER_READ);
        Page<OrderDto> orders = orderService.listOrdersForStaff(page, size, locationAccessService.scopeFor(user));
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
            @PathVariable Long orderId) {
        permissionValidator.require(ORDER_READ);
        OrderDto order = orderService.getOrder(orderId, locationAccessService.scopeFor(user));

        Map<String, Object> response = new HashMap<>();
        response.put("data", order);

        return ResponseEntity.ok(response);
    }

    @PostMapping("/{orderId}/process")
    public ResponseEntity<Map<String, Object>> processOrder(
            @AuthenticationPrincipal AuthenticatedUser user,
            @PathVariable Long orderId, 
            @Valid @RequestBody ProcessOrderRequest request) {
        permissionValidator.require(ORDER_READ);
        
        OrderDto order = orderService.processOrder(orderId, request, locationAccessService.scopeFor(user));

        Map<String, Object> response = new HashMap<>();
        response.put("data", order);

        return ResponseEntity.ok(response);
    }

    @PostMapping("/{orderId}/allocate")
    public ResponseEntity<Map<String, Object>> allocateOrder(
            @AuthenticationPrincipal AuthenticatedUser user,
            @PathVariable Long orderId) {
        permissionValidator.require(ORDER_READ);
        OrderDto order = orderService.allocateOrder(orderId, locationAccessService.scopeFor(user));
        Map<String, Object> response = new HashMap<>();
        response.put("data", order);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{orderId}/pack")
    public ResponseEntity<Map<String, Object>> packOrder(
            @AuthenticationPrincipal AuthenticatedUser user,
            @PathVariable Long orderId) {
        permissionValidator.require(ORDER_READ);
        OrderDto order = orderService.packOrder(orderId, locationAccessService.scopeFor(user));
        Map<String, Object> response = new HashMap<>();
        response.put("data", order);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{orderId}/ship")
    public ResponseEntity<Map<String, Object>> shipOrder(
            @AuthenticationPrincipal AuthenticatedUser user,
            @PathVariable Long orderId,
            @Valid @RequestBody ShipOrderRequest request) {
        permissionValidator.require(ORDER_SHIP);
        OrderDto order = orderService.shipOrder(
                orderId,
                request.getCarrier(),
                request.getRecipientName(),
                request.getRecipientPhone(),
                request.getTrackingNumber(),
                locationAccessService.scopeFor(user));
        Map<String, Object> response = new HashMap<>();
        response.put("data", order);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{orderId}/cancel")
    public ResponseEntity<Map<String, Object>> cancelOrder(
            @AuthenticationPrincipal AuthenticatedUser user,
            @PathVariable Long orderId) {
        permissionValidator.require(ORDER_READ);
        OrderDto order = orderService.cancelOrder(orderId, locationAccessService.scopeFor(user));
        Map<String, Object> response = new HashMap<>();
        response.put("data", order);
        return ResponseEntity.ok(response);
    }
}
