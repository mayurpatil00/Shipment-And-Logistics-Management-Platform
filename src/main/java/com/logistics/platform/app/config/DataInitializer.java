package com.logistics.platform.app.config;

import com.logistics.platform.app.user.entity.*;
import com.logistics.platform.app.user.entity.Role;
import com.logistics.platform.app.user.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.*;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.*;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class DataInitializer {
    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder encoder;

    @Value("${app.admin.email:admin@logistics.local}")
    private String adminEmail;

    @Value("${app.admin.default-password:mayurpatil07}")
    private String adminDefaultPassword;

    @Bean
    CommandLineRunner seed() {
        return args -> {
            for (PermissionType p : PermissionType.values()) {
                permissionRepository.findByName(p).orElseGet(() ->
                        permissionRepository.save(Permission.builder().name(p).build()));
            }

            for (RoleType r : RoleType.values()) {
                roleRepository.findByName(r).orElseGet(() ->
                        roleRepository.save(Role.builder().name(r).permissions(new HashSet<>()).build()));
            }

            assignPermissions(RoleType.CUSTOMER, List.of(
                    PermissionType.SHIPMENT_READ, PermissionType.SHIPMENT_CREATE,
                    PermissionType.SHIPMENT_CANCEL, PermissionType.TRACKING_READ));

            assignPermissions(RoleType.OPERATOR, List.of(
                    PermissionType.SHIPMENT_READ, PermissionType.SHIPMENT_CREATE, PermissionType.SHIPMENT_UPDATE,
                    PermissionType.SHIPMENT_CANCEL, PermissionType.TRACKING_READ, PermissionType.TRACKING_WRITE,
                    PermissionType.PARTNER_READ, PermissionType.PARTNER_WRITE,
                    PermissionType.WAREHOUSE_READ, PermissionType.WAREHOUSE_WRITE,
                    PermissionType.PRICING_READ));

            assignPermissions(RoleType.DELIVERY_PARTNER, List.of(
                    PermissionType.SHIPMENT_READ, PermissionType.TRACKING_READ, PermissionType.TRACKING_WRITE));

            Role admin = roleRepository.findByName(RoleType.ADMIN).orElseThrow();
            admin.setPermissions(new HashSet<>(permissionRepository.findAll()));
            roleRepository.save(admin);

            if (!userRepository.existsByEmailIgnoreCase(adminEmail)) {
                String rawPassword = (adminDefaultPassword != null && !adminDefaultPassword.isBlank())
                        ? adminDefaultPassword
                        : UUID.randomUUID().toString();

                userRepository.save(User.builder()
                        .email(adminEmail.toLowerCase().trim())
                        .passwordHash(encoder.encode(rawPassword))
                        .fullName("System Administrator")
                        .roles(new HashSet<>(Set.of(admin)))
                        .build());

                if (adminDefaultPassword == null || adminDefaultPassword.isBlank()) {
                    log.warn("=== No app.admin.default-password set. Generated a one-time admin password for '{}': {} " +
                            "- store it now it will not be shown again. ===", adminEmail, rawPassword);
                }
            }
        };
    }

    private void assignPermissions(RoleType roleType, List<PermissionType> permissionTypes) {
        Role role = roleRepository.findByName(roleType).orElseThrow();
        Set<Permission> permissions = new HashSet<>();
        for (PermissionType type : permissionTypes) {
            permissionRepository.findByName(type).ifPresent(permissions::add);
        }
        role.setPermissions(permissions);
        roleRepository.save(role);
    }
}
