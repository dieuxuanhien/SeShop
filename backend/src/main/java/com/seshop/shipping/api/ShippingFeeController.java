package com.seshop.shipping.api;

import com.seshop.shared.api.ApiResponse;
import com.seshop.shipping.infrastructure.GhnClient;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import com.seshop.catalog.infrastructure.persistence.ProductVariantEntity;
import com.seshop.catalog.infrastructure.persistence.ProductVariantRepository;
import com.seshop.commerce.infrastructure.persistence.CartEntity;
import com.seshop.commerce.infrastructure.persistence.CartItemEntity;
import com.seshop.commerce.infrastructure.persistence.CartRepository;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/shipping")
public class ShippingFeeController {

    private final GhnClient ghnClient;
    private final CartRepository cartRepository;
    private final ProductVariantRepository variantRepository;

    public ShippingFeeController(GhnClient ghnClient, CartRepository cartRepository, ProductVariantRepository variantRepository) {
        this.ghnClient = ghnClient;
        this.cartRepository = cartRepository;
        this.variantRepository = variantRepository;
    }

    /**
     * POST /api/v1/shipping/estimate-fee
     * Body: { "toAddress": "..." }
     * Returns: { "fee": 25000 }
     */
    @PostMapping("/estimate-fee")
    public ResponseEntity<ApiResponse<Map<String, Object>>> estimateFee(
            @RequestBody Map<String, Object> body) {
        Object toDistrictIdObj = body.get("toDistrictId");
        Integer toDistrictId = toDistrictIdObj != null ? Integer.valueOf(toDistrictIdObj.toString()) : null;
        Object toWardCodeObj = body.get("toWardCode");
        String toWardCode = toWardCodeObj != null ? toWardCodeObj.toString() : null;
        Object cartIdObj = body.get("cartId");
        Long cartId = cartIdObj != null ? Long.valueOf(cartIdObj.toString()) : null;

        int totalWeight = 0;
        if (cartId != null) {
            CartEntity cart = cartRepository.findById(cartId).orElse(null);
            if (cart != null) {
                for (CartItemEntity item : cart.getItems()) {
                    ProductVariantEntity variant = variantRepository.findById(item.getVariantId()).orElse(null);
                    if (variant != null) {
                        Map<String, String> attrs = variant.getAttributes();
                        int weight = 200;
                        if (attrs != null && attrs.containsKey("weight_grams")) {
                            try {
                                weight = Integer.parseInt(attrs.get("weight_grams"));
                            } catch (NumberFormatException ignored) {}
                        }
                        totalWeight += weight * item.getQty();
                    }
                }
            }
        }
        if (totalWeight == 0) totalWeight = 200;

        long fee = ghnClient.estimateShippingFee(toDistrictId, toWardCode, totalWeight);
        return ResponseEntity.ok(ApiResponse.success(Map.of("fee", fee)));
    }

    /**
     * POST /api/v1/shipping/validate-address
     * Body: { "ward": "...", "district": "...", "city": "..." }
     * Returns: { "valid": true, "message": "..." }
     */
    @PostMapping("/validate-address")
    public ResponseEntity<ApiResponse<Map<String, Object>>> validateAddress(
            @RequestBody Map<String, String> body) {
        String ward = body.getOrDefault("ward", "");
        String district = body.getOrDefault("district", "");
        String city = body.getOrDefault("city", "");

        if (!StringUtils.hasText(ward) || !StringUtils.hasText(district) || !StringUtils.hasText(city)) {
            return ResponseEntity.ok(ApiResponse.success(Map.of(
                    "valid", false,
                    "message", "Ward, district, and city are required"
            )));
        }

        boolean valid = ghnClient.validateAddress(ward, district, city);
        String message = valid
                ? "Address is valid and can be serviced by GHN"
                : "Address could not be validated. Please check ward, district, and city names.";
        return ResponseEntity.ok(ApiResponse.success(Map.of("valid", valid, "message", message)));
    }

    /**
     * GET /api/v1/shipping/provinces
     */
    @GetMapping("/provinces")
    public ResponseEntity<ApiResponse<java.util.List<Map<String, Object>>>> getProvinces() {
        return ResponseEntity.ok(ApiResponse.success(ghnClient.getProvinces()));
    }

    /**
     * GET /api/v1/shipping/districts?provinceId=...
     */
    @GetMapping("/districts")
    public ResponseEntity<ApiResponse<java.util.List<Map<String, Object>>>> getDistricts(
            @RequestParam(defaultValue = "202") int provinceId) {
        return ResponseEntity.ok(ApiResponse.success(ghnClient.getDistricts(provinceId)));
    }

    /**
     * GET /api/v1/shipping/wards?districtId=...
     */
    @GetMapping("/wards")
    public ResponseEntity<ApiResponse<java.util.List<Map<String, Object>>>> getWards(
            @RequestParam(defaultValue = "1442") int districtId) {
        return ResponseEntity.ok(ApiResponse.success(ghnClient.getWards(districtId)));
    }
}
