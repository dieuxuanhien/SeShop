package com.seshop.review.api;

import com.seshop.review.api.dto.CreateReviewRequest;
import com.seshop.review.api.dto.ReviewDto;
import com.seshop.review.application.ReviewService;
import com.seshop.shared.security.AuthenticatedUser;
import com.seshop.shared.security.PermissionValidator;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/reviews")
public class ReviewController {

    private static final String REVIEW_MODERATE = "review.moderate";

    private final ReviewService reviewService;
    private final PermissionValidator permissionValidator;
    private final com.seshop.shared.util.FileStorageService fileStorageService;

    public ReviewController(ReviewService reviewService, PermissionValidator permissionValidator, com.seshop.shared.util.FileStorageService fileStorageService) {
        this.reviewService = reviewService;
        this.permissionValidator = permissionValidator;
        this.fileStorageService = fileStorageService;
    }

    @PostMapping
    public ResponseEntity<Map<String, Object>> create(
            @AuthenticationPrincipal AuthenticatedUser user,
            @Valid @RequestBody CreateReviewRequest request
    ) {
        ReviewDto dto = reviewService.createReview(user.userId(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("data", dto));
    }

    @GetMapping("/product/{productId}")
    public ResponseEntity<Map<String, Object>> listByProduct(@PathVariable Long productId) {
        List<ReviewDto> items = reviewService.getReviewsByProduct(productId);
        return ResponseEntity.ok(Map.of("data", items));
    }

    @PostMapping("/upload-image")
    public ResponseEntity<Map<String, Object>> uploadImage(
            @AuthenticationPrincipal AuthenticatedUser user,
            @org.springframework.web.bind.annotation.RequestParam("file") org.springframework.web.multipart.MultipartFile file) {
        String url = fileStorageService.store(file);
        return ResponseEntity.ok(Map.of("data", Map.of("imageUrl", url)));
    }

    // ── Staff Moderation ────────────────────────────────────────────────────

    @GetMapping("/pending")
    public ResponseEntity<Map<String, Object>> listPending() {
        permissionValidator.require(REVIEW_MODERATE);
        List<ReviewDto> items = reviewService.getPendingReviews();
        return ResponseEntity.ok(Map.of("data", items));
    }

    @PutMapping("/{reviewId}/approve")
    public ResponseEntity<Map<String, Object>> approve(@PathVariable Long reviewId) {
        permissionValidator.require(REVIEW_MODERATE);
        ReviewDto dto = reviewService.approveReview(reviewId);
        return ResponseEntity.ok(Map.of("data", dto));
    }

    @PutMapping("/{reviewId}/reject")
    public ResponseEntity<Map<String, Object>> reject(
            @PathVariable Long reviewId,
            @RequestBody(required = false) Map<String, String> body) {
        permissionValidator.require(REVIEW_MODERATE);
        String reason = body != null ? body.get("reason") : null;
        ReviewDto dto = reviewService.rejectReview(reviewId, reason);
        return ResponseEntity.ok(Map.of("data", dto));
    }
}
