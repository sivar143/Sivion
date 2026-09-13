package com.sivion.api.procurement.repo;
import com.sivion.api.procurement.domain.Supplier; import org.springframework.data.jpa.repository.JpaRepository; import java.util.*;
public interface SupplierRepository extends JpaRepository<Supplier,Long>{List<Supplier> findByTenantIdOrderByName(Long tenantId);boolean existsByTenantIdAndCode(Long tenantId,String code);Optional<Supplier> findByIdAndTenantId(Long id,Long tenantId);}
