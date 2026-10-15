package com.sivion.api.supplychain.repo;
import com.sivion.api.supplychain.domain.ProductionLot;
import java.util.*;
import org.springframework.data.jpa.repository.JpaRepository;
public interface ProductionLotRepository extends JpaRepository<ProductionLot, Long> {
 List<ProductionLot> findAllByTenantIdOrderByProducedAtDesc(Long tenantId);
 Optional<ProductionLot> findByIdAndTenantId(Long id, Long tenantId);
 Optional<ProductionLot> findByLotCodeAndTenantId(String lotCode, Long tenantId);
 boolean existsByLotCodeAndTenantId(String lotCode, Long tenantId);
}
