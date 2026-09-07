package com.logistics.platform.app.tracking.dto;

import com.logistics.platform.app.shipment.entity.ShipmentStatus;
import jakarta.validation.constraints.*;

public record TrackingEventRequest(
        @NotNull ShipmentStatus status,
        @NotBlank String location,
        @Size(max = 500) String description
) {}
