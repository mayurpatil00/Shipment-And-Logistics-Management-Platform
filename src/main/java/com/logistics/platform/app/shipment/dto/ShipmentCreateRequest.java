package com.logistics.platform.app.shipment.dto;

import com.logistics.platform.app.common.Address;
import com.logistics.platform.app.shipment.entity.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;

public record ShipmentCreateRequest(
        @NotNull @Valid Address pickupAddress,
        @NotNull @Valid Address deliveryAddress,
        @NotNull @DecimalMin("0.01") BigDecimal weight,
        @DecimalMin("0.01") BigDecimal length,
        @DecimalMin("0.01") BigDecimal width,
        @DecimalMin("0.01") BigDecimal height,
        @NotNull PackageType packageType,
        @NotNull DeliveryType deliveryType,
        @NotNull Long warehouseId
) {}
