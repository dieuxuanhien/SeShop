package com.seshop.review.api;

import com.seshop.review.api.dto.CreateReviewRequest;
import com.seshop.review.api.dto.ReviewDto;
import com.seshop.review.application.ReviewService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/reviews")
public class ReviewController {

    private final ReviewService reviewService;

    public ReviewController(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    @PostMapping
    public ResponseEntity<Map<String, Object>> create(@Valid @RequestBody CreateReviewRequest request) {
        ReviewDto dto = reviewService.createReview(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("data", dto));
    }

    @GetMapping("/product/{productId}")
    public ResponseEntity<Map<String, Object>> listByProduct(@PathVariable Long productId) {
        List<ReviewDto> items = reviewService.getReviewsByProduct(productId);
        return ResponseEntity.ok(Map.of("data", items));
    }
}
