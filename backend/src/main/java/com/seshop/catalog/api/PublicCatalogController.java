package com.seshop.catalog.api;

import com.seshop.catalog.api.dto.CategoryDto;
import com.seshop.catalog.api.dto.ProductDto;
import com.seshop.catalog.application.CatalogService;
import com.seshop.review.application.ReviewService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1")
public class PublicCatalogController {

    private final CatalogService catalogService;
    private final ReviewService reviewService;

    public PublicCatalogController(CatalogService catalogService, ReviewService reviewService) {
        this.catalogService = catalogService;
        this.reviewService = reviewService;
    }

    @GetMapping("/products")
    public ResponseEntity<Map<String, Object>> browseProducts(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String brand,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) String size,
            @RequestParam(required = false) String color,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int pageSize,
            @RequestParam(defaultValue = "createdAt,desc") String[] sort) {

        String sortField = sort[0];
        Sort.Direction sortDirection = sort.length > 1 && sort[1].equalsIgnoreCase("asc") ?
                Sort.Direction.ASC : Sort.Direction.DESC;
        
        Pageable pageable = PageRequest.of(page, pageSize, Sort.by(sortDirection, sortField));

        boolean hasExtendedFilters = categoryId != null || size != null || color != null
                || minPrice != null || maxPrice != null;

        Page<ProductDto> products;
        if (hasExtendedFilters) {
            products = catalogService.getPublishedProductsFiltered(
                    keyword, brand, categoryId, size, color, minPrice, maxPrice, pageable);
        } else {
            products = catalogService.getPublishedProducts(keyword, brand, pageable);
        }

        Map<String, Object> data = new HashMap<>();
        data.put("items", products.getContent());
        data.put("page", products.getNumber());
        data.put("size", products.getSize());
        data.put("totalElements", products.getTotalElements());
        data.put("totalPages", products.getTotalPages());

        Map<String, Object> response = new HashMap<>();
        response.put("data", data);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/products/{productId}")
    public ResponseEntity<Map<String, Object>> getProductDetail(@PathVariable Long productId) {
        ProductDto product = catalogService.getProductById(productId);
        
        Map<String, Object> response = new HashMap<>();
        response.put("data", product);
        
        return ResponseEntity.ok(response);
    }

    @GetMapping("/products/{productId}/rating")
    public ResponseEntity<Map<String, Object>> getProductRating(@PathVariable Long productId) {
        Double avgRating = reviewService.getAverageRating(productId);
        Map<String, Object> data = new HashMap<>();
        data.put("averageRating", avgRating);
        data.put("productId", productId);
        return ResponseEntity.ok(Map.of("data", data));
    }

    @GetMapping("/categories")
    public ResponseEntity<Map<String, Object>> getCategories() {
        Map<String, Object> response = new HashMap<>();
        response.put("data", catalogService.getCategories());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/brands")
    public ResponseEntity<Map<String, Object>> getBrands() {
        Map<String, Object> response = new HashMap<>();
        response.put("data", catalogService.getBrands());
        return ResponseEntity.ok(response);
    }
}
