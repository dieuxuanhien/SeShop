package com.seshop.catalog.infrastructure.persistence;

import java.math.BigDecimal;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductRepository extends JpaRepository<ProductEntity, Long> {

    @Query("SELECT p FROM ProductEntity p WHERE " +
           "(:keyword = '' OR LOWER(p.name) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
           "AND (:brand = '' OR LOWER(p.brand) = LOWER(:brand))")
    Page<ProductEntity> findByFilters(
            @Param("keyword") String keyword,
            @Param("brand") String brand,
            Pageable pageable);

    @Query("SELECT p FROM ProductEntity p WHERE p.status = 'PUBLISHED' " +
           "AND (:keyword = '' OR LOWER(p.name) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
           "AND (:brand = '' OR LOWER(p.brand) = LOWER(:brand))")
    Page<ProductEntity> findPublishedProducts(
            @Param("keyword") String keyword,
            @Param("brand") String brand,
            Pageable pageable);

    /**
     * Extended browse query supporting category, variant-level size/color, and
     * price range filters.  Only returns PUBLISHED products.
     */
    @Query("SELECT DISTINCT p FROM ProductEntity p " +
           "LEFT JOIN p.variants v " +
           "LEFT JOIN ProductCategoryEntity pc ON pc.productId = p.id " +
           "WHERE p.status = 'PUBLISHED' " +
           "AND (:keyword = '' OR LOWER(p.name) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
           "AND (:brand = '' OR LOWER(p.brand) = LOWER(:brand)) " +
           "AND (:categoryId IS NULL OR pc.categoryId = :categoryId) " +
           "AND (:size = '' OR LOWER(v.size) = LOWER(:size)) " +
           "AND (:color = '' OR LOWER(v.color) = LOWER(:color)) " +
           "AND (:minPrice IS NULL OR v.price >= :minPrice) " +
           "AND (:maxPrice IS NULL OR v.price <= :maxPrice)")
    Page<ProductEntity> findPublishedProductsFiltered(
            @Param("keyword") String keyword,
            @Param("brand") String brand,
            @Param("categoryId") Long categoryId,
            @Param("size") String size,
            @Param("color") String color,
            @Param("minPrice") BigDecimal minPrice,
            @Param("maxPrice") BigDecimal maxPrice,
            Pageable pageable);
}
