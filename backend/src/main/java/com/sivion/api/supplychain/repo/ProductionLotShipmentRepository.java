package com.sivion.api.supplychain.repo;

import com.sivion.api.supplychain.domain.ProductionLotShipment;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductionLotShipmentRepository extends JpaRepository<ProductionLotShipment, Long> {
    List<ProductionLotShipment> findAllByTenantIdAndShipmentId(Long tenantId, Long shipmentId);
    List<ProductionLotShipment> findAllByTenantIdAndProductionLotId(Long tenantId, Long productionLotId);
    Optional<ProductionLotShipment> findByTenantIdAndProductionLotIdAndShipmentId(Long tenantId, Long productionLotId, Long shipmentId);
}