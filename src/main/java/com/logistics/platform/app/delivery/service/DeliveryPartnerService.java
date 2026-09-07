package com.logistics.platform.app.delivery.service;

import com.logistics.platform.app.delivery.dto.*;
import com.logistics.platform.app.delivery.entity.DeliveryPartner;
import com.logistics.platform.app.delivery.repository.DeliveryPartnerRepository;
import com.logistics.platform.app.exception.ResourceNotFoundException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DeliveryPartnerService {

    private final DeliveryPartnerRepository repository;

    @Transactional
    public DeliveryPartnerResponse create(@Valid DeliveryPartnerCreateRequest request) {
        DeliveryPartner partner = DeliveryPartner.builder()
                .name(request.name().trim())
                .phone(request.phone().trim())
                .maxCapacity(request.maxCapacity())
                .currentLoad(BigDecimal.ZERO)
                .build();
        repository.save(partner);
        return toResponse(partner);
    }

    @Transactional(readOnly = true)
    public List<DeliveryPartnerResponse> list() {
        return repository.findAll().stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public DeliveryPartnerResponse getById(Long id) {
        return toResponse(repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Delivery partner not found with id: " + id)));
    }

    private DeliveryPartnerResponse toResponse(DeliveryPartner p) {
        return new DeliveryPartnerResponse(p.getId(), p.getName(), p.getPhone(), p.isActive(),
                p.getStatus(), p.getMaxCapacity(), p.getCurrentLoad());
    }
}
