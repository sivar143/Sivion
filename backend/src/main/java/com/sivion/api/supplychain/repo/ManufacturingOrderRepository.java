package com.sivion.api.supplychain.repo;

import com.sivion.api.supplychain.domain.ManufacturingOrder;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ManufacturingOrderRepository extends JpaRepository<ManufacturingOrder, Long> {
    Optional<ManufacturingOrder> findByIdAndTenantId(Long id, Long tenantId);
    List<ManufacturingOrder> findAllByTenantIdOrderByCreatedAtDesc(Long tenantId);
    boolean existsByTenantIdAndOrderNumber(Long tenantId, String orderNumber);
}