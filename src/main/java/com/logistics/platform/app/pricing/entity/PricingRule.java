package com.logistics.platform.app.pricing.entity;

import com.logistics.platform.app.shipment.entity.DeliveryType;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;

@Entity
@Table(name = "pricing_rules")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class PricingRule {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, unique = true, length = 30)
    private DeliveryType deliveryType;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal basePrice;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal pricePerKg;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal pricePerKm;

    @Column(nullable = false)
    @Builder.Default
    private boolean active = true;
}
