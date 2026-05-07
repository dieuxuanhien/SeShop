package com.seshop.commerce.application;

import com.seshop.catalog.infrastructure.persistence.ProductVariantEntity;
import com.seshop.catalog.infrastructure.persistence.ProductVariantRepository;
import com.seshop.commerce.api.dto.AddToCartRequest;
import com.seshop.commerce.api.dto.CartDto;
import com.seshop.commerce.infrastructure.persistence.CartEntity;
import com.seshop.commerce.infrastructure.persistence.CartItemEntity;
import com.seshop.commerce.infrastructure.persistence.CartRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.stream.Collectors;

@Service
@Transactional
public class CartService {

    private final CartRepository cartRepository;
    private final ProductVariantRepository productVariantRepository;

    public CartService(CartRepository cartRepository, ProductVariantRepository productVariantRepository) {
        this.cartRepository = cartRepository;
        this.productVariantRepository = productVariantRepository;
    }

    public CartDto getActiveCart(Long customerId) {
        CartEntity cart = cartRepository.findByCustomerIdAndStatus(customerId, "ACTIVE")
                .orElseGet(() -> {
                    CartEntity newCart = new CartEntity();
                    newCart.setCustomerId(customerId);
                    newCart.setStatus("ACTIVE");
                    return cartRepository.save(newCart);
                });
        return mapToDto(cart);
    }

    public CartDto addToCart(Long customerId, AddToCartRequest request) {
        CartEntity cart = cartRepository.findByCustomerIdAndStatus(customerId, "ACTIVE")
                .orElseGet(() -> {
                    CartEntity newCart = new CartEntity();
                    newCart.setCustomerId(customerId);
                    newCart.setStatus("ACTIVE");
                    return cartRepository.save(newCart);
                });

        CartItemEntity item = cart.getItems().stream()
                .filter(i -> i.getVariantId().equals(request.getVariantId()))
                .findFirst()
                .orElseGet(() -> {
                    CartItemEntity newItem = new CartItemEntity();
                    newItem.setCart(cart);
                    newItem.setVariantId(request.getVariantId());
                    newItem.setQty(0);
                    cart.getItems().add(newItem);
                    return newItem;
                });

        item.setQty(item.getQty() + request.getQty());
        
        CartEntity saved = cartRepository.save(cart);
        return mapToDto(saved);
    }

    public CartDto updateItem(Long customerId, Long itemId, Integer qty) {
        CartEntity cart = activeCart(customerId);
        CartItemEntity item = cart.getItems().stream()
                .filter(candidate -> candidate.getId().equals(itemId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Cart item not found"));
        item.setQty(qty);
        return mapToDto(cartRepository.save(cart));
    }

    public void removeItem(Long customerId, Long itemId) {
        CartEntity cart = activeCart(customerId);
        boolean removed = cart.getItems().removeIf(item -> item.getId().equals(itemId));
        if (!removed) {
            throw new IllegalArgumentException("Cart item not found");
        }
        cartRepository.save(cart);
    }

    private CartEntity activeCart(Long customerId) {
        return cartRepository.findByCustomerIdAndStatus(customerId, "ACTIVE")
                .orElseThrow(() -> new IllegalArgumentException("Active cart not found"));
    }

    private CartDto mapToDto(CartEntity entity) {
        CartDto dto = new CartDto();
        dto.setId(entity.getId());
        dto.setCustomerId(entity.getCustomerId());
        dto.setStatus(entity.getStatus());
        dto.setItems(entity.getItems().stream().map(item -> {
            CartDto.CartItemDto itemDto = new CartDto.CartItemDto();
            itemDto.setId(item.getId());
            itemDto.setVariantId(item.getVariantId());
            itemDto.setQty(item.getQty());
            ProductVariantEntity variant = productVariantRepository.findById(item.getVariantId())
                    .orElseThrow(() -> new IllegalArgumentException("Variant not found"));
            itemDto.setSkuCode(variant.getSkuCode());
            itemDto.setName(variant.getProduct().getName());
            itemDto.setUnitPrice(variant.getPrice());
            return itemDto;
        }).collect(Collectors.toList()));
        return dto;
    }
}
