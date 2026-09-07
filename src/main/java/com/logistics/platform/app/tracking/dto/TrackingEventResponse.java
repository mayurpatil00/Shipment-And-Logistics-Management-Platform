package com.logistics.platform.app.tracking.dto;

import com.logistics.platform.app.shipment.entity.ShipmentStatus;
import java.time.LocalDateTime;

public record TrackingEventResponse(Long id,
                                    ShipmentStatus status,
                                    String location,
                                    String description,
                                    LocalDateTime timestamp) {}
