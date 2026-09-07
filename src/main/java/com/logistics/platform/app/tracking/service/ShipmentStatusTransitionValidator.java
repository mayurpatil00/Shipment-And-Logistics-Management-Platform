package com.logistics.platform.app.tracking.service;

import com.logistics.platform.app.shipment.entity.ShipmentStatus;

import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

public final class ShipmentStatusTransitionValidator {

    private static final Map<ShipmentStatus, Set<ShipmentStatus>> ALLOWED = new EnumMap<>(ShipmentStatus.class);

    static {
        ALLOWED.put(ShipmentStatus.CREATED, EnumSet.of(ShipmentStatus.PICKED_UP, ShipmentStatus.CANCELLED));
        ALLOWED.put(ShipmentStatus.PICKED_UP, EnumSet.of(ShipmentStatus.IN_TRANSIT, ShipmentStatus.CANCELLED));
        ALLOWED.put(ShipmentStatus.IN_TRANSIT, EnumSet.of(ShipmentStatus.AT_WAREHOUSE, ShipmentStatus.OUT_FOR_DELIVERY, ShipmentStatus.CANCELLED));
        ALLOWED.put(ShipmentStatus.AT_WAREHOUSE, EnumSet.of(ShipmentStatus.IN_TRANSIT, ShipmentStatus.OUT_FOR_DELIVERY, ShipmentStatus.CANCELLED));
        ALLOWED.put(ShipmentStatus.OUT_FOR_DELIVERY, EnumSet.of(ShipmentStatus.DELIVERED, ShipmentStatus.CANCELLED));
        ALLOWED.put(ShipmentStatus.DELIVERED, EnumSet.noneOf(ShipmentStatus.class));
        ALLOWED.put(ShipmentStatus.CANCELLED, EnumSet.noneOf(ShipmentStatus.class));
    }

    private ShipmentStatusTransitionValidator() {}

    public static boolean isAllowed(ShipmentStatus from, ShipmentStatus to) {
        if (from == to) return false;
        return ALLOWED.getOrDefault(from, EnumSet.noneOf(ShipmentStatus.class)).contains(to);
    }
}
