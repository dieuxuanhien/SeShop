package com.seshop.catalog.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductVariantRepository extends JpaRepository<ProductVariantEntity, Long> {
    Optional<ProductVariantEntity> findBySkuCode(String skuCode);

    @Query("SELECT v FROM ProductVariantEntity v " +
           "JOIN FETCH v.product p " +
           "LEFT JOIN FETCH p.images " +
           "WHERE v.status = 'ACTIVE' AND p.status = 'PUBLISHED'")
    List<ProductVariantEntity> findActivePublishedWithProductAndImages();
}
