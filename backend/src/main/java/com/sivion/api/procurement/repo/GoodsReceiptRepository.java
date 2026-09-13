package com.sivion.api.procurement.repo;
import com.sivion.api.procurement.domain.GoodsReceipt; import org.springframework.data.jpa.repository.JpaRepository; import java.util.*;
public interface GoodsReceiptRepository extends JpaRepository<GoodsReceipt,Long>{List<GoodsReceipt> findByTenantIdOrderByReceivedAtDesc(Long tenantId);}
