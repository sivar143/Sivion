package com.sivion.api.procurement.repo;

import com.sivion.api.procurement.domain.*; import org.springframework.data.jpa.repository.JpaRepository; import java.util.*;

public interface SupplierRepository extends JpaRepository<Supplier,Long>{List<Supplier> findByTenantIdOrderByName(Long tenantId);boolean existsByTenantIdAndCode(Long tenantId,String code);Optional<Supplier> findByIdAndTenantId(Long id,Long tenantId);}
public interface PurchaseRequestRepository extends JpaRepository<PurchaseRequest,Long>{List<PurchaseRequest> findByTenantIdOrderByRequestedAtDesc(Long tenantId);}
public interface PurchaseOrderRepository extends JpaRepository<PurchaseOrder,Long>{List<PurchaseOrder> findByTenantIdOrderByOrderDateDesc(Long tenantId);}
public interface GoodsReceiptRepository extends JpaRepository<GoodsReceipt,Long>{List<GoodsReceipt> findByTenantIdOrderByReceivedAtDesc(Long tenantId);}
