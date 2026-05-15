package com.seshop.catalog.api;

import com.seshop.catalog.api.dto.CreateProductRequest;
import com.seshop.catalog.api.dto.CreateVariantRequest;
import com.seshop.catalog.api.dto.ProductDto;
import com.seshop.catalog.api.dto.RegisterProductImageRequest;
import com.seshop.catalog.application.CatalogService;
import com.seshop.shared.security.PermissionValidator;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/staff/products")
public class StaffCatalogController {

    private static final String CATALOG_WRITE = "catalog.write";

    private final CatalogService catalogService;
    private final PermissionValidator permissionValidator;

    public StaffCatalogController(CatalogService catalogService, PermissionValidator permissionValidator) {
        this.catalogService = catalogService;
        this.permissionValidator = permissionValidator;
    }

    @PostMapping
    public ResponseEntity<Map<String, Object>> createProduct(@Valid @RequestBody CreateProductRequest request) {
        permissionValidator.require(CATALOG_WRITE);
        ProductDto product = catalogService.createProduct(request);

        Map<String, Object> response = new HashMap<>();
        response.put("data", product);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/{productId}/variants")
    public ResponseEntity<Map<String, Object>> createVariants(
            @PathVariable Long productId,
            @Valid @RequestBody Map<String, List<CreateVariantRequest>> requestMap) {
        permissionValidator.require(CATALOG_WRITE);
        
        List<CreateVariantRequest> variants = requestMap.get("variants");
        ProductDto product = catalogService.createVariants(productId, variants);

        Map<String, Object> response = new HashMap<>();
        response.put("data", product);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{productId}")
    public ResponseEntity<Map<String, Object>> updateProduct(
            @PathVariable Long productId,
            @Valid @RequestBody CreateProductRequest request) {
        permissionValidator.require(CATALOG_WRITE);
        ProductDto product = catalogService.updateProduct(productId, request);

        Map<String, Object> response = new HashMap<>();
        response.put("data", product);

        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<Map<String, Object>> listProducts(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String brand,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        permissionValidator.require(CATALOG_WRITE);
        
        org.springframework.data.domain.Pageable pageable = org.springframework.data.domain.PageRequest.of(page, size);
        org.springframework.data.domain.Page<ProductDto> products = catalogService.getAllProducts(keyword, brand, pageable);

        Map<String, Object> data = new java.util.HashMap<>();
        data.put("items", products.getContent());
        data.put("page", products.getNumber());
        data.put("size", products.getSize());
        data.put("totalElements", products.getTotalElements());
        data.put("totalPages", products.getTotalPages());

        Map<String, Object> response = new java.util.HashMap<>();
        response.put("data", data);

        return ResponseEntity.ok(response);
    }

    @PostMapping("/{productId}/images")
    public ResponseEntity<Map<String, Object>> registerImage(
            @PathVariable Long productId,
            @Valid @RequestBody RegisterProductImageRequest request) {
        permissionValidator.require(CATALOG_WRITE);
        ProductDto product = catalogService.registerImage(productId, request);

        Map<String, Object> response = new HashMap<>();
        response.put("data", product);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/{productId}/images/upload")
    public ResponseEntity<Map<String, Object>> uploadImage(
            @PathVariable Long productId,
            @RequestParam("file") org.springframework.web.multipart.MultipartFile file) {
        permissionValidator.require(CATALOG_WRITE);
        ProductDto product = catalogService.uploadImage(productId, file);

        Map<String, Object> response = new java.util.HashMap<>();
        response.put("data", product);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
