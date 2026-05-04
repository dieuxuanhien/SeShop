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
            @RequestParam Long variantId) {
        
        List<LocationAvailabilityDto> locations = inventoryService.getAvailabilityByVariant(variantId);
        
        Map<String, Object> data = new HashMap<>();
        data.put("productId", productId);
        data.put("variantId", variantId);
        data.put("locations", locations);

        Map<String, Object> response = new HashMap<>();
        response.put("data", data);

        return ResponseEntity.ok(response);
    }
}
