package com.logistics.platform.app.warehouse.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record WarehouseCreateRequest(
        @NotBlank(message = "Warehouse name is required") String name,
        @NotBlank(message = "Location is required") String location,
        @NotNull(message = "Capacity is required")
        @DecimalMin(value = "0.01", message = "Capacity must be greater than zero") BigDecimal capacity
) {}
