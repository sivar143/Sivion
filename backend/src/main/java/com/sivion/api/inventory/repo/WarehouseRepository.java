package com.sivion.api.inventory.repo;
import com.sivion.api.inventory.domain.Warehouse;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.*;
public interface WarehouseRepository extends JpaRepository<Warehouse,Long>{ List<Warehouse> findByTenantIdOrderByName(Long tenantId); Optional<Warehouse> findByIdAndTenantId(Long id,Long tenantId); }
