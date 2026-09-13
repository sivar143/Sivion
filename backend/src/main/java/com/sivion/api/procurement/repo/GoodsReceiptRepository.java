package com.sivion.api.procurement.repo;

import com.sivion.api.procurement.domain.GoodsReceipt;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.math.BigDecimal;
import java.util.*;

public interface GoodsReceiptRepository extends JpaRepository<GoodsReceipt,Long> {
    List<GoodsReceipt> findByTenantIdOrderByReceivedAtDesc(Long tenantId);

    @Query("select coalesce(sum(g.quantity), 0) from GoodsReceipt g where g.tenantId=:tenantId and g.purchaseOrderId=:purchaseOrderId and g.productId=:productId and g.warehouseId=:warehouseId")
    BigDecimal receivedQuantity(@Param("tenantId") Long tenantId,
                                @Param("purchaseOrderId") Long purchaseOrderId,
                                @Param("productId") Long productId,
                                @Param("warehouseId") Long warehouseId);
}
