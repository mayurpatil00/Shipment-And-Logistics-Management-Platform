package com.logistics.platform.app.delivery.dto;

import com.logistics.platform.app.delivery.entity.PartnerStatus;
import java.math.BigDecimal;

public record DeliveryPartnerResponse(
        Long id, String name, String phone, boolean active,
        PartnerStatus status, BigDecimal maxCapacity, BigDecimal currentLoad
) {}
