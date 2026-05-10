package com.seshop.review.infrastructure.persistence;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ReviewRepository extends JpaRepository<ReviewEntity, Long> {

    Optional<ReviewEntity> findByOrderItemIdAndCustomerUserId(Long orderItemId, Long customerUserId);

    @Query("""
            select review
            from ReviewEntity review
            join OrderItemEntity orderItem on orderItem.id = review.orderItemId
            join ProductVariantEntity variant on variant.id = orderItem.variantId
            where variant.product.id = :productId
              and review.status = 'PUBLISHED'
            order by review.createdAt desc
            """)
    List<ReviewEntity> findPublishedByProductId(@Param("productId") Long productId);
}
