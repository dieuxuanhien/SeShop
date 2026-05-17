package com.seshop.refund.infrastructure.persistence;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ReturnItemRepository extends JpaRepository<ReturnItemEntity, Long> {
    List<ReturnItemEntity> findByReturnRequest_Id(Long returnRequestId);
}
