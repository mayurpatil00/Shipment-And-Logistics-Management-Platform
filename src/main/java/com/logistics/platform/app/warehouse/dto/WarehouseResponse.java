package com.logistics.platform.app.warehouse.dto;

import java.math.BigDecimal;

public record WarehouseResponse(
        Long id,
        String name,
        String location,
        BigDecimal capacity,
        BigDecimal currentLoad
) {}
