package com.seshop.inventory.api;

import com.seshop.inventory.api.dto.ProductVariantDto;
import com.seshop.inventory.application.InventoryService;
import com.seshop.shared.api.ApiResponse;
import com.seshop.shared.security.PermissionValidator;
import org.springframework.web.bind.annotation.*;

/**
 * POS and Inventory lookup endpoints for real-time SKU queries.
 * 
 * References:
 * - POS workflow: UC8 (docs/10.SRS/SESHOP SRS.md)
 * - API Spec: docs/3.Design/SESHOP API Spec.md
 * - Database: product_variants, inventory_balances tables
 */
@RestController
@RequestMapping("/api/v1/staff/inventory")
public class SkuLookupController {

    private static final String INVENTORY_ADJUST = "inventory.adjust";
    private static final String INVENTORY_TRANSFER = "inventory.transfer";
    private static final String POS_SELL = "pos.sell";

    private final InventoryService inventoryService;
    private final PermissionValidator permissionValidator;

    public SkuLookupController(InventoryService inventoryService, PermissionValidator permissionValidator) {
        this.inventoryService = inventoryService;
        this.permissionValidator = permissionValidator;
    }

    /**
     * GET /api/v1/staff/inventory/balances/sku/{skuCode}
     * 
     * Lookup product variant by SKU code for POS barcode scanning.
     * Returns variant details including name, price, and availability across locations.
     * 
     * Used by: POS.tsx handleAddSku() when cashier scans a barcode
     * 
     * @param skuCode the SKU code (e.g., "SKU-001-BLACK-M")
     * @return ProductVariant with id, skuCode, productName, price
     * @throws NotFoundException if SKU does not exist
     */
    @GetMapping("/balances/sku/{skuCode}")
    public ApiResponse<ProductVariantDto> lookupProductBySku(@PathVariable String skuCode) {
        permissionValidator.requireAny(POS_SELL, INVENTORY_ADJUST, INVENTORY_TRANSFER);
        ProductVariantDto variant = inventoryService.getProductVariantBySku(skuCode);
        return ApiResponse.success(variant);
    }
}
