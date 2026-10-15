package com.sivion.api.supplychain.repo;

import com.sivion.api.supplychain.domain.Shipment;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ShipmentRepository extends JpaRepository<Shipment, Long> {
    List<Shipment> findAllByTenantIdOrderByCreatedAtDesc(Long tenantId);
    Optional<Shipment> findByIdAndTenantId(Long id, Long tenantId);
    Optional<Shipment> findByPublicTrackingToken(String publicTrackingToken);
    boolean existsByShipmentNumberAndTenantId(String shipmentNumber, Long tenantId);
}
