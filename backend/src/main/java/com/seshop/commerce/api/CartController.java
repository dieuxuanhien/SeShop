package com.seshop.commerce.api;

import com.seshop.commerce.api.dto.AddToCartRequest;
import com.seshop.commerce.api.dto.CartDto;
import com.seshop.commerce.application.CartService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/cart")
public class CartController {

    private final CartService cartService;

    public CartController(CartService cartService) {
        this.cartService = cartService;
    }

    @GetMapping
    public ResponseEntity<Map<String, Object>> getCart() {
        // In a real application, customerId would come from the authenticated user context
        Long customerId = 1L; 
        CartDto cart = cartService.getActiveCart(customerId);

        Map<String, Object> response = new HashMap<>();
        response.put("data", cart);

        return ResponseEntity.ok(response);
    }

    @PostMapping("/items")
    public ResponseEntity<Map<String, Object>> addToCart(@Valid @RequestBody AddToCartRequest request) {
        // In a real application, customerId would come from the authenticated user context
        Long customerId = 1L; 
        CartDto cart = cartService.addToCart(customerId, request);

        Map<String, Object> response = new HashMap<>();
        response.put("data", cart);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
