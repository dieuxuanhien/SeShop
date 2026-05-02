package com.seshop.identity.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RolePermissionRepository extends JpaRepository<RolePermissionEntity, RolePermissionId> {
    List<RolePermissionEntity> findByRoleId(Long roleId);
    void deleteByRoleId(Long roleId);
}
