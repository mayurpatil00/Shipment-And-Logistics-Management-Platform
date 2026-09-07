package com.logistics.platform.app.tracking.service;

import com.logistics.platform.app.exception.BusinessException;
import com.logistics.platform.app.exception.ResourceNotFoundException;
import com.logistics.platform.app.shipment.entity.Shipment;
import com.logistics.platform.app.shipment.entity.ShipmentStatus;
import com.logistics.platform.app.shipment.repository.ShipmentRepository;
import com.logistics.platform.app.shipment.service.ShipmentService;
import com.logistics.platform.app.tracking.dto.*;
import com.logistics.platform.app.tracking.entity.ShipmentTrackingEvent;
import com.logistics.platform.app.tracking.repository.ShipmentTrackingEventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TrackingService {
    private final ShipmentRepository shipmentRepository;
    private final ShipmentTrackingEventRepository eventRepository;
    private final ShipmentService shipmentService;

    @Transactional(readOnly = true)
    public List<TrackingEventResponse> history(String trackingNumber, Long requesterId, boolean isStaff) {
        Shipment s = shipmentRepository.findByTrackingNumber(trackingNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Shipment not found"));

        if (!isStaff && !s.getCustomerId().equals(requesterId)) {
            throw new ResourceNotFoundException("Shipment not found");
        }

        return eventRepository.findByShipmentIdOrderByTimestampAsc(s.getId()).stream()
                .map(e -> new TrackingEventResponse(e.getId(), e.getStatus(), e.getLocation(), e.getDescription(), e.getTimestamp()))
                .toList();
    }

    @Transactional
    public TrackingEventResponse add(String trackingNumber, TrackingEventRequest r) {
        Shipment s = shipmentRepository.findByTrackingNumber(trackingNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Shipment not found"));


        if (!ShipmentStatusTransitionValidator.isAllowed(s.getStatus(), r.status())) {
            throw new BusinessException("Cannot transition shipment from " + s.getStatus() + " to " + r.status());
        }

        s.setStatus(r.status());
        s.setCurrentLocation(r.location());
        shipmentRepository.save(s);


        if (r.status() == ShipmentStatus.OUT_FOR_DELIVERY) {
            shipmentService.assignDeliveryPartner(s);
        } else if (r.status() == ShipmentStatus.DELIVERED || r.status() == ShipmentStatus.CANCELLED) {
            shipmentService.releaseReservedCapacity(s);
        }

        ShipmentTrackingEvent e = eventRepository.save(ShipmentTrackingEvent.builder().shipment(s)
                .status(r.status()).location(r.location()).description(r.description()).build());
        return new TrackingEventResponse(e.getId(), e.getStatus(), e.getLocation(), e.getDescription(), e.getTimestamp());
    }
}
