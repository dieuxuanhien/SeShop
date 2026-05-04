package com.seshop.commerce.application;

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

    public CartService(CartRepository cartRepository) {
        this.cartRepository = cartRepository;
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
            return itemDto;
        }).collect(Collectors.toList()));
        return dto;
    }
}
