package com.logistics.platform.app.pricing.config;

import com.logistics.platform.app.pricing.entity.PricingRule;
import com.logistics.platform.app.pricing.repository.PriceRuleRepository;
import com.logistics.platform.app.shipment.entity.DeliveryType;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.math.BigDecimal;

@Configuration
public class PricingDataInitializer {

    @Bean
    CommandLineRunner initPricingRules(PriceRuleRepository repository) {
        return args -> {
            for (DeliveryType type : DeliveryType.values()) {
                if (repository.findByDeliveryTypeAndActiveTrue(type).isEmpty()) {
                    PricingRule rule = PricingRule.builder()
                            .deliveryType(type)
                            .basePrice(new BigDecimal("100.00"))
                            .pricePerKg(new BigDecimal("20.00"))
                            .pricePerKm(new BigDecimal("5.00"))
                            .active(true)
                            .build();
                    repository.save(rule);
                }
            }
        };
    }
}