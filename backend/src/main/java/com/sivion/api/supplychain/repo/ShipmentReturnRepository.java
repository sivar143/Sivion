package com.sivion.api.supplychain.repo;

import com.sivion.api.supplychain.domain.ShipmentReturn;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ShipmentReturnRepository extends JpaRepository<ShipmentReturn, Long> {
    boolean existsByTenantIdAndReturnReference(Long tenantId, String returnReference);
    Optional<ShipmentReturn> findByTenantIdAndReturnReference(Long tenantId, String returnReference);
    java.util.List<ShipmentReturn> findAllByTenantIdAndShipmentId(Long tenantId, Long shipmentId);
}