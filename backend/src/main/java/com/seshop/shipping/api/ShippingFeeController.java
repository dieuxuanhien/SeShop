package com.seshop.shipping.api;

import com.seshop.shared.api.ApiResponse;
import com.seshop.shipping.infrastructure.GhnClient;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/shipping")
public class ShippingFeeController {

    private final GhnClient ghnClient;

    public ShippingFeeController(GhnClient ghnClient) {
        this.ghnClient = ghnClient;
    }

    /**
     * POST /api/v1/shipping/estimate-fee
     * Body: { "toAddress": "..." }
     * Returns: { "fee": 25000 }
     */
    @PostMapping("/estimate-fee")
    public ResponseEntity<ApiResponse<Map<String, Object>>> estimateFee(
            @RequestBody Map<String, String> body) {
        String toAddress = body.getOrDefault("toAddress", "");
        long fee = ghnClient.estimateShippingFee(toAddress);
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
