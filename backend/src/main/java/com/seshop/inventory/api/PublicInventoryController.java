package com.seshop.inventory.api;

import com.seshop.inventory.api.dto.LocationAvailabilityDto;
import com.seshop.inventory.application.InventoryService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/products")
public class PublicInventoryController {

    private final InventoryService inventoryService;

    public PublicInventoryController(InventoryService inventoryService) {
        this.inventoryService = inventoryService;
    }

    @GetMapping("/{productId}/availability")
    public ResponseEntity<Map<String, Object>> getProductAvailability(
            @PathVariable Long productId,
            @RequestParam(required = false) Long variantId) {

        List<LocationAvailabilityDto> locations = variantId == null
                ? inventoryService.getAvailabilityByProduct(productId)
                : inventoryService.getAvailabilityByVariant(variantId);

        Map<String, Object> response = new HashMap<>();
        response.put("data", locations);

        return ResponseEntity.ok(response);
    }
}
