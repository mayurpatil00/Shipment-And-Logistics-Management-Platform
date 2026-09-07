package com.logistics.platform.app.user.repository;

import com.logistics.platform.app.user.entity.Permission;
import com.logistics.platform.app.user.entity.PermissionType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PermissionRepository extends JpaRepository<Permission, Long> {
    Optional<Permission> findByName(PermissionType name);
}
