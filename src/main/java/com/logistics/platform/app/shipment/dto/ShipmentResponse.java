package com.logistics.platform.app.shipment.dto;

import com.logistics.platform.app.common.Address;
import com.logistics.platform.app.shipment.entity.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public record ShipmentResponse(
        Long id, String trackingNumber,
        Long customerId,
        Address pickupAddress,
        Address deliveryAddress,
        BigDecimal weight,
        BigDecimal length,
        BigDecimal width,
        BigDecimal height,
        PackageType packageType,
        DeliveryType deliveryType,
        BigDecimal price,
        ShipmentStatus status,
        Long deliveryPartnerId,
        Long warehouseId,
        LocalDateTime estimateDeliveryDate,
        String currentLocation,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {}
