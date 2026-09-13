package com.sivion.api.inventory.repo;
import com.sivion.api.inventory.domain.Material;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.*;
public interface MaterialRepository extends JpaRepository<Material,Long>{ List<Material> findByTenantIdOrderByName(Long tenantId); Optional<Material> findByIdAndTenantId(Long id,Long tenantId); boolean existsByTenantIdAndSku(Long tenantId,String sku); }
