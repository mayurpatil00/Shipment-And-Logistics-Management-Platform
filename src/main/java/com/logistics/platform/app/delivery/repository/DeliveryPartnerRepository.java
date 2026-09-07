package com.logistics.platform.app.delivery.repository;

import com.logistics.platform.app.delivery.entity.DeliveryPartner;
import com.logistics.platform.app.delivery.entity.PartnerStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DeliveryPartnerRepository extends JpaRepository<DeliveryPartner, Long> {
    List<DeliveryPartner> findByStatus(PartnerStatus status);


    @Query("select p from DeliveryPartner p where p.active = true and p.status = 'AVAILABLE' " +
            "and (p.maxCapacity - p.currentLoad) >= :weight order by p.currentLoad asc")
    List<DeliveryPartner> findEligiblePartners(@Param("weight") java.math.BigDecimal weight);
}
