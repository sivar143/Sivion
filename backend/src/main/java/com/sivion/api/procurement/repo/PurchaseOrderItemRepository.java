package com.sivion.api.procurement.repo;

import com.sivion.api.procurement.domain.PurchaseOrderItem;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.*;

public interface PurchaseOrderItemRepository extends JpaRepository<PurchaseOrderItem,Long>{
 List<PurchaseOrderItem> findByPurchaseOrderId(Long purchaseOrderId);
}
