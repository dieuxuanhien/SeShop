package com.seshop.catalog.infrastructure.persistence;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductRepository extends JpaRepository<ProductEntity, Long> {
    
    @Query("SELECT p FROM ProductEntity p WHERE p.status = 'PUBLISHED' " +
           "AND (:keyword = '' OR LOWER(p.name) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
           "AND (:brand = '' OR LOWER(p.brand) = LOWER(:brand))")
    Page<ProductEntity> findPublishedProducts(
            @Param("keyword") String keyword,
            @Param("brand") String brand,
            Pageable pageable);
}
