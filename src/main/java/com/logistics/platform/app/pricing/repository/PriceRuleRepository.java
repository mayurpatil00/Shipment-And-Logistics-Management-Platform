package com.logistics.platform.app.pricing.repository;

import com.logistics.platform.app.pricing.entity.PricingRule;
import com.logistics.platform.app.shipment.entity.DeliveryType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PriceRuleRepository extends JpaRepository<PricingRule, Long> {
    Optional<PricingRule> findByDeliveryTypeAndActiveTrue(DeliveryType deliveryType);
}
