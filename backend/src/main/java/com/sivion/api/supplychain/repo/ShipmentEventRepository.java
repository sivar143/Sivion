package com.sivion.api.supplychain.repo;

import com.sivion.api.supplychain.domain.Shipment;
import com.sivion.api.supplychain.domain.ShipmentEvent;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ShipmentEventRepository extends JpaRepository<ShipmentEvent, Long> {
    List<ShipmentEvent> findAllByShipmentOrderByEventTimeAscIdAsc(Shipment shipment);
    Optional<ShipmentEvent> findByTenantIdAndIdempotencyKey(Long tenantId, String idempotencyKey);
}
