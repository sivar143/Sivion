package com.sivion.api.supplychain.repo;

import com.sivion.api.supplychain.domain.QualityInspection;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface QualityInspectionRepository extends JpaRepository<QualityInspection, Long> {
    List<QualityInspection> findByTenantIdAndLotCodeOrderByInspectedAtAsc(Long tenantId, String lotCode);
}