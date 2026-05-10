package com.seshop.review.application;

import com.seshop.catalog.infrastructure.persistence.ProductVariantRepository;
import com.seshop.commerce.infrastructure.persistence.OrderItemEntity;
import com.seshop.commerce.infrastructure.persistence.OrderItemRepository;
import com.seshop.review.api.dto.CreateReviewRequest;
import com.seshop.review.api.dto.ReviewDto;
import com.seshop.review.infrastructure.persistence.ReviewEntity;
import com.seshop.review.infrastructure.persistence.ReviewRepository;
import com.seshop.shared.exception.ForbiddenOperationException;
import com.seshop.shared.exception.ResourceNotFoundException;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final OrderItemRepository orderItemRepository;
    private final ProductVariantRepository productVariantRepository;

    public ReviewService(
            ReviewRepository reviewRepository,
            OrderItemRepository orderItemRepository,
            ProductVariantRepository productVariantRepository
    ) {
        this.reviewRepository = reviewRepository;
        this.orderItemRepository = orderItemRepository;
        this.productVariantRepository = productVariantRepository;
    }

    public ReviewDto createReview(Long customerId, CreateReviewRequest request) {
        OrderItemEntity orderItem = orderItemRepository.findById(request.getOrderItemId())
                .orElseThrow(() -> new ResourceNotFoundException("REV_404", "Order item not found"));
        if (!orderItem.getOrder().getCustomerId().equals(customerId)) {
            throw new ForbiddenOperationException("Order item belongs to another customer");
        }

        ReviewEntity review = reviewRepository
                .findByOrderItemIdAndCustomerUserId(request.getOrderItemId(), customerId)
                .orElseGet(ReviewEntity::new);
        review.setOrderItemId(request.getOrderItemId());
        review.setCustomerUserId(customerId);
        review.setRating(request.getRating());
        review.setComment(request.getComment());
        review.setImageUrl(request.getImageUrl());
        review.setStatus("PUBLISHED");

        return mapToDto(reviewRepository.save(review), resolveProductId(orderItem));
    }

    @Transactional(readOnly = true)
    public List<ReviewDto> getReviewsByProduct(Long productId) {
        return reviewRepository.findPublishedByProductId(productId).stream()
                .map(review -> mapToDto(review, resolveProductId(review.getOrderItemId())))
                .toList();
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
        dto.setCreatedAt(entity.getCreatedAt());
        return dto;
    }
}
