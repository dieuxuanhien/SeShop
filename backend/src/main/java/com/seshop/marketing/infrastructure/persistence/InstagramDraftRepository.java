package com.seshop.marketing.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface InstagramDraftRepository extends JpaRepository<InstagramDraftEntity, Long> {
}
