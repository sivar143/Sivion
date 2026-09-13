package com.sivion.api.inventory.repo;

import com.sivion.api.inventory.domain.Material;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import java.util.*;

public interface MaterialRepository extends JpaRepository<Material,Long>{
 List<Material> findByTenantIdOrderByName(Long tenantId);
 Optional<Material> findByIdAndTenantId(Long id,Long tenantId);
 boolean existsByTenantIdAndSku(Long tenantId,String sku);
 @Lock(LockModeType.PESSIMISTIC_WRITE)
 @Query("select m from Material m where m.id = :id and m.tenantId = :tenantId")
 Optional<Material> findForUpdate(Long id,Long tenantId);
}
