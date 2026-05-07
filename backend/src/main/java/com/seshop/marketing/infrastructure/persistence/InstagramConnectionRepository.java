package com.seshop.marketing.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface InstagramConnectionRepository extends JpaRepository<InstagramConnectionEntity, Long> {
    Optional<InstagramConnectionEntity> findByUserIdAndAccountId(Long userId, String accountId);
    Optional<InstagramConnectionEntity> findByUserId(Long userId);
}
