package com.seshop.catalog.application;

import com.seshop.audit.application.AuditService;
import com.seshop.audit.domain.AuditAction;
import com.seshop.catalog.api.dto.CategoryDto;
import com.seshop.catalog.api.dto.CreateProductRequest;
import com.seshop.catalog.api.dto.CreateVariantRequest;
import com.seshop.catalog.api.dto.ProductDto;
import com.seshop.catalog.api.dto.RegisterProductImageRequest;
import com.seshop.catalog.infrastructure.persistence.*;
import com.seshop.review.infrastructure.persistence.ReviewRepository;
import com.seshop.shared.exception.BusinessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Transactional
public class CatalogService {

    private final ProductRepository productRepository;
    private final ProductVariantRepository productVariantRepository;
    private final CategoryRepository categoryRepository;
    private final com.seshop.shared.util.FileStorageService fileStorageService;
    private final AuditService auditService;
    private final ReviewRepository reviewRepository;

    public CatalogService(ProductRepository productRepository,
            ProductVariantRepository productVariantRepository,
            CategoryRepository categoryRepository,
            com.seshop.shared.util.FileStorageService fileStorageService,
            AuditService auditService,
            ReviewRepository reviewRepository) {
        this.productRepository = productRepository;
        this.productVariantRepository = productVariantRepository;
        this.categoryRepository = categoryRepository;
        this.fileStorageService = fileStorageService;
        this.auditService = auditService;
        this.reviewRepository = reviewRepository;
    }

    public ProductDto createProduct(CreateProductRequest request) {
        ProductEntity entity = new ProductEntity();
        entity.setName(request.getName());
        entity.setBrand(request.getBrand());
        entity.setDescription(request.getDescription());
        entity.setStatus(request.getStatus());

        ProductEntity saved = productRepository.save(entity);

        Map<String, Object> metadata = new LinkedHashMap<>();
        metadata.put("productId", saved.getId());
        metadata.put("name", saved.getName());
        metadata.put("brand", saved.getBrand());
        metadata.put("status", saved.getStatus());
        auditService.write(AuditAction.PRODUCT_CREATED, "Product", saved.getId().toString(), metadata);

        return mapToDto(saved);
    }

    public ProductDto updateProduct(Long productId, CreateProductRequest request) {
        ProductEntity entity = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("Product not found"));

        // capture before-state for audit
        Map<String, Object> before = new LinkedHashMap<>();
        before.put("name", entity.getName());
        before.put("brand", entity.getBrand());
        before.put("status", entity.getStatus());

        entity.setName(request.getName());
        entity.setBrand(request.getBrand());
        entity.setDescription(request.getDescription());
        entity.setStatus(request.getStatus());
        ProductEntity saved = productRepository.save(entity);

        Map<String, Object> after = new LinkedHashMap<>();
        after.put("name", saved.getName());
        after.put("brand", saved.getBrand());
        after.put("status", saved.getStatus());

        Map<String, Object> metadata = new LinkedHashMap<>();
        metadata.put("productId", productId);
        metadata.put("before", before);
        metadata.put("after", after);
        auditService.write(AuditAction.PRODUCT_UPDATED, "Product", productId.toString(), metadata);

        return mapToDto(saved);
    }

    public ProductDto createVariants(Long productId, List<CreateVariantRequest> requests) {
        ProductEntity product = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("Product not found"));

        for (CreateVariantRequest request : requests) {
            // BR14: SKU code must be unique across all products
            productVariantRepository.findBySkuCode(request.getSkuCode()).ifPresent(existing -> {
                throw new BusinessException("CAT_002",
                        "SKU code '" + request.getSkuCode() + "' is already in use");
            });

            ProductVariantEntity variant = new ProductVariantEntity();
            variant.setProduct(product);
            variant.setSkuCode(request.getSkuCode());
            variant.setSize(request.getSize());
            variant.setColor(request.getColor());
            variant.setPrice(request.getPrice());
            variant.setStatus(request.getStatus());

            product.getVariants().add(variant);
        }

        ProductEntity saved = productRepository.save(product);
        return mapToDto(saved);
    }

    public ProductDto deleteVariant(Long productId, Long variantId) {
        ProductEntity product = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("Product not found"));

        ProductVariantEntity variant = product.getVariants().stream()
                .filter(candidate -> candidate.getId().equals(variantId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Variant not found"));

        variant.setStatus("INACTIVE");
        ProductEntity saved = productRepository.save(product);

        Map<String, Object> metadata = new LinkedHashMap<>();
        metadata.put("productId", productId);
        metadata.put("variantId", variantId);
        metadata.put("skuCode", variant.getSkuCode());
        metadata.put("status", variant.getStatus());
        auditService.write(AuditAction.PRODUCT_UPDATED, "ProductVariant", variantId.toString(), metadata);

        return mapToDto(saved);
    }

    public ProductDto registerImage(Long productId, RegisterProductImageRequest request) {
        ProductEntity product = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("Product not found"));
        ProductImageEntity image = new ProductImageEntity();
        image.setProduct(product);
        image.setUrl(request.getUrl());
        image.setSortOrder(request.getSortOrder() == null ? product.getImages().size() : request.getSortOrder());
        image.setIsInstagramReady(Boolean.TRUE.equals(request.getIsInstagramReady()));
        product.getImages().add(image);
        return mapToDto(productRepository.save(product));
    }

    public ProductDto uploadImage(Long productId, org.springframework.web.multipart.MultipartFile file) {
        ProductEntity product = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("Product not found"));

        String url = fileStorageService.store(file);

        ProductImageEntity image = new ProductImageEntity();
        image.setProduct(product);
        image.setUrl(url);
        image.setSortOrder(product.getImages().size());
        image.setIsInstagramReady(true);
        product.getImages().add(image);

        return mapToDto(productRepository.save(product));
    }

    @Transactional(readOnly = true)
    public Page<ProductDto> getAllProducts(String keyword, String brand, Pageable pageable) {
        return productRepository.findByFilters(normalizeFilter(keyword), normalizeFilter(brand), pageable)
                .map(this::mapToDto);
    }

    @Transactional(readOnly = true)
    public Page<ProductDto> getPublishedProducts(String keyword, String brand, Pageable pageable) {
        return productRepository.findPublishedProducts(normalizeFilter(keyword), normalizeFilter(brand), pageable)
                .map(this::mapToDto);
    }

    /**
     * Extended browse supporting category, size, color, and price range filters.
     * UC13: Browse and filter products/variants.
     */
    @Transactional(readOnly = true)
    public Page<ProductDto> getPublishedProductsFiltered(String keyword, String brand,
            Long categoryId, String size, String color,
            BigDecimal minPrice, BigDecimal maxPrice,
            Pageable pageable) {
        return productRepository.findPublishedProductsFiltered(
                normalizeFilter(keyword),
                normalizeFilter(brand),
                categoryId,
                normalizeFilter(size),
                normalizeFilter(color),
                minPrice,
                maxPrice,
                pageable)
                .map(this::mapToDto);
    }

    @Transactional(readOnly = true)
    public ProductDto getProductById(Long productId) {
        ProductEntity product = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("Product not found"));
        return mapToDto(product);
    }

    @Transactional(readOnly = true)
    public List<CategoryDto> getCategories() {
        return categoryRepository.findAll().stream()
                .map(category -> {
                    CategoryDto dto = new CategoryDto();
                    dto.setId(category.getId());
                    dto.setName(category.getName());
                    dto.setSlug(
                            category.getName().toLowerCase().replaceAll("[^a-z0-9]+", "-").replaceAll("(^-|-$)", ""));
                    return dto;
                })
                .toList();
    }

    @Transactional(readOnly = true)
    public List<String> getBrands() {
        return productRepository.findDistinctBrands();
    }

    private ProductDto mapToDto(ProductEntity entity) {
        ProductDto dto = new ProductDto();
        dto.setId(entity.getId());
        dto.setName(entity.getName());
        dto.setBrand(entity.getBrand());
        dto.setDescription(entity.getDescription());
        dto.setStatus(entity.getStatus());

        if (entity.getVariants() != null) {
            dto.setVariants(entity.getVariants().stream().map(v -> {
                ProductDto.VariantDto vDto = new ProductDto.VariantDto();
                vDto.setId(v.getId());
                vDto.setSkuCode(v.getSkuCode());
                vDto.setSize(v.getSize());
                vDto.setColor(v.getColor());
                vDto.setPrice(v.getPrice());
                vDto.setStatus(v.getStatus());
                return vDto;
            }).collect(Collectors.toList()));
        }
        if (entity.getImages() != null) {
            dto.setImages(entity.getImages().stream().map(image -> {
                ProductDto.ImageDto imageDto = new ProductDto.ImageDto();
                imageDto.setId(image.getId());
                imageDto.setUrl(image.getUrl());
                imageDto.setSortOrder(image.getSortOrder());
                imageDto.setInstagramReady(image.getIsInstagramReady());
                return imageDto;
            }).collect(Collectors.toList()));
        }

        // Aggregate review stats (UC14)
        if (entity.getId() != null) {
            dto.setAverageRating(reviewRepository.averageRatingByProductId(entity.getId()));
            dto.setReviewCount(reviewRepository.countPublishedByProductId(entity.getId()));
        }

        return dto;
    }

    private String normalizeFilter(String value) {
        return value == null || value.isBlank() ? "" : value.trim();
    }
}
