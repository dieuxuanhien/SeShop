package com.seshop.commerce.application;

import com.seshop.audit.application.AuditService;
import com.seshop.audit.domain.AuditAction;
import com.seshop.commerce.api.dto.CheckoutRequest;
import com.seshop.commerce.api.dto.CheckoutResponse;
import com.seshop.commerce.api.dto.OrderDto;
import com.seshop.commerce.api.dto.ProcessOrderRequest;
import com.seshop.commerce.infrastructure.persistence.*;
import com.seshop.catalog.infrastructure.persistence.ProductVariantEntity;
import com.seshop.catalog.infrastructure.persistence.ProductVariantRepository;
import com.seshop.inventory.infrastructure.persistence.InventoryBalanceEntity;
import com.seshop.inventory.infrastructure.persistence.InventoryBalanceRepository;
import com.seshop.marketing.api.dto.DiscountValidationResponse;
import com.seshop.marketing.application.DiscountService;
import com.seshop.payment.infrastructure.StripeClient;
import com.seshop.shipping.domain.ShipmentStatuses;
import com.seshop.shipping.infrastructure.GhnClient;
import com.seshop.shipping.infrastructure.persistence.ShipmentEntity;
import com.seshop.shipping.infrastructure.persistence.ShipmentRepository;
import com.seshop.shared.exception.BusinessException;
import com.seshop.shared.exception.ForbiddenOperationException;
import com.seshop.shared.security.LocationScope;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional
public class OrderService {

    private static final String STATUS_ALLOCATED = "ALLOCATED";
    private static final String STATUS_CANCELLED = "CANCELLED";
    private static final String STATUS_CONFIRMED = "CONFIRMED";
    private static final String STATUS_DELIVERED = "DELIVERED";
    private static final String STATUS_FULFILLED = "FULFILLED";
    private static final String STATUS_PACKED = "PACKED";
    private static final String STATUS_PAID = "PAID";
    private static final String STATUS_PAYMENT_FAILED = "PAYMENT_FAILED";
    private static final String STATUS_PENDING = "PENDING";
    private static final String STATUS_PENDING_PAYMENT = "PENDING_PAYMENT";
    private static final String STATUS_RELEASED = "RELEASED";
    private static final String STATUS_SHIPPED = "SHIPPED";
    private static final String PAYMENT_PROVIDER_COD = "COD";
    private static final String PAYMENT_STATUS_FAILED = "FAILED";
    private static final String PAYMENT_STATUS_PAID = "PAID";
    private static final String PAYMENT_STATUS_PENDING = "PENDING";
    private static final String SHIPMENT_STATUS_DELIVERED = "DELIVERED";
    private static final String SHIPMENT_STATUS_IN_TRANSIT = "IN_TRANSIT";
    private static final String SHIPMENT_STATUS_PENDING = "PENDING";
    private static final String SHIPMENT_STATUS_SHIPPED = "SHIPPED";
    private static final String DEFAULT_CURRENCY = "VND";
    private static final BigDecimal TAX_RATE = new BigDecimal("0.1");
    private static final Set<String> SHIPPABLE_ORDER_STATUSES =
            Set.of(STATUS_ALLOCATED, STATUS_CONFIRMED, STATUS_PACKED, STATUS_PAID);

    private final OrderRepository orderRepository;
    private final CartRepository cartRepository;
    private final PaymentRepository paymentRepository;
    private final StripeClient stripeClient;
    private final GhnClient ghnClient;
    private final ShipmentRepository shipmentRepository;
    private final ProductVariantRepository productVariantRepository;
    private final InventoryBalanceRepository balanceRepository;
    private final OrderAllocationRepository allocationRepository;
    private final PickTaskRepository pickTaskRepository;
    private final DiscountService discountService;
    private final AuditService auditService;

    public OrderService(OrderRepository orderRepository,
                        CartRepository cartRepository,
                        PaymentRepository paymentRepository,
                        StripeClient stripeClient,
                        GhnClient ghnClient,
                        ShipmentRepository shipmentRepository,
                        ProductVariantRepository productVariantRepository,
                        InventoryBalanceRepository balanceRepository,
                        OrderAllocationRepository allocationRepository,
                        PickTaskRepository pickTaskRepository,
                        DiscountService discountService,
                        AuditService auditService) {
        this.orderRepository = orderRepository;
        this.cartRepository = cartRepository;
        this.paymentRepository = paymentRepository;
        this.stripeClient = stripeClient;
        this.ghnClient = ghnClient;
        this.shipmentRepository = shipmentRepository;
        this.productVariantRepository = productVariantRepository;
        this.balanceRepository = balanceRepository;
        this.allocationRepository = allocationRepository;
        this.pickTaskRepository = pickTaskRepository;
        this.discountService = discountService;
        this.auditService = auditService;
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
        order.setStatus(STATUS_PENDING);
        order.setPaymentStatus(PAYMENT_STATUS_PENDING);
        order.setShipmentStatus(SHIPMENT_STATUS_PENDING);
        order.setCurrency(DEFAULT_CURRENCY);
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

        applyOrderTotals(order, subtotal, BigDecimal.ZERO);

        OrderEntity savedOrder = orderRepository.save(order);
        String discountCode = resolveDiscountCode(request);
        if (discountCode != null) {
            DiscountValidationResponse discount = discountService.redeemDiscount(
                    discountCode,
                    customerId,
                    savedOrder.getId(),
                    subtotal
            );
            applyOrderTotals(savedOrder, subtotal, discount.getDiscountAmount());
        }
        allocateStockForOrder(savedOrder);

        PaymentEntity payment = new PaymentEntity();
        payment.setOrder(savedOrder);
        payment.setAmount(savedOrder.getTotalAmount());
        String provider = request.resolvePaymentProvider();
        if (provider == null || provider.isBlank()) {
            throw new BusinessException("PAY_002", "Payment provider is required");
        }
        provider = provider.toUpperCase();
        payment.setProvider(provider);

        String clientSecret = null;
        if ("STRIPE".equals(provider)) {
            StripeClient.StripePaymentResult result = stripeClient.createPaymentIntent(
                    savedOrder.getTotalAmount(),
                    UUID.randomUUID().toString(),
                    savedOrder.getOrderNumber()
            );
            payment.setStatus(normalizeStripePaymentStatus(result.status()));
            payment.setTransactionId(result.transactionId());
            clientSecret = result.clientSecret();
            savedOrder.setPaymentStatus(payment.getStatus());
            savedOrder.setStatus(PAYMENT_STATUS_PAID.equals(payment.getStatus()) ? STATUS_PAID : STATUS_PENDING_PAYMENT);
        } else if (PAYMENT_PROVIDER_COD.equals(provider)) {
            payment.setStatus(PAYMENT_STATUS_PENDING);
            payment.setTransactionId("COD-" + UUID.randomUUID());
            savedOrder.setPaymentStatus(PAYMENT_STATUS_PENDING);
            savedOrder.setStatus(STATUS_CONFIRMED);
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
        response.setClientSecret(clientSecret);
        return response;
    }

    @Transactional(readOnly = true)
    public Page<OrderDto> listOrdersForCustomer(Long customerId, int page, int size) {
        return orderRepository.findByCustomerId(customerId, PageRequest.of(page, size)).map(this::mapToDto);
    }

    @Transactional(readOnly = true)
    public Page<OrderDto> listOrdersForStaff(int page, int size) {
        return listOrdersForStaff(page, size, LocationScope.all());
    }

    @Transactional(readOnly = true)
    public Page<OrderDto> listOrdersForStaff(int page, int size, LocationScope locationScope) {
        PageRequest pageRequest = PageRequest.of(page, size);
        if (locationScope.isEmpty()) {
            return new PageImpl<>(List.of(), pageRequest, 0);
        }
        Page<OrderEntity> orders = locationScope.allLocations()
                ? orderRepository.findAll(pageRequest)
                : orderRepository.findByAllocatedLocationIds(locationScope.locationIds(), pageRequest);
        return orders.map(this::mapToDto);
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

    public OrderDto getOrder(Long orderId, LocationScope locationScope) {
        requireOrderScope(orderId, locationScope);
        return getOrder(orderId);
    }

    public OrderDto processOrder(Long orderId, ProcessOrderRequest request) {
        return processOrder(orderId, request, LocationScope.all());
    }

    public OrderDto processOrder(Long orderId, ProcessOrderRequest request, LocationScope locationScope) {
        requireOrderScope(orderId, locationScope);
        OrderEntity order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Order not found"));

        switch (request.getAction()) {
            case "CONFIRM":
                order.setStatus(STATUS_CONFIRMED);
                break;
            case "SHIP":
                fulfillAllocatedStock(order);
                order.setStatus(STATUS_SHIPPED);
                order.setShipmentStatus(SHIPMENT_STATUS_SHIPPED);
                break;
            case "DELIVER":
                order.setStatus(STATUS_DELIVERED);
                order.setShipmentStatus(SHIPMENT_STATUS_DELIVERED);
                completeCodPaymentIfPending(order);
                break;
            case "CANCEL":
                releaseAllocatedStock(order);
                order.setStatus(STATUS_CANCELLED);
                break;
            default:
                throw new IllegalArgumentException("Invalid action");
        }

        return mapToDto(orderRepository.save(order));
    }

    public OrderDto allocateOrder(Long orderId) {
        return allocateOrder(orderId, LocationScope.all());
    }

    public OrderDto allocateOrder(Long orderId, LocationScope locationScope) {
        requireOrderScope(orderId, locationScope);
        OrderEntity order = orderRepository.findById(orderId)
                .orElseThrow(() -> new BusinessException("ORD_002", "Order not found"));
        allocateStockForOrder(order);
        return mapToDto(orderRepository.save(order));
    }

    public OrderDto packOrder(Long orderId) {
        return packOrder(orderId, LocationScope.all());
    }

    public OrderDto packOrder(Long orderId, LocationScope locationScope) {
        requireOrderScope(orderId, locationScope);
        OrderEntity order = orderRepository.findById(orderId)
                .orElseThrow(() -> new BusinessException("ORD_002", "Order not found"));
        order.setStatus(STATUS_PACKED);
        return mapToDto(orderRepository.save(order));
    }

    public OrderDto shipOrder(Long orderId, String carrier, String toName, String toPhone, String trackingNumber) {
        return shipOrder(orderId, carrier, toName, toPhone, trackingNumber, LocationScope.all());
    }

    public OrderDto shipOrder(
            Long orderId,
            String carrier,
            String toName,
            String toPhone,
            String trackingNumber,
            LocationScope locationScope) {
        requireOrderScope(orderId, locationScope);
        OrderEntity order = orderRepository.findById(orderId)
                .orElseThrow(() -> new BusinessException("ORD_002", "Order not found"));
        validateOrderCanShip(order);
        String normalizedCarrier = ShipmentStatuses.normalizeCarrier(carrier)
                .orElseThrow(() -> new BusinessException("ORD_003", "Carrier is required"));
        String providedTrackingNumber = ShipmentStatuses.normalizeTrackingNumber(trackingNumber)
                .orElse(null);
        if (trackingNumber != null && !trackingNumber.isBlank() && providedTrackingNumber == null) {
            throw new BusinessException("ORD_003", "Tracking number format is invalid");
        }
        if (providedTrackingNumber == null && "GHN".equals(normalizedCarrier)) {
            validateGhnShipmentRequest(toName, toPhone, order.getShippingAddress());
        }

        String previousOrderStatus = order.getStatus();
        String previousOrderShipmentStatus = order.getShipmentStatus();
        fulfillAllocatedStock(order);

        ShipmentEntity shipment = shipmentRepository.findByOrderId(orderId).orElse(new ShipmentEntity());
        boolean newShipment = shipment.getId() == null;
        String previousShipmentStatus = shipment.getStatus();
        String previousTrackingNumber = shipment.getTrackingNumber();

        shipment.setOrderId(orderId);
        shipment.setCarrier(normalizedCarrier);

        if (providedTrackingNumber != null) {
            shipment.setTrackingNumber(providedTrackingNumber);
            shipment.setStatus(SHIPMENT_STATUS_SHIPPED);
        } else if ("GHN".equals(normalizedCarrier)) {
            GhnClient.GhnShipmentResult result = ghnClient.createShippingOrder(
                    order.getOrderNumber(),
                    toName,
                    toPhone,
                    order.getShippingAddress()
            );
            shipment.setTrackingNumber(ShipmentStatuses.normalizeTrackingNumber(result.trackingNumber())
                    .orElseThrow(() -> new BusinessException("ORD_002", "GHN returned invalid tracking number")));
            shipment.setStatus(ShipmentStatuses.normalizeStatus(result.status()));
        } else {
            shipment.setTrackingNumber("LOCAL-" + UUID.randomUUID().toString().replace("-", "").substring(0, 16).toUpperCase());
            shipment.setStatus(SHIPMENT_STATUS_SHIPPED);
        }
        if (shipment.getShippedAt() == null) {
            shipment.setShippedAt(OffsetDateTime.now());
        }
        ShipmentEntity savedShipment = shipmentRepository.save(shipment);

        applyOrderShipmentStatus(order, shipment.getStatus());
        OrderEntity savedOrder = orderRepository.save(order);
        auditShipmentSaved(
                newShipment ? AuditAction.SHIPMENT_CREATED : AuditAction.SHIPMENT_STATUS_CHANGED,
                savedShipment,
                previousShipmentStatus,
                previousTrackingNumber,
                previousOrderStatus,
                previousOrderShipmentStatus,
                "STAFF_SHIP"
        );
        return mapToDto(savedOrder);
    }

    public OrderDto cancelOrder(Long orderId) {
        return cancelOrder(orderId, LocationScope.all());
    }

    public OrderDto cancelOrder(Long orderId, LocationScope locationScope) {
        requireOrderScope(orderId, locationScope);
        OrderEntity order = orderRepository.findById(orderId)
                .orElseThrow(() -> new BusinessException("ORD_002", "Order not found"));
        releaseAllocatedStock(order);
        order.setStatus(STATUS_CANCELLED);
        return mapToDto(orderRepository.save(order));
    }

    public OrderDto markPaymentFailed(Long orderId) {
        OrderEntity order = orderRepository.findById(orderId)
                .orElseThrow(() -> new BusinessException("ORD_002", "Order not found"));
        releaseAllocatedStock(order);
        order.setStatus(STATUS_PAYMENT_FAILED);
        order.setPaymentStatus(PAYMENT_STATUS_FAILED);
        return mapToDto(orderRepository.save(order));
    }

    public OrderDto markPaymentPaid(Long orderId) {
        OrderEntity order = orderRepository.findById(orderId)
                .orElseThrow(() -> new BusinessException("ORD_002", "Order not found"));
        order.setPaymentStatus(PAYMENT_STATUS_PAID);
        if (STATUS_PENDING_PAYMENT.equals(order.getStatus())) {
            order.setStatus(STATUS_PAID);
        }
        return mapToDto(orderRepository.save(order));
    }

    private List<OrderAllocationEntity> allocateStockForOrder(OrderEntity order) {
        if (STATUS_CANCELLED.equals(order.getStatus())
                || STATUS_SHIPPED.equals(order.getStatus())
                || STATUS_DELIVERED.equals(order.getStatus())) {
            throw new BusinessException("ORD_003", "Order cannot be allocated from its current state");
        }

        List<OrderAllocationEntity> activeAllocations =
                allocationRepository.findByOrderIdAndStatus(order.getId(), STATUS_ALLOCATED);
        if (!activeAllocations.isEmpty()) {
            order.setStatus(STATUS_ALLOCATED);
            return activeAllocations;
        }

        List<OrderAllocationEntity> allocations = order.getItems().stream()
                .flatMap(item -> reserveItemAllocations(item).stream())
                .toList();
        order.setStatus(STATUS_ALLOCATED);
        return allocations;
    }

    private void applyOrderTotals(OrderEntity order, BigDecimal subtotal, BigDecimal discountAmount) {
        BigDecimal discount = normalizeDiscountAmount(discountAmount, subtotal);
        order.setSubtotalAmount(subtotal);
        order.setDiscountAmount(discount);
        order.setTaxAmount(subtotal.multiply(TAX_RATE));
        order.setTotalAmount(subtotal.subtract(discount).add(order.getTaxAmount()));
    }

    private BigDecimal normalizeDiscountAmount(BigDecimal discountAmount, BigDecimal subtotal) {
        if (discountAmount == null || discountAmount.signum() < 0) {
            return BigDecimal.ZERO;
        }
        return discountAmount.compareTo(subtotal) > 0 ? subtotal : discountAmount;
    }

    private String resolveDiscountCode(CheckoutRequest request) {
        if (request.getDiscountCode() != null && !request.getDiscountCode().isBlank()) {
            return request.getDiscountCode().trim();
        }
        if (request.getPromotionCode() != null && !request.getPromotionCode().isBlank()) {
            return request.getPromotionCode().trim();
        }
        return null;
    }

    private List<OrderAllocationEntity> reserveItemAllocations(OrderItemEntity item) {
        List<InventoryBalanceEntity> balances =
                balanceRepository.findForUpdateByVariantIdOrderById(item.getVariantId());
        int requiredQty = item.getQty();
        int totalAvailable = balances.stream().mapToInt(this::availableQty).sum();
        if (totalAvailable < requiredQty) {
            throw new BusinessException("INV_001", "Insufficient available stock to allocate order");
        }

        int remainingQty = requiredQty;
        List<OrderAllocationEntity> allocations = new java.util.ArrayList<>();
        for (InventoryBalanceEntity balance : balances) {
            if (remainingQty == 0) {
                break;
            }
            int allocationQty = Math.min(availableQty(balance), remainingQty);
            if (allocationQty <= 0) {
                continue;
            }

            balance.setReservedQty(balance.getReservedQty() + allocationQty);
            balanceRepository.save(balance);

            OrderAllocationEntity allocation = new OrderAllocationEntity();
            allocation.setOrderItem(item);
            allocation.setLocation(balance.getLocation());
            allocation.setAllocatedQty(allocationQty);
            allocation.setStatus(STATUS_ALLOCATED);
            OrderAllocationEntity savedAllocation = allocationRepository.save(allocation);
            allocations.add(savedAllocation);
            
            PickTaskEntity pickTask = new PickTaskEntity();
            pickTask.setAllocation(savedAllocation);
            pickTask.setStatus("PENDING");
            pickTaskRepository.save(pickTask);
            
            remainingQty -= allocationQty;
        }
        return allocations;
    }

    private void fulfillAllocatedStock(OrderEntity order) {
        if (STATUS_SHIPPED.equals(order.getStatus()) || STATUS_DELIVERED.equals(order.getStatus())) {
            return;
        }

        List<OrderAllocationEntity> allocations = allocateStockForOrder(order);
        for (OrderAllocationEntity allocation : allocations) {
            InventoryBalanceEntity balance = balanceRepository
                    .findForUpdateByVariantIdAndLocationId(
                            allocation.getOrderItem().getVariantId(),
                            allocation.getLocation().getId())
                    .orElseThrow(() -> new BusinessException("INV_001", "Allocated stock balance not found"));

            if (balance.getReservedQty() < allocation.getAllocatedQty()
                    || balance.getOnHandQty() < allocation.getAllocatedQty()) {
                throw new BusinessException("INV_001", "Allocated stock balance is inconsistent");
            }

            balance.setReservedQty(balance.getReservedQty() - allocation.getAllocatedQty());
            balance.setOnHandQty(balance.getOnHandQty() - allocation.getAllocatedQty());
            balanceRepository.save(balance);
            allocation.setStatus(STATUS_FULFILLED);
            allocationRepository.save(allocation);
        }
    }

    private void releaseAllocatedStock(OrderEntity order) {
        List<OrderAllocationEntity> allocations =
                allocationRepository.findByOrderIdAndStatus(order.getId(), STATUS_ALLOCATED);
        for (OrderAllocationEntity allocation : allocations) {
            InventoryBalanceEntity balance = balanceRepository
                    .findForUpdateByVariantIdAndLocationId(
                            allocation.getOrderItem().getVariantId(),
                            allocation.getLocation().getId())
                    .orElseThrow(() -> new BusinessException("INV_001", "Allocated stock balance not found"));

            if (balance.getReservedQty() < allocation.getAllocatedQty()) {
                throw new BusinessException("INV_001", "Allocated stock balance is inconsistent");
            }

            balance.setReservedQty(balance.getReservedQty() - allocation.getAllocatedQty());
            balanceRepository.save(balance);
            allocation.setStatus(STATUS_RELEASED);
            allocationRepository.save(allocation);
        }
    }

    private int availableQty(InventoryBalanceEntity balance) {
        return balance.getOnHandQty() - balance.getReservedQty();
    }

    public String refreshShipmentStatus(Long orderId) {
        ShipmentEntity shipment = shipmentRepository.findByOrderId(orderId)
                .orElseThrow(() -> new BusinessException("ORD_002", "Shipment not found"));
        OrderEntity order = orderRepository.findById(orderId)
                .orElseThrow(() -> new BusinessException("ORD_002", "Order not found"));
        String previousShipmentStatus = shipment.getStatus();
        String previousOrderStatus = order.getStatus();
        String previousOrderShipmentStatus = order.getShipmentStatus();
        String status = ShipmentStatuses.normalizeStatus(ghnClient.getShippingStatus(shipment.getTrackingNumber()));
        shipment.setStatus(status);
        if (SHIPMENT_STATUS_DELIVERED.equals(status)) {
            shipment.setDeliveredAt(OffsetDateTime.now());
        }
        ShipmentEntity savedShipment = shipmentRepository.save(shipment);
        applyOrderShipmentStatus(order, status);
        orderRepository.save(order);
        if (!status.equals(previousShipmentStatus)
                || !order.getStatus().equals(previousOrderStatus)
                || !order.getShipmentStatus().equals(previousOrderShipmentStatus)) {
            auditShipmentSaved(
                    AuditAction.SHIPMENT_STATUS_CHANGED,
                    savedShipment,
                    previousShipmentStatus,
                    shipment.getTrackingNumber(),
                    previousOrderStatus,
                    previousOrderShipmentStatus,
                    "TRACKING_REFRESH"
            );
        }
        return status;
    }

    private String normalizeStripePaymentStatus(String stripeStatus) {
        if (stripeStatus == null) {
            return PAYMENT_STATUS_PENDING;
        }
        return switch (stripeStatus) {
            case "succeeded" -> PAYMENT_STATUS_PAID;
            case "canceled" -> PAYMENT_STATUS_FAILED;
            case "requires_payment_method", "requires_confirmation", "requires_action", "processing" ->
                    PAYMENT_STATUS_PENDING;
            default -> PAYMENT_STATUS_PENDING;
        };
    }

    @Transactional(readOnly = true)
    public List<String> getTrackingNumbers(Long orderId) {
        return shipmentRepository.findByOrderId(orderId)
                .map(item -> List.of(item.getTrackingNumber()))
                .orElse(List.of());
    }

    @Transactional(readOnly = true)
    public List<Map<String, Object>> getShipmentTrackingEvents(Long orderId) {
        OrderEntity order = orderRepository.findById(orderId)
                .orElseThrow(() -> new BusinessException("ORD_002", "Order not found"));
        ShipmentEntity shipment = shipmentRepository.findByOrderId(orderId).orElse(null);
        String shipmentStatus = shipment == null ? SHIPMENT_STATUS_PENDING : shipment.getStatus();

        List<Map<String, Object>> events = new ArrayList<>();
        events.add(trackingEvent("PLACED", "Order placed", "COMPLETED", order.getCreatedAt()));
        events.add(trackingEvent("ALLOCATED", "Stock allocated", orderStatusAtLeast(order, STATUS_ALLOCATED) ? "COMPLETED" : "PENDING", null));
        events.add(trackingEvent("PACKED", "Packed", orderStatusAtLeast(order, STATUS_PACKED) ? "COMPLETED" : "PENDING", null));
        events.add(trackingEvent(
                SHIPMENT_STATUS_SHIPPED,
                "Shipped",
                shipmentStatusAtLeast(shipmentStatus, SHIPMENT_STATUS_SHIPPED) ? "COMPLETED" : "PENDING",
                shipment == null ? null : shipment.getShippedAt()
        ));
        events.add(trackingEvent(
                SHIPMENT_STATUS_IN_TRANSIT,
                "In transit",
                shipmentStatusAtLeast(shipmentStatus, SHIPMENT_STATUS_IN_TRANSIT) ? "COMPLETED" : "PENDING",
                null
        ));
        events.add(trackingEvent(
                SHIPMENT_STATUS_DELIVERED,
                "Delivered",
                SHIPMENT_STATUS_DELIVERED.equals(shipmentStatus) ? "COMPLETED" : "PENDING",
                shipment == null ? null : shipment.getDeliveredAt()
        ));
        return events;
    }

    private OrderDto mapToDto(OrderEntity entity) {
        OrderDto dto = new OrderDto();
        dto.setId(entity.getId());
        dto.setOrderNumber(entity.getOrderNumber());
        dto.setStatus(entity.getStatus());
        dto.setPaymentStatus(entity.getPaymentStatus());
        dto.setShipmentStatus(entity.getShipmentStatus());
        dto.setTotalAmount(entity.getTotalAmount());
        dto.setCurrency(entity.getCurrency());
        dto.setShippingAddress(entity.getShippingAddress());
        dto.setItems(entity.getItems().stream().map(item -> {
            OrderDto.OrderItemDto itemDto = new OrderDto.OrderItemDto();
            itemDto.setId(item.getId());
            itemDto.setVariantId(item.getVariantId());
            itemDto.setProductName(item.getProductName());
            itemDto.setQty(item.getQty());
            itemDto.setUnitPrice(item.getUnitPrice());
            itemDto.setTotalPrice(item.getTotalPrice());
            // Enrich with variant details (sku, color, size, image)
            productVariantRepository.findById(item.getVariantId()).ifPresent(variant -> {
                itemDto.setSkuCode(variant.getSkuCode());
                itemDto.setColor(variant.getColor());
                itemDto.setSize(variant.getSize());
                // Pick first product image
                if (variant.getProduct() != null && variant.getProduct().getImages() != null) {
                    variant.getProduct().getImages().stream()
                            .sorted(java.util.Comparator.comparingInt(img -> img.getSortOrder() == null ? 0 : img.getSortOrder()))
                            .findFirst()
                            .ifPresent(img -> itemDto.setImageUrl(img.getUrl()));
                }
            });
            return itemDto;
        }).collect(Collectors.toList()));
        return dto;
    }

    private void requireOrderScope(Long orderId, LocationScope locationScope) {
        if (locationScope.allLocations()) {
            return;
        }
        if (locationScope.isEmpty()
                || !allocationRepository.existsByOrderIdAndLocationIds(orderId, locationScope.locationIds())) {
            throw new ForbiddenOperationException("Missing location access for order: " + orderId);
        }
    }

    private void completeCodPaymentIfPending(OrderEntity order) {
        if (PAYMENT_STATUS_PAID.equals(order.getPaymentStatus())) {
            return;
        }
        List<PaymentEntity> payments = paymentRepository.findByOrderId(order.getId());
        payments.stream()
                .filter(payment -> PAYMENT_PROVIDER_COD.equalsIgnoreCase(payment.getProvider()))
                .filter(payment -> PAYMENT_STATUS_PENDING.equalsIgnoreCase(payment.getStatus()))
                .findFirst()
                .ifPresent(payment -> {
                    payment.setStatus(PAYMENT_STATUS_PAID);
                    paymentRepository.save(payment);
                    order.setPaymentStatus(PAYMENT_STATUS_PAID);
                });
    }

    private void validateOrderCanShip(OrderEntity order) {
        if (!SHIPPABLE_ORDER_STATUSES.contains(order.getStatus())) {
            throw new BusinessException("ORD_003", "Order must be confirmed, paid, allocated, or packed before shipping");
        }
        if (PAYMENT_STATUS_PAID.equals(order.getPaymentStatus()) || hasPendingCodPayment(order.getId())) {
            return;
        }
        throw new BusinessException("ORD_003", "Order must be paid or pending COD before shipping");
    }

    private boolean hasPendingCodPayment(Long orderId) {
        return paymentRepository.findByOrderId(orderId).stream()
                .anyMatch(payment -> PAYMENT_PROVIDER_COD.equalsIgnoreCase(payment.getProvider())
                        && PAYMENT_STATUS_PENDING.equalsIgnoreCase(payment.getStatus()));
    }

    private void validateGhnShipmentRequest(String toName, String toPhone, String shippingAddress) {
        if (!hasText(toName) || !hasText(toPhone) || !hasText(shippingAddress)) {
            throw new BusinessException("ORD_003", "GHN shipments require recipient name, phone, and shipping address");
        }
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }

    private void applyOrderShipmentStatus(OrderEntity order, String shipmentStatus) {
        if (SHIPMENT_STATUS_DELIVERED.equals(shipmentStatus)) {
            order.setStatus(STATUS_DELIVERED);
            order.setShipmentStatus(SHIPMENT_STATUS_DELIVERED);
            completeCodPaymentIfPending(order);
            return;
        }
        if (SHIPMENT_STATUS_IN_TRANSIT.equals(shipmentStatus) || SHIPMENT_STATUS_SHIPPED.equals(shipmentStatus)) {
            order.setStatus(STATUS_SHIPPED);
            order.setShipmentStatus(shipmentStatus);
        }
    }

    private void auditShipmentSaved(
            AuditAction action,
            ShipmentEntity shipment,
            String previousShipmentStatus,
            String previousTrackingNumber,
            String previousOrderStatus,
            String previousOrderShipmentStatus,
            String source
    ) {
        Map<String, Object> metadata = new LinkedHashMap<>();
        metadata.put("orderId", shipment.getOrderId());
        metadata.put("carrier", shipment.getCarrier());
        metadata.put("trackingNumber", shipment.getTrackingNumber());
        metadata.put("previousTrackingNumber", previousTrackingNumber);
        metadata.put("previousShipmentStatus", previousShipmentStatus);
        metadata.put("newShipmentStatus", shipment.getStatus());
        metadata.put("previousOrderStatus", previousOrderStatus);
        metadata.put("previousOrderShipmentStatus", previousOrderShipmentStatus);
        metadata.put("source", source);
        auditService.write(action, "Shipment", shipmentTargetId(shipment), metadata);
    }

    private String shipmentTargetId(ShipmentEntity shipment) {
        return shipment.getId() == null ? "order:" + shipment.getOrderId() : shipment.getId().toString();
    }

    private Map<String, Object> trackingEvent(String code, String label, String state, OffsetDateTime occurredAt) {
        Map<String, Object> event = new LinkedHashMap<>();
        event.put("code", code);
        event.put("label", label);
        event.put("state", state);
        if (occurredAt != null) {
            event.put("occurredAt", occurredAt);
        }
        return event;
    }

    private boolean orderStatusAtLeast(OrderEntity order, String expectedStatus) {
        return orderStatusRank(order.getStatus()) >= orderStatusRank(expectedStatus);
    }

    private int orderStatusRank(String status) {
        if (STATUS_DELIVERED.equals(status)) {
            return 6;
        }
        if (STATUS_SHIPPED.equals(status)) {
            return 5;
        }
        if (STATUS_PACKED.equals(status)) {
            return 4;
        }
        if (STATUS_ALLOCATED.equals(status)) {
            return 3;
        }
        if (STATUS_PAID.equals(status) || STATUS_CONFIRMED.equals(status)) {
            return 2;
        }
        return 1;
    }

    private boolean shipmentStatusAtLeast(String actualStatus, String expectedStatus) {
        return shipmentStatusRank(actualStatus) >= shipmentStatusRank(expectedStatus);
    }

    private int shipmentStatusRank(String status) {
        if (SHIPMENT_STATUS_DELIVERED.equals(status)) {
            return 4;
        }
        if (SHIPMENT_STATUS_IN_TRANSIT.equals(status)) {
            return 3;
        }
        if (SHIPMENT_STATUS_SHIPPED.equals(status)) {
            return 2;
        }
        return 1;
    }
}
