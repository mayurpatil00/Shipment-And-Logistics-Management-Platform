package com.logistics.platform.app.user.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "permissions", indexes = @Index(name = "idx_permission_name", columnList = "name"))
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Permission {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, unique = true, length = 50)
    private PermissionType name;
}
