package com.seshop.review.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import com.seshop.audit.application.AuditService;
import com.seshop.audit.domain.AuditAction;
import com.seshop.catalog.infrastructure.persistence.ProductEntity;
import com.seshop.catalog.infrastructure.persistence.ProductVariantEntity;
import com.seshop.catalog.infrastructure.persistence.ProductVariantRepository;
import com.seshop.commerce.infrastructure.persistence.OrderEntity;
import com.seshop.commerce.infrastructure.persistence.OrderItemEntity;
import com.seshop.commerce.infrastructure.persistence.OrderItemRepository;
import com.seshop.review.api.dto.CreateReviewRequest;
import com.seshop.review.api.dto.ReviewDto;
import com.seshop.review.infrastructure.persistence.ReviewEntity;
import com.seshop.review.infrastructure.persistence.ReviewRepository;
import com.seshop.shared.exception.BusinessException;
import com.seshop.shared.exception.ForbiddenOperationException;
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

/**
 * UC14: Customer reviews, moderation workflow, and audit events.
 */
@ExtendWith(MockitoExtension.class)
class ReviewServiceTest {

    @Mock private ReviewRepository reviewRepository;
    @Mock private OrderItemRepository orderItemRepository;
    @Mock private ProductVariantRepository productVariantRepository;
    @Mock private AuditService auditService;

    private ReviewService service;

    @BeforeEach
    void setUp() {
        service = new ReviewService(reviewRepository, orderItemRepository, productVariantRepository, auditService);
    }

    // ── createReview ────────────────────────────────────────────────────────

    @Test
    void createReviewSucceedsForDeliveredOrder() {
        OrderItemEntity orderItem = deliveredOrderItem(42L, 5001L);
        given(orderItemRepository.findById(5001L)).willReturn(Optional.of(orderItem));
        given(reviewRepository.findByOrderItemIdAndCustomerUserId(5001L, 42L)).willReturn(Optional.empty());

        ReviewEntity saved = reviewEntity(1L, "PENDING_MODERATION");
        given(reviewRepository.save(any())).willReturn(saved);

        ProductVariantEntity variant = variantForProduct(10L);
        given(productVariantRepository.findById(anyLong())).willReturn(Optional.of(variant));

        CreateReviewRequest request = new CreateReviewRequest();
        request.setOrderItemId(5001L);
        request.setRating(5);
        request.setComment("Great product!");

        ReviewDto result = service.createReview(42L, request);

        assertThat(result.getStatus()).isEqualTo("PENDING_MODERATION");
        verify(auditService).write(eq(AuditAction.REVIEW_SUBMITTED), eq("Review"), any(), any(Map.class));
    }

    @Test
    void createReviewRejectsNonDeliveredOrder() {
        OrderItemEntity orderItem = orderItemWithStatus(42L, 5001L, "PROCESSING");
        given(orderItemRepository.findById(5001L)).willReturn(Optional.of(orderItem));

        CreateReviewRequest request = new CreateReviewRequest();
        request.setOrderItemId(5001L);
        request.setRating(4);
        request.setComment("Good");

        assertThatThrownBy(() -> service.createReview(42L, request))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Reviews can only be submitted for delivered orders");
    }

    @Test
    void createReviewRejectsWrongCustomer() {
        OrderItemEntity orderItem = deliveredOrderItem(99L, 5001L);
        given(orderItemRepository.findById(5001L)).willReturn(Optional.of(orderItem));

        CreateReviewRequest request = new CreateReviewRequest();
        request.setOrderItemId(5001L);
        request.setRating(5);
        request.setComment("Great");

        assertThatThrownBy(() -> service.createReview(42L, request))
                .isInstanceOf(ForbiddenOperationException.class);
    }

    @Test
    void createReviewRejectsExpiredWindow() {
        OrderItemEntity orderItem = deliveredOrderItem(42L, 5001L);
        // simulate delivery 31 days ago
        orderItem.getOrder().setUpdatedAt(OffsetDateTime.now().minusDays(31));
        given(orderItemRepository.findById(5001L)).willReturn(Optional.of(orderItem));

        CreateReviewRequest request = new CreateReviewRequest();
        request.setOrderItemId(5001L);
        request.setRating(3);
        request.setComment("Late review");

        assertThatThrownBy(() -> service.createReview(42L, request))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Review window has expired");
    }

    // ── approveReview ───────────────────────────────────────────────────────

    @Test
    void approveReviewTransitionsToPublished() {
        ReviewEntity review = reviewEntity(1L, "PENDING_MODERATION");
        review.setOrderItemId(5001L);
        given(reviewRepository.findById(1L)).willReturn(Optional.of(review));
        given(reviewRepository.save(any())).willAnswer(inv -> inv.getArgument(0));

        OrderItemEntity oi = deliveredOrderItem(42L, 5001L);
        given(orderItemRepository.findById(5001L)).willReturn(Optional.of(oi));
        given(productVariantRepository.findById(anyLong())).willReturn(Optional.of(variantForProduct(10L)));

        ReviewDto result = service.approveReview(1L);

        assertThat(result.getStatus()).isEqualTo("PUBLISHED");
        verify(auditService).write(eq(AuditAction.REVIEW_MODERATED), eq("Review"), any(), any(Map.class));
    }

    @Test
    void approveReviewRejectsAlreadyPublished() {
        ReviewEntity review = reviewEntity(1L, "PUBLISHED");
        given(reviewRepository.findById(1L)).willReturn(Optional.of(review));

        assertThatThrownBy(() -> service.approveReview(1L))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Only pending reviews can be approved");
    }

    // ── rejectReview ────────────────────────────────────────────────────────

    @Test
    void rejectReviewTransitionsToRejected() {
        ReviewEntity review = reviewEntity(1L, "PENDING_MODERATION");
        review.setOrderItemId(5001L);
        given(reviewRepository.findById(1L)).willReturn(Optional.of(review));
        given(reviewRepository.save(any())).willAnswer(inv -> inv.getArgument(0));

        OrderItemEntity oi = deliveredOrderItem(42L, 5001L);
        given(orderItemRepository.findById(5001L)).willReturn(Optional.of(oi));
        given(productVariantRepository.findById(anyLong())).willReturn(Optional.of(variantForProduct(10L)));

        ReviewDto result = service.rejectReview(1L, "Inappropriate content");

        assertThat(result.getStatus()).isEqualTo("REJECTED");
        verify(auditService).write(eq(AuditAction.REVIEW_MODERATED), eq("Review"), any(), any(Map.class));
    }

    // ── getPendingReviews ───────────────────────────────────────────────────

    @Test
    void getPendingReviewsReturnsOnlyPending() {
        ReviewEntity r1 = reviewEntity(1L, "PENDING_MODERATION");
        r1.setOrderItemId(5001L);
        given(reviewRepository.findByStatus("PENDING_MODERATION")).willReturn(List.of(r1));
        OrderItemEntity oi = deliveredOrderItem(42L, 5001L);
        given(orderItemRepository.findById(5001L)).willReturn(Optional.of(oi));
        given(productVariantRepository.findById(anyLong())).willReturn(Optional.of(variantForProduct(10L)));

        List<ReviewDto> pending = service.getPendingReviews();

        assertThat(pending).hasSize(1);
    }

    // ── helpers ─────────────────────────────────────────────────────────────

    private OrderItemEntity deliveredOrderItem(Long customerId, Long orderItemId) {
        return orderItemWithStatus(customerId, orderItemId, "DELIVERED");
    }

    private OrderItemEntity orderItemWithStatus(Long customerId, Long orderItemId, String status) {
        OrderEntity order = new OrderEntity();
        order.setCustomerId(customerId);
        order.setStatus(status);
        order.setUpdatedAt(OffsetDateTime.now().minusDays(1));

        OrderItemEntity item = new OrderItemEntity();
        item.setId(orderItemId);
        item.setOrder(order);
        item.setVariantId(100L);
        return item;
    }

    private ReviewEntity reviewEntity(Long id, String status) {
        ReviewEntity entity = new ReviewEntity();
        entity.setOrderItemId(1L);
        entity.setCustomerUserId(42L);
        entity.setRating(5);
        entity.setComment("Good");
        entity.setStatus(status);
        // use reflection or a public setter-ID if available; for test purposes
        // the repository mock controls ID via save
        try {
            var field = ReviewEntity.class.getDeclaredField("id");
            field.setAccessible(true);
            field.set(entity, id);
        } catch (Exception ignored) {
        }
        return entity;
    }

    private ProductVariantEntity variantForProduct(Long productId) {
        ProductEntity product = new ProductEntity();
        product.setId(productId);
        ProductVariantEntity variant = new ProductVariantEntity();
        variant.setProduct(product);
        return variant;
    }
}
