package com.sivion.api.inventory.repo;

import com.sivion.api.inventory.domain.InventoryReservation;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import java.math.BigDecimal;
import java.util.*;

public interface InventoryReservationRepository extends JpaRepository<InventoryReservation,Long>{
    List<InventoryReservation> findByTenantIdAndSalesOrderIdAndStatus(Long tenantId,Long salesOrderId,String status);
    Optional<InventoryReservation> findByTenantIdAndSalesOrderIdAndProductIdAndWarehouseId(Long tenantId,Long salesOrderId,Long productId,Long warehouseId);
    @Query("select coalesce(sum(r.quantity),0) from InventoryReservation r where r.tenantId=:tenant and r.warehouseId=:warehouse and r.productId=:product and r.status='RESERVED'")
    BigDecimal reservedQuantity(@Param("tenant")Long tenant,@Param("warehouse")Long warehouse,@Param("product")Long product);
}
