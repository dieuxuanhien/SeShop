package com.seshop.catalog.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

import com.seshop.catalog.api.dto.CategoryDto;
import com.seshop.catalog.api.dto.CreateProductRequest;
import com.seshop.catalog.api.dto.CreateVariantRequest;
import com.seshop.catalog.api.dto.ProductDto;
import com.seshop.catalog.infrastructure.persistence.CategoryEntity;
import com.seshop.catalog.infrastructure.persistence.CategoryRepository;
import com.seshop.catalog.infrastructure.persistence.ProductEntity;
import com.seshop.catalog.infrastructure.persistence.ProductRepository;
import com.seshop.catalog.infrastructure.persistence.ProductVariantRepository;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

/**
 * UC5: Add product and SKUs – BR13 (mandatory fields), BR14 (SKU uniqueness), BR15 (media).
 * UC13: Browse and filter variants.
 */
@ExtendWith(MockitoExtension.class)
class CatalogServiceTest {

    @Mock
    private ProductRepository productRepository;
    @Mock
    private ProductVariantRepository productVariantRepository;
    @Mock
    private CategoryRepository categoryRepository;

    private CatalogService service;

    @BeforeEach
    void setUp() {
        service = new CatalogService(productRepository, productVariantRepository, categoryRepository);
    }

    // ── createProduct ───────────────────────────────────────────────────────

    @Test
    void createProductPersistsAndReturnsDto() {
        ProductEntity saved = productEntity(10L, "Linen Shirt", "SeShop", "DRAFT");
        given(productRepository.save(any(ProductEntity.class))).willReturn(saved);

        CreateProductRequest request = new CreateProductRequest();
        request.setName("Linen Shirt");
        request.setBrand("SeShop");
        request.setDescription("Light summer fabric");
        request.setStatus("DRAFT");

        ProductDto result = service.createProduct(request);

        assertThat(result.getId()).isEqualTo(10L);
        assertThat(result.getName()).isEqualTo("Linen Shirt");
        assertThat(result.getStatus()).isEqualTo("DRAFT");
    }

    // ── updateProduct ───────────────────────────────────────────────────────

    @Test
    void updateProductModifiesExistingEntity() {
        ProductEntity existing = productEntity(10L, "Old Name", "Brand", "DRAFT");
        given(productRepository.findById(10L)).willReturn(Optional.of(existing));
        given(productRepository.save(any())).willAnswer(inv -> inv.getArgument(0));

        CreateProductRequest request = new CreateProductRequest();
        request.setName("New Name");
        request.setBrand("NewBrand");
        request.setDescription("Updated");
        request.setStatus("PUBLISHED");

        ProductDto result = service.updateProduct(10L, request);

        assertThat(result.getName()).isEqualTo("New Name");
        assertThat(result.getStatus()).isEqualTo("PUBLISHED");
    }

    @Test
    void updateProductThrowsWhenNotFound() {
        given(productRepository.findById(99L)).willReturn(Optional.empty());

        CreateProductRequest request = new CreateProductRequest();
        request.setName("X");
        request.setBrand("Y");
        request.setStatus("DRAFT");

        assertThatThrownBy(() -> service.updateProduct(99L, request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("not found");
    }

    // ── createVariants ──────────────────────────────────────────────────────

    @Test
    void createVariantsAddsVariantsToExistingProduct() {
        ProductEntity existing = productEntity(10L, "Shirt", "Brand", "DRAFT");
        given(productRepository.findById(10L)).willReturn(Optional.of(existing));
        given(productRepository.save(any())).willAnswer(inv -> inv.getArgument(0));

        CreateVariantRequest variantRequest = new CreateVariantRequest();
        variantRequest.setSkuCode("SKU-SHIRT-M");
        variantRequest.setSize("M");
        variantRequest.setColor("White");
        variantRequest.setPrice(new BigDecimal("590000"));
        variantRequest.setStatus("ACTIVE");

        ProductDto result = service.createVariants(10L, List.of(variantRequest));

        assertThat(result.getVariants()).hasSize(1);
        assertThat(result.getVariants().get(0).getSkuCode()).isEqualTo("SKU-SHIRT-M");
    }

    // ── getPublishedProducts ────────────────────────────────────────────────

    @Test
    void getPublishedProductsReturnsPagedResults() {
        ProductEntity p = productEntity(1L, "Vintage Tee", "Hanes", "PUBLISHED");
        given(productRepository.findPublishedProducts(null, null, PageRequest.of(0, 10)))
                .willReturn(new PageImpl<>(List.of(p)));

        var page = service.getPublishedProducts(null, null, PageRequest.of(0, 10));

        assertThat(page.getContent()).hasSize(1);
        assertThat(page.getContent().get(0).getName()).isEqualTo("Vintage Tee");
    }

    // ── getCategories ───────────────────────────────────────────────────────

    @Test
    void getCategoriesReturnsMappedDtos() {
        CategoryEntity cat = new CategoryEntity();
        cat.setId(5L);
        cat.setName("Band Tees");
        cat.setDescription("Vintage band T-shirts");
        given(categoryRepository.findAll()).willReturn(List.of(cat));

        List<CategoryDto> result = service.getCategories();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("Band Tees");
    }

    // ── getProductById ──────────────────────────────────────────────────────

    @Test
    void getProductByIdThrowsWhenNotFound() {
        given(productRepository.findById(404L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> service.getProductById(404L))
                .isInstanceOf(IllegalArgumentException.class);
    }

    // ── helpers ─────────────────────────────────────────────────────────────

    private ProductEntity productEntity(Long id, String name, String brand, String status) {
        ProductEntity e = new ProductEntity();
        e.setId(id);
        e.setName(name);
        e.setBrand(brand);
        e.setStatus(status);
        return e;
    }
}
