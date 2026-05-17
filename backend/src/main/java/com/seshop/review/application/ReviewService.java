package com.seshop.review.application;

import com.seshop.audit.application.AuditService;
import com.seshop.audit.domain.AuditAction;
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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
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
 * - Staff can approve or reject reviews (moderation workflow).
 */
@Service
@Transactional
public class ReviewService {

    /** Maximum days after order delivery within which a review may be submitted. */
    private static final int REVIEW_WINDOW_DAYS = 30;

    private final ReviewRepository reviewRepository;
    private final OrderItemRepository orderItemRepository;
    private final ProductVariantRepository productVariantRepository;
    private final AuditService auditService;

    public ReviewService(
            ReviewRepository reviewRepository,
            OrderItemRepository orderItemRepository,
            ProductVariantRepository productVariantRepository,
            AuditService auditService) {
        this.reviewRepository = reviewRepository;
        this.orderItemRepository = orderItemRepository;
        this.productVariantRepository = productVariantRepository;
        this.auditService = auditService;
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

        ReviewEntity saved = reviewRepository.save(review);

        Map<String, Object> metadata = new LinkedHashMap<>();
        metadata.put("reviewId", saved.getId());
        metadata.put("orderItemId", saved.getOrderItemId());
        metadata.put("customerId", customerId);
        metadata.put("rating", saved.getRating());
        metadata.put("status", saved.getStatus());
        auditService.write(AuditAction.REVIEW_SUBMITTED, "Review", saved.getId().toString(), metadata);

        return mapToDto(saved, resolveProductId(orderItem));
    }

    /**
     * Staff moderation: approve a review so it becomes publicly visible.
     */
    public ReviewDto approveReview(Long reviewId) {
        ReviewEntity review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ResourceNotFoundException("REV_404", "Review not found"));

        if (!"PENDING_MODERATION".equals(review.getStatus())) {
            throw new BusinessException("REV_003",
                    "Only pending reviews can be approved; current status: " + review.getStatus());
        }

        review.setStatus("PUBLISHED");
        ReviewEntity saved = reviewRepository.save(review);

        Map<String, Object> metadata = new LinkedHashMap<>();
        metadata.put("reviewId", saved.getId());
        metadata.put("previousStatus", "PENDING_MODERATION");
        metadata.put("newStatus", "PUBLISHED");
        auditService.write(AuditAction.REVIEW_MODERATED, "Review", saved.getId().toString(), metadata);

        return mapToDto(saved, resolveProductId(saved.getOrderItemId()));
    }

    /**
     * Staff moderation: reject a review so it will not be published.
     */
    public ReviewDto rejectReview(Long reviewId, String reason) {
        ReviewEntity review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ResourceNotFoundException("REV_404", "Review not found"));

        if (!"PENDING_MODERATION".equals(review.getStatus())) {
            throw new BusinessException("REV_003",
                    "Only pending reviews can be rejected; current status: " + review.getStatus());
        }

        review.setStatus("REJECTED");
        ReviewEntity saved = reviewRepository.save(review);

        Map<String, Object> metadata = new LinkedHashMap<>();
        metadata.put("reviewId", saved.getId());
        metadata.put("previousStatus", "PENDING_MODERATION");
        metadata.put("newStatus", "REJECTED");
        metadata.put("reason", reason);
        auditService.write(AuditAction.REVIEW_MODERATED, "Review", saved.getId().toString(), metadata);

        return mapToDto(saved, resolveProductId(saved.getOrderItemId()));
    }

    /**
     * Staff: list all reviews pending moderation.
     */
    @Transactional(readOnly = true)
    public List<ReviewDto> getPendingReviews() {
        return reviewRepository.findByStatus("PENDING_MODERATION").stream()
                .map(review -> mapToDto(review, resolveProductId(review.getOrderItemId())))
                .toList();
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
