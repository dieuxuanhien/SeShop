package com.seshop.commerce.api;

import com.seshop.commerce.api.dto.AddToCartRequest;
import com.seshop.commerce.api.dto.CartDto;
import com.seshop.commerce.application.CartService;
import com.seshop.shared.api.ApiResponse;
import com.seshop.shared.security.AuthenticatedUser;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1")
public class CartController {

    private final CartService cartService;

    public CartController(CartService cartService) {
        this.cartService = cartService;
    }

    @GetMapping({"/cart", "/carts/me"})
    public ApiResponse<CartDto> getCart(@AuthenticationPrincipal AuthenticatedUser user) {
        CartDto cart = cartService.getActiveCart(user.userId());
        return ApiResponse.success(cart);
    }

    @PostMapping({"/cart/items", "/carts/me/items"})
    public ResponseEntity<ApiResponse<CartDto>> addToCart(
            @AuthenticationPrincipal AuthenticatedUser user,
            @Valid @RequestBody AddToCartRequest request
    ) {
        CartDto cart = cartService.addToCart(user.userId(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(cart));
    }

    @PatchMapping("/carts/me/items/{itemId}")
    public ApiResponse<CartDto> updateCartItem(
            @AuthenticationPrincipal AuthenticatedUser user,
            @PathVariable Long itemId,
            @RequestBody Map<String, @Positive Integer> request) {
        CartDto cart = cartService.updateItem(user.userId(), itemId, request.get("qty"));
        return ApiResponse.success(cart);
    }

    @DeleteMapping("/carts/me/items/{itemId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void removeCartItem(@AuthenticationPrincipal AuthenticatedUser user, @PathVariable Long itemId) {
        cartService.removeItem(user.userId(), itemId);
    }
}
