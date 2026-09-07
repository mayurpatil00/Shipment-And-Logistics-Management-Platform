package com.logistics.platform.app.delivery.dto;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;


public record DeliveryPartnerCreateRequest(
        @NotBlank @Size(max = 120) String name,
        @NotBlank @Size(max = 20) String phone,
        @NotNull @DecimalMin("0.01") BigDecimal maxCapacity
) {}
