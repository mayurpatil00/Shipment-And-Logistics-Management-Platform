package com.logistics.platform.app.user.repository;

import com.logistics.platform.app.user.entity.Role;
import com.logistics.platform.app.user.entity.RoleType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {
    Optional<Role> findByName(RoleType name);
}
