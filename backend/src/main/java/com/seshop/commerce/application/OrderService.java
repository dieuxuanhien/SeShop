package com.seshop.commerce.application;

import com.seshop.commerce.api.dto.CheckoutRequest;
import com.seshop.commerce.api.dto.OrderDto;
import com.seshop.commerce.api.dto.ProcessOrderRequest;
import com.seshop.commerce.infrastructure.persistence.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional
public class OrderService {

    private final OrderRepository orderRepository;
    private final CartRepository cartRepository;
    private final PaymentRepository paymentRepository;

    public OrderService(OrderRepository orderRepository,
                        CartRepository cartRepository,
                        PaymentRepository paymentRepository) {
        this.orderRepository = orderRepository;
        this.cartRepository = cartRepository;
        this.paymentRepository = paymentRepository;
    }

    public OrderDto checkout(Long customerId, CheckoutRequest request) {
        CartEntity cart = cartRepository.findById(request.getCartId())
                .orElseThrow(() -> new IllegalArgumentException("Cart not found"));

        if (!cart.getCustomerId().equals(customerId)) {
            throw new IllegalStateException("Cart does not belong to the user");
        }

        if (cart.getItems().isEmpty()) {
            throw new IllegalStateException("Cart is empty");
        }

        OrderEntity order = new OrderEntity();
        order.setCustomerId(customerId);
        order.setOrderNumber("ORD-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        order.setStatus("PENDING");
        order.setShippingAddress(request.getShippingAddress());
        order.setBillingAddress(request.getBillingAddress());

        BigDecimal subtotal = BigDecimal.ZERO;
        
        for (CartItemEntity cartItem : cart.getItems()) {
            OrderItemEntity orderItem = new OrderItemEntity();
            orderItem.setOrder(order);
            orderItem.setVariantId(cartItem.getVariantId());
            // Normally, product name, sku, and price would be fetched from Catalog module.
            // For now, mocking with default values or dummy fetch logic.
            orderItem.setProductName("Product Name");
            orderItem.setSku("SKU-123");
            orderItem.setQty(cartItem.getQty());
            orderItem.setUnitPrice(new BigDecimal("99.99"));
            orderItem.setTotalPrice(orderItem.getUnitPrice().multiply(new BigDecimal(cartItem.getQty())));
            
            subtotal = subtotal.add(orderItem.getTotalPrice());
            order.getItems().add(orderItem);
        }

        order.setSubtotalAmount(subtotal);
        // Implement promotion logic here if request.getPromotionCode() is present
        order.setDiscountAmount(BigDecimal.ZERO); 
        order.setTaxAmount(subtotal.multiply(new BigDecimal("0.1"))); // 10% tax
        order.setTotalAmount(subtotal.subtract(order.getDiscountAmount()).add(order.getTaxAmount()));

        OrderEntity savedOrder = orderRepository.save(order);

        // Process Payment logic
        PaymentEntity payment = new PaymentEntity();
        payment.setOrder(savedOrder);
        payment.setAmount(savedOrder.getTotalAmount());
        payment.setProvider(request.getPaymentProvider());
        payment.setStatus("COMPLETED"); // Assuming instant success for mock
        payment.setTransactionId(UUID.randomUUID().toString());
        paymentRepository.save(payment);

        savedOrder.setStatus("CONFIRMED"); // Update order status based on payment success
        cart.setStatus("COMPLETED");
        cartRepository.save(cart);

        return mapToDto(savedOrder);
    }

    public OrderDto getOrder(Long orderId) {
        OrderEntity order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Order not found"));
        return mapToDto(order);
    }

    public OrderDto processOrder(Long orderId, ProcessOrderRequest request) {
        OrderEntity order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Order not found"));

        switch (request.getAction()) {
            case "CONFIRM":
                order.setStatus("CONFIRMED");
                break;
            case "SHIP":
                order.setStatus("SHIPPED");
                break;
            case "DELIVER":
                order.setStatus("DELIVERED");
                break;
            case "CANCEL":
                order.setStatus("CANCELLED");
                break;
            default:
                throw new IllegalArgumentException("Invalid action");
        }

        return mapToDto(orderRepository.save(order));
    }

    private OrderDto mapToDto(OrderEntity entity) {
        OrderDto dto = new OrderDto();
        dto.setId(entity.getId());
        dto.setOrderNumber(entity.getOrderNumber());
        dto.setStatus(entity.getStatus());
        dto.setTotalAmount(entity.getTotalAmount());
        dto.setShippingAddress(entity.getShippingAddress());
        dto.setItems(entity.getItems().stream().map(item -> {
            OrderDto.OrderItemDto itemDto = new OrderDto.OrderItemDto();
            itemDto.setId(item.getId());
            itemDto.setVariantId(item.getVariantId());
            itemDto.setProductName(item.getProductName());
            itemDto.setQty(item.getQty());
            itemDto.setUnitPrice(item.getUnitPrice());
            itemDto.setTotalPrice(item.getTotalPrice());
            return itemDto;
        }).collect(Collectors.toList()));
        return dto;
    }
}
