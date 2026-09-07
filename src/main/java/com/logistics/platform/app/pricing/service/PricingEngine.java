package com.logistics.platform.app.pricing.service;

import com.logistics.platform.app.exception.BadRequestException;
import com.logistics.platform.app.pricing.entity.PricingRule;
import com.logistics.platform.app.pricing.repository.PriceRuleRepository;
import com.logistics.platform.app.shipment.entity.DeliveryType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class PricingEngine {
    private final PriceRuleRepository repository;

    public BigDecimal calculatePrice(BigDecimal weight, DeliveryType type, BigDecimal distanceKm) {

        if (weight == null || weight.signum() <= 0) {
            throw new BadRequestException("Weight must be greater than zero");
        }
        if (distanceKm == null || distanceKm.signum() < 0) {
            throw new BadRequestException("Distance cannot be negative");
        }

        PricingRule rule = repository.findByDeliveryTypeAndActiveTrue(type)
                .orElseThrow(() -> new IllegalStateException("No active pricing rule found for " + type));

        return rule.getBasePrice()
                .add(weight.multiply(rule.getPricePerKg()))
                .add(distanceKm.multiply(rule.getPricePerKm()));
    }
}
