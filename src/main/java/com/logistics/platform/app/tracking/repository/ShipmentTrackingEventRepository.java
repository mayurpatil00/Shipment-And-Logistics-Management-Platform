package com.logistics.platform.app.tracking.repository;

import com.logistics.platform.app.tracking.entity.ShipmentTrackingEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ShipmentTrackingEventRepository extends JpaRepository<ShipmentTrackingEvent, Long> {
    List<ShipmentTrackingEvent> findByShipmentIdOrderByTimestampAsc(Long shipmentId);
}
