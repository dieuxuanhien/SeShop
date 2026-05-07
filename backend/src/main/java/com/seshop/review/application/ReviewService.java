package com.seshop.review.application;

import com.seshop.review.api.dto.CreateReviewRequest;
import com.seshop.review.api.dto.ReviewDto;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicLong;
import org.springframework.stereotype.Service;

@Service
public class ReviewService {

    private final AtomicLong reviewIdGenerator = new AtomicLong(1);
    private final Map<Long, List<ReviewDto>> productReviews = new ConcurrentHashMap<>();

    public ReviewDto createReview(CreateReviewRequest request) {
        ReviewDto dto = new ReviewDto();
        dto.setReviewId(reviewIdGenerator.getAndIncrement());
        dto.setOrderItemId(request.getOrderItemId());
        dto.setProductId(0L);
        dto.setRating(request.getRating());
        dto.setComment(request.getComment());
        dto.setImageUrl(request.getImageUrl());
        dto.setCreatedAt(OffsetDateTime.now());

        productReviews.computeIfAbsent(dto.getProductId(), key -> new CopyOnWriteArrayList<>()).add(dto);
        return dto;
    }

    public List<ReviewDto> getReviewsByProduct(Long productId) {
        return productReviews.getOrDefault(productId, List.of());
    }
}
