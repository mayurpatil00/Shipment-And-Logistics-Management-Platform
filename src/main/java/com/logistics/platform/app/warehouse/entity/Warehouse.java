package com.logistics.platform.app.warehouse.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;

@Entity
@Table(name = "warehouses",
        uniqueConstraints = @UniqueConstraint(name = "uk_warehouse_name",
                columnNames = "name"))
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Warehouse {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 120)
    private String name;

    @Column(nullable = false, length = 255)
    private String location;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal capacity;

    @Column(nullable = false, precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal currentLoad = BigDecimal.ZERO;

    @Version
    private Long version;
}
