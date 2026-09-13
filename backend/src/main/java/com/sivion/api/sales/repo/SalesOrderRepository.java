package com.sivion.api.sales.repo;

import com.sivion.api.sales.domain.SalesOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface SalesOrderRepository extends JpaRepository<SalesOrder,Long> {
    List<SalesOrder> findByTenantIdOrderByOrderDateDesc(Long tenantId);
    Optional<SalesOrder> findByIdAndTenantId(Long id, Long tenantId);
    boolean existsByTenantIdAndOrderNumber(Long tenantId, String orderNumber);
}
