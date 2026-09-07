package com.logistics.platform.app.shipment.service;

import com.logistics.platform.app.audit.service.AuditService;
import com.logistics.platform.app.common.PageResponse;
import com.logistics.platform.app.delivery.entity.DeliveryPartner;
import com.logistics.platform.app.delivery.service.DeliveryAssignmentService;
import com.logistics.platform.app.exception.*;
import com.logistics.platform.app.pricing.service.PricingEngine;
import com.logistics.platform.app.shipment.dto.*;
import com.logistics.platform.app.shipment.entity.*;
import com.logistics.platform.app.shipment.repository.ShipmentRepository;
import com.logistics.platform.app.tracking.entity.ShipmentTrackingEvent;
import com.logistics.platform.app.tracking.repository.ShipmentTrackingEventRepository;
import com.logistics.platform.app.warehouse.entity.Warehouse;
import com.logistics.platform.app.warehouse.repository.WarehouseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ShipmentService {
    private final ShipmentRepository shipmentRepository;
    private final WarehouseRepository warehouseRepository;
    private final ShipmentTrackingEventRepository trackingRepository;
    private final PricingEngine pricingEngine;
    private final AuditService auditService;
    private final DeliveryAssignmentService deliveryAssignmentService;

    @Transactional
    public ShipmentResponse create(ShipmentCreateRequest r, Long customerId) {
        Warehouse warehouse = warehouseRepository.findById(r.warehouseId())
                .orElseThrow(() -> new ResourceNotFoundException("Warehouse not found"));

        if (warehouse.getCurrentLoad().add(r.weight()).compareTo(warehouse.getCapacity()) > 0) {
            throw new BusinessException("Warehouse capacity exceeded");
        }


        BigDecimal price = pricingEngine.calculatePrice(r.weight(), r.deliveryType(), BigDecimal.ZERO);
        String tracking = generateTrackingNumber();

        Shipment s = Shipment.builder().trackingNumber(tracking).customerId(customerId)
                .pickupAddress(r.pickupAddress()).deliveryAddress(r.deliveryAddress())
                .weight(r.weight()).length(r.length()).width(r.width()).height(r.height())
                .packageType(r.packageType()).deliveryType(r.deliveryType()).price(price)
                .warehouse(warehouse).currentLocation(warehouse.getLocation()).build();

        warehouse.setCurrentLoad(warehouse.getCurrentLoad().add(r.weight()));
        shipmentRepository.save(s);
        warehouseRepository.save(warehouse);

        trackingRepository.save(ShipmentTrackingEvent.builder().shipment(s)
                .status(ShipmentStatus.CREATED).location(warehouse.getLocation())
                .description("Shipment created").build());

        auditService.record(customerId, "CREATE", "SHIPMENT", s.getId(), null, tracking);
        return toResponse(s);
    }

    @Transactional(readOnly = true)
    public ShipmentResponse getByTracking(String tracking, Long requesterId, boolean isStaff) {
        Shipment shipment = shipmentRepository.findByTrackingNumber(tracking)
                .orElseThrow(() -> new ResourceNotFoundException("Shipment not found"));

        if (!isStaff && !shipment.getCustomerId().equals(requesterId)) {
            throw new ResourceNotFoundException("Shipment not found");
        }
        return toResponse(shipment);
    }

    @Transactional(readOnly = true)
    public PageResponse<ShipmentResponse> getMine(Long customerId, int page, int size) {
        Page<ShipmentResponse> p = shipmentRepository.findByCustomerId(customerId,
                PageRequest.of(page, size, Sort.by("createdAt").descending())).map(this::toResponse);
        return PageResponse.from(p);
    }

    @Transactional
    public ShipmentResponse cancel(Long id, Long userId, boolean isStaff) {
        Shipment s = shipmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Shipment not found"));

        if (!isStaff && !s.getCustomerId().equals(userId)) {
            throw new ResourceNotFoundException("Shipment not found");
        }

        if (s.getStatus() == ShipmentStatus.DELIVERED || s.getStatus() == ShipmentStatus.CANCELLED) {
            throw new BusinessException("Shipment cannot be cancelled in current state");
        }

        ShipmentStatus old = s.getStatus();
        s.setStatus(ShipmentStatus.CANCELLED);
        shipmentRepository.save(s);

        releaseReservedCapacity(s);

        trackingRepository.save(ShipmentTrackingEvent.builder().shipment(s).status(ShipmentStatus.CANCELLED)
                .location(s.getCurrentLocation()).description("Shipment cancelled").build());
        auditService.record(userId, "CANCEL", "SHIPMENT", id, old.name(), ShipmentStatus.CANCELLED.name());
        return toResponse(s);
    }


    @Transactional
    public void releaseReservedCapacity(Shipment s) {
        if (s.getWarehouse() != null) {
            Warehouse warehouse = s.getWarehouse();
            BigDecimal newLoad = warehouse.getCurrentLoad().subtract(s.getWeight());
            warehouse.setCurrentLoad(newLoad.signum() < 0 ? BigDecimal.ZERO : newLoad);
            warehouseRepository.save(warehouse);
        }
        if (s.getDeliveryPartner() != null) {
            deliveryAssignmentService.release(s.getDeliveryPartner().getId(), s.getWeight());
        }
    }

    @Transactional
    public void assignDeliveryPartner(Shipment s) {
        if (s.getDeliveryPartner() != null) return;
        DeliveryPartner partner = deliveryAssignmentService.assign(s.getWeight());
        s.setDeliveryPartner(partner);
        shipmentRepository.save(s);
    }

    private String generateTrackingNumber() {
        String value;
        do {
            value = "TRK-" + UUID.randomUUID().toString().replace("-", "").substring(0, 12).toUpperCase();
        } while (shipmentRepository.existsByTrackingNumber(value));
        return value;
    }

    private ShipmentResponse toResponse(Shipment s) {
        return new ShipmentResponse(s.getId(), s.getTrackingNumber(), s.getCustomerId(),
                s.getPickupAddress(), s.getDeliveryAddress(), s.getWeight(), s.getLength(), s.getWidth(), s.getHeight(),
                s.getPackageType(), s.getDeliveryType(), s.getPrice(), s.getStatus(),
                s.getDeliveryPartner() == null ? null : s.getDeliveryPartner().getId(),
                s.getWarehouse() == null ? null : s.getWarehouse().getId(), s.getEstimateDeliveryDate(),
                s.getCurrentLocation(), s.getCreatedAt(), s.getUpdatedAt());
    }
}
