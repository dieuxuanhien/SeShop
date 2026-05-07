package com.seshop.commerce.application;

import com.seshop.commerce.api.dto.CheckoutRequest;
import com.seshop.commerce.api.dto.CheckoutResponse;
import com.seshop.commerce.api.dto.OrderDto;
import com.seshop.commerce.api.dto.ProcessOrderRequest;
import com.seshop.commerce.infrastructure.persistence.*;
import com.seshop.catalog.infrastructure.persistence.ProductVariantEntity;
import com.seshop.catalog.infrastructure.persistence.ProductVariantRepository;
import com.seshop.payment.infrastructure.StripeClient;
import com.seshop.shipping.infrastructure.GhnClient;
import com.seshop.shipping.infrastructure.persistence.ShipmentEntity;
import com.seshop.shipping.infrastructure.persistence.ShipmentRepository;
import com.seshop.shared.exception.BusinessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional
public class OrderService {

    private final OrderRepository orderRepository;
    private final CartRepository cartRepository;
    private final PaymentRepository paymentRepository;
    private final StripeClient stripeClient;
    private final GhnClient ghnClient;
    private final ShipmentRepository shipmentRepository;
    private final ProductVariantRepository productVariantRepository;

    public OrderService(OrderRepository orderRepository,
                        CartRepository cartRepository,
                        PaymentRepository paymentRepository,
                        StripeClient stripeClient,
                        GhnClient ghnClient,
                        ShipmentRepository shipmentRepository,
                        ProductVariantRepository productVariantRepository) {
        this.orderRepository = orderRepository;
        this.cartRepository = cartRepository;
        this.paymentRepository = paymentRepository;
        this.stripeClient = stripeClient;
        this.ghnClient = ghnClient;
        this.shipmentRepository = shipmentRepository;
        this.productVariantRepository = productVariantRepository;
    }

    public CheckoutResponse checkout(Long customerId, CheckoutRequest request) {
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
        order.setShippingAddress(request.shippingAddressText());
        order.setBillingAddress(request.billingAddressText());

        BigDecimal subtotal = BigDecimal.ZERO;
        
        for (CartItemEntity cartItem : cart.getItems()) {
            OrderItemEntity orderItem = new OrderItemEntity();
            orderItem.setOrder(order);
            orderItem.setVariantId(cartItem.getVariantId());
            ProductVariantEntity variant = productVariantRepository.findById(cartItem.getVariantId())
                    .orElseThrow(() -> new BusinessException("CAT_002", "Product variant not found"));
            orderItem.setProductName(variant.getProduct().getName());
            orderItem.setSku(variant.getSkuCode());
            orderItem.setQty(cartItem.getQty());
            orderItem.setUnitPrice(variant.getPrice());
            orderItem.setTotalPrice(orderItem.getUnitPrice().multiply(new BigDecimal(cartItem.getQty())));
            
            subtotal = subtotal.add(orderItem.getTotalPrice());
            order.getItems().add(orderItem);
        }

        order.setSubtotalAmount(subtotal);
        order.setDiscountAmount(BigDecimal.ZERO); 
        order.setTaxAmount(subtotal.multiply(new BigDecimal("0.1")));
        order.setTotalAmount(subtotal.subtract(order.getDiscountAmount()).add(order.getTaxAmount()));

        OrderEntity savedOrder = orderRepository.save(order);

        PaymentEntity payment = new PaymentEntity();
        payment.setOrder(savedOrder);
        payment.setAmount(savedOrder.getTotalAmount());
        String provider = request.resolvePaymentProvider();
        if (provider == null || provider.isBlank()) {
            throw new BusinessException("PAY_002", "Payment provider is required");
        }
        provider = provider.toUpperCase();
        payment.setProvider(provider);

        if ("STRIPE".equals(provider)) {
            StripeClient.StripePaymentResult result = stripeClient.createPaymentIntent(
                    savedOrder.getTotalAmount(),
                    UUID.randomUUID().toString(),
                    savedOrder.getOrderNumber()
            );
            payment.setStatus(normalizeStripeStatus(result.status()));
            payment.setTransactionId(result.transactionId());
            savedOrder.setStatus("PAYMENT_PENDING");
        } else if ("COD".equals(provider)) {
            payment.setStatus("PENDING");
            payment.setTransactionId("COD-" + UUID.randomUUID());
            savedOrder.setStatus("CONFIRMED");
        } else {
            throw new BusinessException("PAY_002", "Unsupported payment provider");
        }
        paymentRepository.save(payment);

        cart.setStatus("COMPLETED");
        cartRepository.save(cart);

        CheckoutResponse response = new CheckoutResponse();
        response.setOrderId(savedOrder.getId());
        response.setOrderNumber(savedOrder.getOrderNumber());
        response.setPaymentStatus(payment.getStatus());
        response.setShipmentStatus("PENDING");
        return response;
    }

    @Transactional(readOnly = true)
    public Page<OrderDto> listOrdersForCustomer(Long customerId, int page, int size) {
        return orderRepository.findByCustomerId(customerId, PageRequest.of(page, size)).map(this::mapToDto);
    }

    @Transactional(readOnly = true)
    public Page<OrderDto> listOrdersForStaff(int page, int size) {
        return orderRepository.findAll(PageRequest.of(page, size)).map(this::mapToDto);
    }

    @Transactional(readOnly = true)
    public OrderDto getOrderForCustomer(Long customerId, Long orderId) {
        OrderEntity order = orderRepository.findById(orderId)
                .filter(item -> item.getCustomerId().equals(customerId))
                .orElseThrow(() -> new BusinessException("ORD_002", "Order not found"));
        return mapToDto(order);
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

    public OrderDto allocateOrder(Long orderId) {
        OrderEntity order = orderRepository.findById(orderId)
                .orElseThrow(() -> new BusinessException("ORD_002", "Order not found"));
        order.setStatus("ALLOCATED");
        return mapToDto(orderRepository.save(order));
    }

    public OrderDto packOrder(Long orderId) {
        OrderEntity order = orderRepository.findById(orderId)
                .orElseThrow(() -> new BusinessException("ORD_002", "Order not found"));
        order.setStatus("PACKED");
        return mapToDto(orderRepository.save(order));
    }

    public OrderDto shipOrder(Long orderId, String carrier, String toName, String toPhone, String trackingNumber) {
        OrderEntity order = orderRepository.findById(orderId)
                .orElseThrow(() -> new BusinessException("ORD_002", "Order not found"));

        ShipmentEntity shipment = shipmentRepository.findByOrderId(orderId).orElse(new ShipmentEntity());
        shipment.setOrderId(orderId);
        shipment.setCarrier(carrier);

        if (trackingNumber != null && !trackingNumber.isBlank()) {
            shipment.setTrackingNumber(trackingNumber);
            shipment.setStatus("SHIPPED");
        } else if ("GHN".equalsIgnoreCase(carrier)) {
            GhnClient.GhnShipmentResult result = ghnClient.createShippingOrder(
                    order.getOrderNumber(),
                    toName,
                    toPhone,
                    order.getShippingAddress()
            );
            shipment.setTrackingNumber(result.trackingNumber());
            shipment.setStatus(result.status());
        } else {
            shipment.setTrackingNumber("LOCAL-" + UUID.randomUUID());
            shipment.setStatus("SHIPPED");
        }
        shipment.setShippedAt(OffsetDateTime.now());
        shipmentRepository.save(shipment);

        order.setStatus("SHIPPED");
        return mapToDto(orderRepository.save(order));
    }

    public OrderDto cancelOrder(Long orderId) {
        OrderEntity order = orderRepository.findById(orderId)
                .orElseThrow(() -> new BusinessException("ORD_002", "Order not found"));
        order.setStatus("CANCELLED");
        return mapToDto(orderRepository.save(order));
    }

    public String refreshShipmentStatus(Long orderId) {
        ShipmentEntity shipment = shipmentRepository.findByOrderId(orderId)
                .orElseThrow(() -> new BusinessException("ORD_002", "Shipment not found"));
        String status = ghnClient.getShippingStatus(shipment.getTrackingNumber());
        shipment.setStatus(status);
        if ("delivered".equalsIgnoreCase(status)) {
            shipment.setDeliveredAt(OffsetDateTime.now());
        }
        shipmentRepository.save(shipment);
        return status;
    }

    private String normalizeStripeStatus(String stripeStatus) {
        if (stripeStatus == null) {
            return "PENDING";
        }
        return switch (stripeStatus) {
            case "succeeded" -> "COMPLETED";
            case "requires_payment_method", "requires_confirmation", "requires_action" -> "PENDING";
            default -> "PENDING";
        };
    }

    @Transactional(readOnly = true)
    public List<String> getTrackingNumbers(Long orderId) {
        return shipmentRepository.findByOrderId(orderId)
                .map(item -> List.of(item.getTrackingNumber()))
                .orElse(List.of());
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
