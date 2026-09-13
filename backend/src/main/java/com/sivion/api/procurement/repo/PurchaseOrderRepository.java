package com.sivion.api.procurement.repo;
import com.sivion.api.procurement.domain.PurchaseOrder; import org.springframework.data.jpa.repository.JpaRepository; import java.util.*;
public interface PurchaseOrderRepository extends JpaRepository<PurchaseOrder,Long>{List<PurchaseOrder> findByTenantIdOrderByOrderDateDesc(Long tenantId);}
