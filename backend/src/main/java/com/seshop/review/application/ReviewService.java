package com.seshop.review.application;

import com.seshop.catalog.infrastructure.persistence.ProductVariantRepository;
import com.seshop.commerce.infrastructure.persistence.OrderItemEntity;
import com.seshop.commerce.infrastructure.persistence.OrderItemRepository;
import com.seshop.review.api.dto.CreateReviewRequest;
import com.seshop.review.api.dto.ReviewDto;
import com.seshop.review.infrastructure.persistence.ReviewEntity;
import com.seshop.review.infrastructure.persistence.ReviewRepository;
import com.seshop.shared.exception.BusinessException;
import com.seshop.shared.exception.ForbiddenOperationException;
import com.seshop.shared.exception.ResourceNotFoundException;
import java.time.OffsetDateTime;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * UC14: Customer reviews.
 *
 * Rules enforced:
 * - Customer must own the order item (order ownership check).
 * - The order must be in DELIVERED status (delivered-order check).
 * - Review must be submitted within 30 days of order delivery (review window).
 * - New reviews enter PENDING_MODERATION state; they are not immediately
 * visible.
 * - getReviewsByProduct returns only PUBLISHED reviews (moderation gate).
 * - getAverageRating returns the aggregate score across all PUBLISHED reviews.
 */
@Service
@Transactional
public class ReviewService {

    /** Maximum days after order delivery within which a review may be submitted. */
    private static final int REVIEW_WINDOW_DAYS = 30;

    private final ReviewRepository reviewRepository;
    private final OrderItemRepository orderItemRepository;
    private final ProductVariantRepository productVariantRepository;

    public ReviewService(
            ReviewRepository reviewRepository,
            OrderItemRepository orderItemRepository,
            ProductVariantRepository productVariantRepository) {
        this.reviewRepository = reviewRepository;
        this.orderItemRepository = orderItemRepository;
        this.productVariantRepository = productVariantRepository;
    }

    public ReviewDto createReview(Long customerId, CreateReviewRequest request) {
        OrderItemEntity orderItem = orderItemRepository.findById(request.getOrderItemId())
                .orElseThrow(() -> new ResourceNotFoundException("REV_404", "Order item not found"));

        // Ownership check
        if (!orderItem.getOrder().getCustomerId().equals(customerId)) {
            throw new ForbiddenOperationException("Order item belongs to another customer");
        }

        // Delivered-order check
        String orderStatus = orderItem.getOrder().getStatus();
        if (!"DELIVERED".equals(orderStatus)) {
            throw new BusinessException("REV_001",
                    "Reviews can only be submitted for delivered orders; current status: " + orderStatus);
        }

        // Review window check — use order updatedAt as a proxy for delivery time
        OffsetDateTime deliveredAt = orderItem.getOrder().getUpdatedAt();
        if (deliveredAt != null
                && OffsetDateTime.now().isAfter(deliveredAt.plusDays(REVIEW_WINDOW_DAYS))) {
            throw new BusinessException("REV_002",
                    "Review window has expired; reviews must be submitted within "
                            + REVIEW_WINDOW_DAYS + " days of delivery");
        }

        ReviewEntity review = reviewRepository
                .findByOrderItemIdAndCustomerUserId(request.getOrderItemId(), customerId)
                .orElseGet(ReviewEntity::new);
        review.setOrderItemId(request.getOrderItemId());
        review.setCustomerUserId(customerId);
        review.setRating(request.getRating());
        review.setComment(request.getComment());
        review.setImageUrl(request.getImageUrl());
        // New/updated reviews enter moderation queue; staff must approve before
        // publishing
        review.setStatus("PENDING_MODERATION");

        return mapToDto(reviewRepository.save(review), resolveProductId(orderItem));
    }

    @Transactional(readOnly = true)
    public List<ReviewDto> getReviewsByProduct(Long productId) {
        return reviewRepository.findPublishedByProductId(productId).stream()
                .map(review -> mapToDto(review, resolveProductId(review.getOrderItemId())))
                .toList();
    }

    @Transactional(readOnly = true)
    public Double getAverageRating(Long productId) {
        return reviewRepository.averageRatingByProductId(productId);
    }

    private Long resolveProductId(Long orderItemId) {
        OrderItemEntity orderItem = orderItemRepository.findById(orderItemId)
                .orElseThrow(() -> new ResourceNotFoundException("REV_404", "Order item not found"));
        return resolveProductId(orderItem);
    }

    private Long resolveProductId(OrderItemEntity orderItem) {
        return productVariantRepository.findById(orderItem.getVariantId())
                .orElseThrow(() -> new ResourceNotFoundException("CAT_404", "Product variant not found"))
                .getProduct()
                .getId();
    }

    private ReviewDto mapToDto(ReviewEntity entity, Long productId) {
        ReviewDto dto = new ReviewDto();
        dto.setReviewId(entity.getId());
        dto.setProductId(productId);
        dto.setOrderItemId(entity.getOrderItemId());
        dto.setRating(entity.getRating());
        dto.setComment(entity.getComment());
        dto.setImageUrl(entity.getImageUrl());
        dto.setStatus(entity.getStatus());
        dto.setCreatedAt(entity.getCreatedAt());
        return dto;
    }
}
