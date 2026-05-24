package com.seshop.commerce.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;

import com.seshop.audit.application.AuditService;
import com.seshop.audit.domain.AuditAction;
import com.seshop.catalog.infrastructure.persistence.ProductVariantRepository;
import com.seshop.catalog.infrastructure.persistence.ProductEntity;
import com.seshop.catalog.infrastructure.persistence.ProductVariantEntity;
import com.seshop.commerce.api.dto.CheckoutRequest;
import com.seshop.commerce.api.dto.CheckoutResponse;
import com.seshop.commerce.api.dto.OrderDto;
import com.seshop.commerce.api.dto.ProcessOrderRequest;
import com.seshop.commerce.infrastructure.persistence.CartEntity;
import com.seshop.commerce.infrastructure.persistence.CartItemEntity;
import com.seshop.commerce.infrastructure.persistence.CartRepository;
import com.seshop.commerce.infrastructure.persistence.OrderAllocationEntity;
import com.seshop.commerce.infrastructure.persistence.OrderAllocationRepository;
import com.seshop.commerce.infrastructure.persistence.OrderEntity;
import com.seshop.commerce.infrastructure.persistence.OrderItemEntity;
import com.seshop.commerce.infrastructure.persistence.OrderRepository;
import com.seshop.commerce.infrastructure.persistence.PaymentEntity;
import com.seshop.commerce.infrastructure.persistence.PaymentRepository;
import com.seshop.inventory.infrastructure.persistence.InventoryBalanceEntity;
import com.seshop.inventory.infrastructure.persistence.InventoryBalanceRepository;
import com.seshop.inventory.infrastructure.persistence.LocationEntity;
import com.seshop.marketing.api.dto.DiscountValidationResponse;
import com.seshop.marketing.application.DiscountService;
import com.seshop.payment.infrastructure.StripeClient;
import com.seshop.shipping.infrastructure.GhnClient;
import com.seshop.shipping.infrastructure.persistence.ShipmentEntity;
import com.seshop.shipping.infrastructure.persistence.ShipmentRepository;
import com.seshop.shared.exception.BusinessException;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private CartRepository cartRepository;

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private StripeClient stripeClient;

    @Mock
    private GhnClient ghnClient;

    @Mock
    private ShipmentRepository shipmentRepository;

    @Mock
    private ProductVariantRepository productVariantRepository;

    @Mock
    private InventoryBalanceRepository balanceRepository;

    @Mock
    private OrderAllocationRepository allocationRepository;

    @Mock
    private DiscountService discountService;

    @Mock
    private AuditService auditService;

    private OrderService service;

    @BeforeEach
    void setUp() {
        service = new OrderService(
                orderRepository,
                cartRepository,
                paymentRepository,
                stripeClient,
                ghnClient,
                shipmentRepository,
                productVariantRepository,
                balanceRepository,
                allocationRepository,
                null, discountService,
                auditService
        );
    }

    @Test
    void checkoutReservesAvailableStockBeforePayment() {
        CartEntity cart = cart(301L, 42L, cartItem(7001L, 2));
        ProductVariantEntity variant = variant(7001L, "SKU-001", "Linen Shirt", "590000.00");
        InventoryBalanceEntity balance = balance(9001L, 7001L, location(11L), 5, 1);

        given(cartRepository.findById(301L)).willReturn(Optional.of(cart));
        given(productVariantRepository.findById(7001L)).willReturn(Optional.of(variant));
        given(orderRepository.save(any(OrderEntity.class))).willAnswer(invocation -> {
            OrderEntity order = invocation.getArgument(0);
            order.setId(1001L);
            return order;
        });
        given(allocationRepository.findByOrderIdAndStatus(1001L, "ALLOCATED")).willReturn(List.of());
        given(balanceRepository.findForUpdateByVariantIdOrderById(7001L)).willReturn(List.of(balance));
        given(allocationRepository.save(any(OrderAllocationEntity.class)))
                .willAnswer(invocation -> invocation.getArgument(0));
        given(paymentRepository.save(any(PaymentEntity.class))).willAnswer(invocation -> invocation.getArgument(0));
        given(cartRepository.save(cart)).willReturn(cart);

        CheckoutResponse response = service.checkout(42L, checkoutRequest(301L, "COD"));

        assertThat(response.getOrderId()).isEqualTo(1001L);
        assertThat(response.getPaymentStatus()).isEqualTo("PENDING");
        assertThat(balance.getReservedQty()).isEqualTo(3);
        assertThat(cart.getStatus()).isEqualTo("COMPLETED");

        ArgumentCaptor<OrderAllocationEntity> allocationCaptor =
                ArgumentCaptor.forClass(OrderAllocationEntity.class);
        then(allocationRepository).should().save(allocationCaptor.capture());
        assertThat(allocationCaptor.getValue().getAllocatedQty()).isEqualTo(2);

        ArgumentCaptor<PaymentEntity> paymentCaptor = ArgumentCaptor.forClass(PaymentEntity.class);
        then(paymentRepository).should().save(paymentCaptor.capture());
        assertThat(paymentCaptor.getValue().getProvider()).isEqualTo("COD");
        assertThat(paymentCaptor.getValue().getStatus()).isEqualTo("PENDING");
    }

    @Test
    void checkoutAppliesDiscountToOrderAndPaymentTotal() {
        CartEntity cart = cart(301L, 42L, cartItem(7001L, 2));
        ProductVariantEntity variant = variant(7001L, "SKU-001", "Linen Shirt", "590000.00");
        InventoryBalanceEntity balance = balance(9001L, 7001L, location(11L), 5, 0);
        CheckoutRequest request = checkoutRequest(301L, "COD");
        request.setDiscountCode("SAVE100");

        given(cartRepository.findById(301L)).willReturn(Optional.of(cart));
        given(productVariantRepository.findById(7001L)).willReturn(Optional.of(variant));
        given(orderRepository.save(any(OrderEntity.class))).willAnswer(invocation -> {
            OrderEntity order = invocation.getArgument(0);
            order.setId(1001L);
            return order;
        });
        DiscountValidationResponse discount = new DiscountValidationResponse();
        discount.setValid(true);
        discount.setDiscountAmount(new BigDecimal("100000"));
        given(discountService.redeemDiscount("SAVE100", 42L, 1001L, new BigDecimal("1180000.00")))
                .willReturn(discount);
        given(allocationRepository.findByOrderIdAndStatus(1001L, "ALLOCATED")).willReturn(List.of());
        given(balanceRepository.findForUpdateByVariantIdOrderById(7001L)).willReturn(List.of(balance));
        given(allocationRepository.save(any(OrderAllocationEntity.class)))
                .willAnswer(invocation -> invocation.getArgument(0));
        given(paymentRepository.save(any(PaymentEntity.class))).willAnswer(invocation -> invocation.getArgument(0));
        given(cartRepository.save(cart)).willReturn(cart);

        service.checkout(42L, request);

        ArgumentCaptor<OrderEntity> orderCaptor = ArgumentCaptor.forClass(OrderEntity.class);
        then(orderRepository).should().save(orderCaptor.capture());
        OrderEntity savedOrder = orderCaptor.getValue();
        assertThat(savedOrder.getSubtotalAmount()).isEqualByComparingTo("1180000.00");
        assertThat(savedOrder.getDiscountAmount()).isEqualByComparingTo("100000");
        assertThat(savedOrder.getTaxAmount()).isEqualByComparingTo("118000.000");
        assertThat(savedOrder.getTotalAmount()).isEqualByComparingTo("1198000.000");

        ArgumentCaptor<PaymentEntity> paymentCaptor = ArgumentCaptor.forClass(PaymentEntity.class);
        then(paymentRepository).should().save(paymentCaptor.capture());
        assertThat(paymentCaptor.getValue().getAmount()).isEqualByComparingTo("1198000.000");
    }

    @Test
    void checkoutRejectsInvalidDiscountBeforeStockReservation() {
        CartEntity cart = cart(301L, 42L, cartItem(7001L, 2));
        ProductVariantEntity variant = variant(7001L, "SKU-001", "Linen Shirt", "590000.00");
        CheckoutRequest request = checkoutRequest(301L, "COD");
        request.setDiscountCode("OLD20");

        given(cartRepository.findById(301L)).willReturn(Optional.of(cart));
        given(productVariantRepository.findById(7001L)).willReturn(Optional.of(variant));
        given(orderRepository.save(any(OrderEntity.class))).willAnswer(invocation -> {
            OrderEntity order = invocation.getArgument(0);
            order.setId(1001L);
            return order;
        });
        given(discountService.redeemDiscount("OLD20", 42L, 1001L, new BigDecimal("1180000.00")))
                .willThrow(new BusinessException("DISC_003", "Discount code is expired or not yet started"));

        assertThatThrownBy(() -> service.checkout(42L, request))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("expired");

        then(balanceRepository).shouldHaveNoInteractions();
        then(allocationRepository).should(never()).save(any());
        then(paymentRepository).should(never()).save(any());
        then(cartRepository).should(never()).save(cart);
    }

    @Test
    void checkoutRejectsWhenStockCannotBeReserved() {
        CartEntity cart = cart(301L, 42L, cartItem(7001L, 2));
        ProductVariantEntity variant = variant(7001L, "SKU-001", "Linen Shirt", "590000.00");
        InventoryBalanceEntity balance = balance(9001L, 7001L, location(11L), 2, 1);

        given(cartRepository.findById(301L)).willReturn(Optional.of(cart));
        given(productVariantRepository.findById(7001L)).willReturn(Optional.of(variant));
        given(orderRepository.save(any(OrderEntity.class))).willAnswer(invocation -> {
            OrderEntity order = invocation.getArgument(0);
            order.setId(1001L);
            return order;
        });
        given(allocationRepository.findByOrderIdAndStatus(1001L, "ALLOCATED")).willReturn(List.of());
        given(balanceRepository.findForUpdateByVariantIdOrderById(7001L)).willReturn(List.of(balance));

        assertThatThrownBy(() -> service.checkout(42L, checkoutRequest(301L, "COD")))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Insufficient available stock");

        assertThat(balance.getReservedQty()).isEqualTo(1);
        assertThat(cart.getStatus()).isEqualTo("ACTIVE");
        then(allocationRepository).should(never()).save(any());
        then(paymentRepository).should(never()).save(any());
        then(cartRepository).should(never()).save(cart);
    }

    @Test
    void allocateOrderPersistsSplitAllocationsAndReservesAvailableStock() {
        OrderEntity order = order(1001L, "CONFIRMED", item(5001L, 7001L, 3));
        InventoryBalanceEntity firstBalance = balance(9001L, 7001L, location(11L), 2, 0);
        InventoryBalanceEntity secondBalance = balance(9002L, 7001L, location(12L), 5, 1);

        given(orderRepository.findById(1001L)).willReturn(Optional.of(order));
        given(allocationRepository.findByOrderIdAndStatus(1001L, "ALLOCATED")).willReturn(List.of());
        given(balanceRepository.findForUpdateByVariantIdOrderById(7001L))
                .willReturn(List.of(firstBalance, secondBalance));
        given(allocationRepository.save(any(OrderAllocationEntity.class)))
                .willAnswer(invocation -> invocation.getArgument(0));
        given(orderRepository.save(order)).willReturn(order);

        service.allocateOrder(1001L);

        assertThat(order.getStatus()).isEqualTo("ALLOCATED");
        assertThat(firstBalance.getReservedQty()).isEqualTo(2);
        assertThat(secondBalance.getReservedQty()).isEqualTo(2);

        ArgumentCaptor<OrderAllocationEntity> allocationCaptor =
                ArgumentCaptor.forClass(OrderAllocationEntity.class);
        then(allocationRepository).should(times(2)).save(allocationCaptor.capture());
        assertThat(allocationCaptor.getAllValues())
                .extracting(OrderAllocationEntity::getAllocatedQty)
                .containsExactly(2, 1);
    }

    @Test
    void allocateOrderRejectsWhenAvailableStockIsShort() {
        OrderEntity order = order(1001L, "CONFIRMED", item(5001L, 7001L, 3));
        InventoryBalanceEntity balance = balance(9001L, 7001L, location(11L), 2, 1);

        given(orderRepository.findById(1001L)).willReturn(Optional.of(order));
        given(allocationRepository.findByOrderIdAndStatus(1001L, "ALLOCATED")).willReturn(List.of());
        given(balanceRepository.findForUpdateByVariantIdOrderById(7001L)).willReturn(List.of(balance));

        assertThatThrownBy(() -> service.allocateOrder(1001L))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Insufficient available stock");

        assertThat(balance.getReservedQty()).isEqualTo(1);
        then(balanceRepository).should(never()).save(any());
        then(allocationRepository).should(never()).save(any());
    }

    @Test
    void shipOrderFulfillsAllocatedStock() {
        OrderItemEntity item = item(5001L, 7001L, 2);
        OrderEntity order = order(1001L, "PACKED", item);
        order.setPaymentStatus("PAID");
        LocationEntity location = location(11L);
        OrderAllocationEntity allocation = allocation(item, location, 2);
        InventoryBalanceEntity balance = balance(9001L, 7001L, location, 5, 2);

        given(orderRepository.findById(1001L)).willReturn(Optional.of(order));
        given(allocationRepository.findByOrderIdAndStatus(1001L, "ALLOCATED"))
                .willReturn(List.of(allocation));
        given(balanceRepository.findForUpdateByVariantIdAndLocationId(7001L, 11L))
                .willReturn(Optional.of(balance));
        given(shipmentRepository.findByOrderId(1001L)).willReturn(Optional.empty());
        given(shipmentRepository.save(any(ShipmentEntity.class))).willAnswer(invocation -> invocation.getArgument(0));
        given(orderRepository.save(order)).willReturn(order);

        service.shipOrder(1001L, "GHN", "Nguyen Van A", "+84901000001", "GHN-123");

        assertThat(order.getStatus()).isEqualTo("SHIPPED");
        assertThat(balance.getOnHandQty()).isEqualTo(3);
        assertThat(balance.getReservedQty()).isZero();
        assertThat(allocation.getStatus()).isEqualTo("FULFILLED");
        then(ghnClient).shouldHaveNoInteractions();
        then(auditService).should().write(
                eq(AuditAction.SHIPMENT_CREATED),
                eq("Shipment"),
                eq("order:1001"),
                argThat((Map<String, ?> metadata) -> "GHN-123".equals(metadata.get("trackingNumber"))
                        && "SHIPPED".equals(metadata.get("newShipmentStatus"))
                        && "STAFF_SHIP".equals(metadata.get("source")))
        );
    }

    @Test
    void shipOrderRejectsInvalidOrderStateBeforeStockFulfillment() {
        OrderItemEntity item = item(5001L, 7001L, 2);
        OrderEntity order = order(1001L, "PAYMENT_FAILED", item);

        given(orderRepository.findById(1001L)).willReturn(Optional.of(order));

        assertThatThrownBy(() -> service.shipOrder(1001L, "GHN", "Nguyen Van A", "+84901000001", "GHN-123"))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("before shipping");

        then(allocationRepository).shouldHaveNoInteractions();
        then(shipmentRepository).shouldHaveNoInteractions();
        then(auditService).shouldHaveNoInteractions();
    }

    @Test
    void shipOrderRejectsInvalidTrackingNumberBeforeStockFulfillment() {
        OrderItemEntity item = item(5001L, 7001L, 2);
        OrderEntity order = order(1001L, "PACKED", item);
        order.setPaymentStatus("PAID");

        given(orderRepository.findById(1001L)).willReturn(Optional.of(order));

        assertThatThrownBy(() -> service.shipOrder(1001L, "GHN", "Nguyen Van A", "+84901000001", "bad tracking !"))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Tracking number format");

        then(allocationRepository).shouldHaveNoInteractions();
        then(shipmentRepository).shouldHaveNoInteractions();
        then(auditService).shouldHaveNoInteractions();
    }

    @Test
    void refreshShipmentStatusMarksDeliveredOrderAndAuditsStatusChange() {
        OrderItemEntity item = item(5001L, 7001L, 2);
        OrderEntity order = order(1001L, "SHIPPED", item);
        order.setPaymentStatus("PENDING");
        PaymentEntity payment = payment(order, "COD", "PENDING");
        ShipmentEntity shipment = shipment(1001L, "GHN", "GHN-123", "SHIPPED");

        given(shipmentRepository.findByOrderId(1001L)).willReturn(Optional.of(shipment));
        given(orderRepository.findById(1001L)).willReturn(Optional.of(order));
        given(ghnClient.getShippingStatus("GHN-123")).willReturn("delivered");
        given(shipmentRepository.save(shipment)).willReturn(shipment);
        given(paymentRepository.findByOrderId(1001L)).willReturn(List.of(payment));
        given(paymentRepository.save(payment)).willReturn(payment);
        given(orderRepository.save(order)).willReturn(order);

        String status = service.refreshShipmentStatus(1001L);

        assertThat(status).isEqualTo("DELIVERED");
        assertThat(shipment.getStatus()).isEqualTo("DELIVERED");
        assertThat(shipment.getDeliveredAt()).isNotNull();
        assertThat(order.getStatus()).isEqualTo("DELIVERED");
        assertThat(order.getShipmentStatus()).isEqualTo("DELIVERED");
        assertThat(payment.getStatus()).isEqualTo("PAID");
        then(auditService).should().write(
                eq(AuditAction.SHIPMENT_STATUS_CHANGED),
                eq("Shipment"),
                eq("order:1001"),
                argThat((Map<String, ?> metadata) -> "SHIPPED".equals(metadata.get("previousShipmentStatus"))
                        && "DELIVERED".equals(metadata.get("newShipmentStatus"))
                        && "TRACKING_REFRESH".equals(metadata.get("source")))
        );
    }

    @Test
    void getShipmentTrackingEventsReflectsPersistedShipmentProgress() {
        OrderItemEntity item = item(5001L, 7001L, 2);
        OrderEntity order = order(1001L, "SHIPPED", item);
        order.setCreatedAt(OffsetDateTime.parse("2026-05-16T10:00:00Z"));
        ShipmentEntity shipment = shipment(1001L, "GHN", "GHN-123", "IN_TRANSIT");
        shipment.setShippedAt(OffsetDateTime.parse("2026-05-16T11:00:00Z"));

        given(orderRepository.findById(1001L)).willReturn(Optional.of(order));
        given(shipmentRepository.findByOrderId(1001L)).willReturn(Optional.of(shipment));

        List<java.util.Map<String, Object>> events = service.getShipmentTrackingEvents(1001L);

        assertThat(events).extracting(event -> event.get("code"))
                .containsExactly("PLACED", "ALLOCATED", "PACKED", "SHIPPED", "IN_TRANSIT", "DELIVERED");
        assertThat(events).filteredOn(event -> "IN_TRANSIT".equals(event.get("code")))
                .allSatisfy(event -> assertThat(event.get("state")).isEqualTo("COMPLETED"));
        assertThat(events).filteredOn(event -> "DELIVERED".equals(event.get("code")))
                .allSatisfy(event -> assertThat(event.get("state")).isEqualTo("PENDING"));
    }

    @Test
    void cancelOrderReleasesAllocatedStock() {
        OrderItemEntity item = item(5001L, 7001L, 2);
        OrderEntity order = order(1001L, "ALLOCATED", item);
        LocationEntity location = location(11L);
        OrderAllocationEntity allocation = allocation(item, location, 2);
        InventoryBalanceEntity balance = balance(9001L, 7001L, location, 5, 2);

        given(orderRepository.findById(1001L)).willReturn(Optional.of(order));
        given(allocationRepository.findByOrderIdAndStatus(1001L, "ALLOCATED"))
                .willReturn(List.of(allocation));
        given(balanceRepository.findForUpdateByVariantIdAndLocationId(7001L, 11L))
                .willReturn(Optional.of(balance));
        given(orderRepository.save(order)).willReturn(order);

        service.cancelOrder(1001L);

        assertThat(order.getStatus()).isEqualTo("CANCELLED");
        assertThat(balance.getOnHandQty()).isEqualTo(5);
        assertThat(balance.getReservedQty()).isZero();
        assertThat(allocation.getStatus()).isEqualTo("RELEASED");
    }

    @Test
    void markPaymentFailedReleasesAllocatedStock() {
        OrderItemEntity item = item(5001L, 7001L, 2);
        OrderEntity order = order(1001L, "PENDING_PAYMENT", item);
        LocationEntity location = location(11L);
        OrderAllocationEntity allocation = allocation(item, location, 2);
        InventoryBalanceEntity balance = balance(9001L, 7001L, location, 5, 2);

        given(orderRepository.findById(1001L)).willReturn(Optional.of(order));
        given(allocationRepository.findByOrderIdAndStatus(1001L, "ALLOCATED"))
                .willReturn(List.of(allocation));
        given(balanceRepository.findForUpdateByVariantIdAndLocationId(7001L, 11L))
                .willReturn(Optional.of(balance));
        given(orderRepository.save(order)).willReturn(order);

        service.markPaymentFailed(1001L);

        assertThat(order.getStatus()).isEqualTo("PAYMENT_FAILED");
        assertThat(order.getPaymentStatus()).isEqualTo("FAILED");
        assertThat(balance.getReservedQty()).isZero();
        assertThat(allocation.getStatus()).isEqualTo("RELEASED");
    }

    @Test
    void processDeliverCompletesPendingCodPayment() {
        OrderItemEntity item = item(5001L, 7001L, 2);
        OrderEntity order = order(1001L, "SHIPPED", item);
        order.setPaymentStatus("PENDING");
        PaymentEntity payment = payment(order, "COD", "PENDING");
        ProcessOrderRequest request = processRequest("DELIVER");

        given(orderRepository.findById(1001L)).willReturn(Optional.of(order));
        given(paymentRepository.findByOrderId(1001L)).willReturn(List.of(payment));
        given(paymentRepository.save(payment)).willReturn(payment);
        given(orderRepository.save(order)).willReturn(order);

        OrderDto result = service.processOrder(1001L, request);

        assertThat(order.getStatus()).isEqualTo("DELIVERED");
        assertThat(order.getShipmentStatus()).isEqualTo("DELIVERED");
        assertThat(order.getPaymentStatus()).isEqualTo("PAID");
        assertThat(payment.getStatus()).isEqualTo("PAID");
        assertThat(result.getPaymentStatus()).isEqualTo("PAID");
    }

    @Test
    void markPaymentPaidUpdatesPendingPaymentOrderState() {
        OrderItemEntity item = item(5001L, 7001L, 2);
        OrderEntity order = order(1001L, "PENDING_PAYMENT", item);
        order.setPaymentStatus("PENDING");

        given(orderRepository.findById(1001L)).willReturn(Optional.of(order));
        given(orderRepository.save(order)).willReturn(order);

        OrderDto result = service.markPaymentPaid(1001L);

        assertThat(order.getStatus()).isEqualTo("PAID");
        assertThat(order.getPaymentStatus()).isEqualTo("PAID");
        assertThat(result.getPaymentStatus()).isEqualTo("PAID");
    }

    private CheckoutRequest checkoutRequest(Long cartId, String paymentProvider) {
        CheckoutRequest request = new CheckoutRequest();
        request.setCartId(cartId);
        request.setPaymentProvider(paymentProvider);

        CheckoutRequest.ShippingAddress address = new CheckoutRequest.ShippingAddress();
        address.setFullName("Nguyen Van A");
        address.setPhoneNumber("+84901000001");
        address.setLine1("123 Le Loi");
        address.setWard("Ben Nghe");
        address.setDistrict("District 1");
        address.setCity("Ho Chi Minh");
        request.setShippingAddress(address);
        return request;
    }

    private CartEntity cart(Long id, Long customerId, CartItemEntity item) {
        CartEntity cart = new CartEntity();
        cart.setId(id);
        cart.setCustomerId(customerId);
        cart.setStatus("ACTIVE");
        cart.setItems(List.of(item));
        item.setCart(cart);
        return cart;
    }

    private CartItemEntity cartItem(Long variantId, int qty) {
        CartItemEntity item = new CartItemEntity();
        item.setVariantId(variantId);
        item.setQty(qty);
        return item;
    }

    private OrderEntity order(Long id, String status, OrderItemEntity item) {
        OrderEntity order = new OrderEntity();
        order.setId(id);
        order.setCustomerId(42L);
        order.setOrderNumber("ORD-1001");
        order.setStatus(status);
        order.setPaymentStatus("PENDING");
        order.setShipmentStatus("PENDING");
        order.setCurrency("VND");
        order.setTotalAmount(new BigDecimal("590000.00"));
        order.setItems(List.of(item));
        item.setOrder(order);
        return order;
    }

    private ProcessOrderRequest processRequest(String action) {
        ProcessOrderRequest request = new ProcessOrderRequest();
        request.setAction(action);
        return request;
    }

    private PaymentEntity payment(OrderEntity order, String provider, String status) {
        PaymentEntity payment = new PaymentEntity();
        payment.setOrder(order);
        payment.setProvider(provider);
        payment.setStatus(status);
        payment.setAmount(order.getTotalAmount());
        return payment;
    }

    private ShipmentEntity shipment(Long orderId, String carrier, String trackingNumber, String status) {
        ShipmentEntity shipment = new ShipmentEntity();
        shipment.setOrderId(orderId);
        shipment.setCarrier(carrier);
        shipment.setTrackingNumber(trackingNumber);
        shipment.setStatus(status);
        return shipment;
    }

    private OrderItemEntity item(Long id, Long variantId, int qty) {
        OrderItemEntity item = new OrderItemEntity();
        item.setId(id);
        item.setVariantId(variantId);
        item.setQty(qty);
        item.setProductName("Linen Shirt");
        item.setSku("SKU-001");
        item.setUnitPrice(new BigDecimal("590000.00"));
        item.setTotalPrice(new BigDecimal("590000.00").multiply(BigDecimal.valueOf(qty)));
        return item;
    }

    private ProductVariantEntity variant(Long id, String skuCode, String productName, String price) {
        ProductEntity product = new ProductEntity();
        product.setName(productName);

        ProductVariantEntity variant = new ProductVariantEntity();
        variant.setId(id);
        variant.setSkuCode(skuCode);
        variant.setProduct(product);
        variant.setPrice(new BigDecimal(price));
        variant.setStatus("ACTIVE");
        return variant;
    }

    private InventoryBalanceEntity balance(Long id, Long variantId, LocationEntity location, int onHandQty, int reservedQty) {
        InventoryBalanceEntity balance = new InventoryBalanceEntity();
        balance.setId(id);
        balance.setVariantId(variantId);
        balance.setLocation(location);
        balance.setOnHandQty(onHandQty);
        balance.setReservedQty(reservedQty);
        return balance;
    }

    private LocationEntity location(Long id) {
        LocationEntity location = new LocationEntity();
        location.setId(id);
        location.setDisplayName("Location " + id);
        location.setCode("LOC-" + id);
        location.setLocationType("STORE");
        location.setStatus("ACTIVE");
        return location;
    }

    private OrderAllocationEntity allocation(OrderItemEntity item, LocationEntity location, int qty) {
        OrderAllocationEntity allocation = new OrderAllocationEntity();
        allocation.setOrderItem(item);
        allocation.setLocation(location);
        allocation.setAllocatedQty(qty);
        allocation.setStatus("ALLOCATED");
        return allocation;
    }
}
