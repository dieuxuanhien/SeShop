package com.seshop.marketing.api;

import com.seshop.marketing.api.dto.DiscountDto;
import com.seshop.marketing.api.dto.DiscountValidateRequest;
import com.seshop.marketing.api.dto.DiscountValidationResponse;
import com.seshop.marketing.application.DiscountService;
import com.seshop.shared.api.ApiResponse;
import com.seshop.shared.security.PermissionValidator;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1")
public class DiscountController {

    private static final String PROMO_MANAGE = "promo.manage";

    private final DiscountService discountService;
    private final PermissionValidator permissionValidator;

    public DiscountController(DiscountService discountService, PermissionValidator permissionValidator) {
        this.discountService = discountService;
        this.permissionValidator = permissionValidator;
    }

    @PostMapping("/discounts/validate")
    public ApiResponse<DiscountValidationResponse> validateDiscount(@RequestBody DiscountValidateRequest request) {
        return ApiResponse.success(discountService.validateDiscount(request));
    }

    @PostMapping("/staff/discounts")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<DiscountDto> createDiscount(@RequestBody DiscountDto request) {
        permissionValidator.require(PROMO_MANAGE);
        return ApiResponse.success(discountService.createDiscount(request));
    }

    @GetMapping("/staff/discounts")
    public ApiResponse<List<DiscountDto>> listDiscounts() {
        permissionValidator.require(PROMO_MANAGE);
        return ApiResponse.success(discountService.listDiscounts());
    }

    @PutMapping("/staff/discounts/{discountId}")
    public ApiResponse<DiscountDto> updateDiscount(@PathVariable Long discountId, @RequestBody DiscountDto request) {
        permissionValidator.require(PROMO_MANAGE);
        return ApiResponse.success(discountService.updateDiscount(discountId, request));
    }

    @DeleteMapping("/staff/discounts/{discountId}")
    public ApiResponse<Void> deactivateDiscount(@PathVariable Long discountId) {
        permissionValidator.require(PROMO_MANAGE);
        discountService.deactivateDiscount(discountId);
        return ApiResponse.success(null);
    }
}
