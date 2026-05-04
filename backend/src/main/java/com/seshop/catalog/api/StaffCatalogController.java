package com.seshop.catalog.api;

import com.seshop.catalog.api.dto.CreateProductRequest;
import com.seshop.catalog.api.dto.CreateVariantRequest;
import com.seshop.catalog.api.dto.ProductDto;
import com.seshop.catalog.application.CatalogService;
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

    private final CatalogService catalogService;

    public StaffCatalogController(CatalogService catalogService) {
        this.catalogService = catalogService;
    }

    @PostMapping
    public ResponseEntity<Map<String, Object>> createProduct(@Valid @RequestBody CreateProductRequest request) {
        ProductDto product = catalogService.createProduct(request);

        Map<String, Object> response = new HashMap<>();
        response.put("data", product);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/{productId}/variants")
    public ResponseEntity<Map<String, Object>> createVariants(
            @PathVariable Long productId,
            @Valid @RequestBody Map<String, List<CreateVariantRequest>> requestMap) {
        
        List<CreateVariantRequest> variants = requestMap.get("variants");
        ProductDto product = catalogService.createVariants(productId, variants);

        Map<String, Object> response = new HashMap<>();
        response.put("data", product);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
