package com.sivion.api.procurement.repo;

import com.sivion.api.procurement.domain.PurchaseOrder;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import java.util.*;

public interface PurchaseOrderRepository extends JpaRepository<PurchaseOrder,Long>{
    List<PurchaseOrder> findByTenantIdOrderByOrderDateDesc(Long tenantId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select o from PurchaseOrder o where o.id=:id and o.tenantId=:tenantId")
    Optional<PurchaseOrder> findForUpdate(@Param("id") Long id,@Param("tenantId") Long tenantId);
}
