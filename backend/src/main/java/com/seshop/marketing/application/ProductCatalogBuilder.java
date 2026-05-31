package com.seshop.marketing.application;

import com.seshop.catalog.infrastructure.persistence.ProductVariantEntity;
import com.seshop.catalog.infrastructure.persistence.ProductVariantRepository;
import com.seshop.inventory.infrastructure.persistence.InventoryBalanceEntity;
import com.seshop.inventory.infrastructure.persistence.InventoryBalanceRepository;
import jakarta.persistence.EntityManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ProductCatalogBuilder {

    private static final Logger log = LoggerFactory.getLogger(ProductCatalogBuilder.class);
    private static final int MAX_VARIANTS = 50;
    private static final long CACHE_TTL_MS = 5 * 60 * 1000L; // 5 minutes

    private final ProductVariantRepository productVariantRepository;
    private final InventoryBalanceRepository inventoryBalanceRepository;
    private final EntityManager entityManager;

    private volatile String cachedCatalog;
    private volatile long cacheTimestamp;

    public ProductCatalogBuilder(
            ProductVariantRepository productVariantRepository,
            InventoryBalanceRepository inventoryBalanceRepository,
            EntityManager entityManager) {
        this.productVariantRepository = productVariantRepository;
        this.inventoryBalanceRepository = inventoryBalanceRepository;
        this.entityManager = entityManager;
    }

    public String buildCatalogSummary() {
        long now = System.currentTimeMillis();
        if (cachedCatalog != null && (now - cacheTimestamp) < CACHE_TTL_MS) {
            return cachedCatalog;
        }

        try {
            String result = buildCatalogInternal();
            cachedCatalog = result;
            cacheTimestamp = System.currentTimeMillis();
            return result;
        } catch (Exception e) {
            log.error("Failed to build catalog summary", e);
            if (cachedCatalog != null) {
                return cachedCatalog;
            }
            return "Catalog temporarily unavailable.";
        }
    }

    private String buildCatalogInternal() {
        List<ProductVariantEntity> variants = productVariantRepository.findActivePublishedWithProductAndImages();

        StringBuilder sb = new StringBuilder();
        sb.append("Available Products:\n");

        variants.stream()
                .filter(v -> calculateAvailableStock(v.getId()) > 0)
                .sorted(Comparator.comparing(v -> v.getProduct().getName()))
                .limit(MAX_VARIANTS)
                .forEach(variant -> {
                    int stock = calculateAvailableStock(variant.getId());
                    List<String> categoryNames = lookupCategoryNames(variant.getProduct().getId());
                    String attributes = formatAttributes(variant.getAttributes());
                    String categories = categoryNames.isEmpty() ? "Uncategorized" : String.join(", ", categoryNames);

                    sb.append("[VID:").append(variant.getId()).append("] ");
                    sb.append("\"").append(variant.getProduct().getName()).append("\"");
                    if (variant.getProduct().getBrand() != null && !variant.getProduct().getBrand().isEmpty()) {
                        sb.append(" by ").append(variant.getProduct().getBrand());
                    }
                    sb.append(" | $").append(variant.getPrice());
                    if (!attributes.isEmpty()) {
                        sb.append(" | ").append(attributes);
                    }
                    sb.append(" | Categories: ").append(categories);
                    sb.append(" | Stock: ").append(stock);
                    sb.append("\n");
                });

        return sb.toString();
    }

    private int calculateAvailableStock(Long variantId) {
        return inventoryBalanceRepository.findByVariantId(variantId).stream()
                .mapToInt(b -> b.getOnHandQty() - b.getReservedQty())
                .sum();
    }

    @SuppressWarnings("unchecked")
    private List<String> lookupCategoryNames(Long productId) {
        try {
            return entityManager.createQuery(
                    "SELECT c.name FROM CategoryEntity c " +
                    "JOIN ProductCategoryEntity pc ON pc.categoryId = c.id " +
                    "WHERE pc.productId = :productId")
                    .setParameter("productId", productId)
                    .getResultList();
        } catch (Exception e) {
            log.debug("Could not fetch categories for product {}", productId, e);
            return List.of();
        }
    }

    private String formatAttributes(Map<String, String> attributes) {
        if (attributes == null || attributes.isEmpty()) {
            return "";
        }
        return attributes.entrySet().stream()
                .map(e -> e.getKey() + ": " + e.getValue())
                .collect(Collectors.joining(", "));
    }
}
