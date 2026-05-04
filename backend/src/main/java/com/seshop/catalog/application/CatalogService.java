package com.seshop.catalog.application;

import com.seshop.catalog.api.dto.CreateProductRequest;
import com.seshop.catalog.api.dto.CreateVariantRequest;
import com.seshop.catalog.api.dto.ProductDto;
import com.seshop.catalog.infrastructure.persistence.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class CatalogService {

    private final ProductRepository productRepository;
    private final ProductVariantRepository productVariantRepository;
    private final CategoryRepository categoryRepository;

    public CatalogService(ProductRepository productRepository, 
                          ProductVariantRepository productVariantRepository,
                          CategoryRepository categoryRepository) {
        this.productRepository = productRepository;
        this.productVariantRepository = productVariantRepository;
        this.categoryRepository = categoryRepository;
    }

    public ProductDto createProduct(CreateProductRequest request) {
        ProductEntity entity = new ProductEntity();
        entity.setName(request.getName());
        entity.setBrand(request.getBrand());
        entity.setDescription(request.getDescription());
        entity.setStatus(request.getStatus());

        ProductEntity saved = productRepository.save(entity);
        return mapToDto(saved);
    }

    public ProductDto createVariants(Long productId, List<CreateVariantRequest> requests) {
        ProductEntity product = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("Product not found"));

        for (CreateVariantRequest request : requests) {
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

    @Transactional(readOnly = true)
    public Page<ProductDto> getPublishedProducts(String keyword, String brand, Pageable pageable) {
        return productRepository.findPublishedProducts(keyword, brand, pageable)
                .map(this::mapToDto);
    }
    
    @Transactional(readOnly = true)
    public ProductDto getProductById(Long productId) {
        ProductEntity product = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("Product not found"));
        return mapToDto(product);
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
        
        return dto;
    }
}
