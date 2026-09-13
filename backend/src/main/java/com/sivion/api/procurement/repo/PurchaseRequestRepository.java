package com.sivion.api.procurement.repo;
import com.sivion.api.procurement.domain.PurchaseRequest; import org.springframework.data.jpa.repository.JpaRepository; import java.util.*;
public interface PurchaseRequestRepository extends JpaRepository<PurchaseRequest,Long>{List<PurchaseRequest> findByTenantIdOrderByRequestedAtDesc(Long tenantId);}
