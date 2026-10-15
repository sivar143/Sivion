package com.sivion.api.supplychain.repo;
import com.sivion.api.supplychain.domain.MaterialLot;
import java.util.*;
import org.springframework.data.jpa.repository.JpaRepository;
public interface MaterialLotRepository extends JpaRepository<MaterialLot, Long> {
 List<MaterialLot> findAllByTenantIdOrderByCreatedAtDesc(Long tenantId);
 Optional<MaterialLot> findByIdAndTenantId(Long id, Long tenantId);
 Optional<MaterialLot> findByLotCodeAndTenantId(String lotCode, Long tenantId);
 boolean existsByLotCodeAndTenantId(String lotCode, Long tenantId);
}
