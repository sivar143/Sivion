package com.sivion.api.inventory.repo;
import com.sivion.api.inventory.domain.InventoryTransaction;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import java.math.BigDecimal;
import java.util.*;
public interface InventoryTransactionRepository extends JpaRepository<InventoryTransaction,Long>{
 @Query("select coalesce(sum(t.quantity),0) from InventoryTransaction t where t.tenantId=:tenant and t.warehouseId=:warehouse and t.productId=:product") BigDecimal balance(@Param("tenant") Long tenant,@Param("warehouse") Long warehouse,@Param("product") Long product);
 boolean existsByTenantIdAndTransactionTypeAndReferenceTypeAndReferenceId(Long tenantId,String transactionType,String referenceType,Long referenceId);
 List<InventoryTransaction> findTop100ByTenantIdOrderByCreatedAtDesc(Long tenantId);
}
