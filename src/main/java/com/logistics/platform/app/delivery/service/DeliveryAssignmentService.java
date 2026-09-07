package com.logistics.platform.app.delivery.service;

import com.logistics.platform.app.delivery.entity.DeliveryPartner;
import com.logistics.platform.app.delivery.entity.PartnerStatus;
import com.logistics.platform.app.delivery.repository.DeliveryPartnerRepository;
import com.logistics.platform.app.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;


@Service
@RequiredArgsConstructor
public class DeliveryAssignmentService {

    private final DeliveryPartnerRepository repository;

    @Transactional
    public DeliveryPartner assign(BigDecimal weight) {
        List<DeliveryPartner> eligible = repository.findEligiblePartners(weight);
        DeliveryPartner partner = eligible.stream().findFirst()
                .orElseThrow(() -> new BusinessException("No delivery partner is currently available for this shipment"));

        partner.setCurrentLoad(partner.getCurrentLoad().add(weight));
        if (partner.getCurrentLoad().compareTo(partner.getMaxCapacity()) >= 0) {
            partner.setStatus(PartnerStatus.BUSY);
        }
        return repository.save(partner);
    }


    @Transactional
    public void release(Long partnerId, BigDecimal weight) {
        if (partnerId == null) return;
        Optional<DeliveryPartner> maybePartner = repository.findById(partnerId);
        if (maybePartner.isEmpty()) return;

        DeliveryPartner partner = maybePartner.get();
        BigDecimal newLoad = partner.getCurrentLoad().subtract(weight);
        partner.setCurrentLoad(newLoad.signum() < 0 ? BigDecimal.ZERO : newLoad);
        if (partner.isActive() && partner.getStatus() == PartnerStatus.BUSY) {
            partner.setStatus(PartnerStatus.AVAILABLE);
        }
        repository.save(partner);
    }
}
