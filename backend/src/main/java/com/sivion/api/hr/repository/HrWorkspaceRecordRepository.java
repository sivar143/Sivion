package com.sivion.api.hr.repository;

import com.sivion.api.hr.domain.HrWorkspaceRecord;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface HrWorkspaceRecordRepository extends JpaRepository<HrWorkspaceRecord, Long> {
    List<HrWorkspaceRecord> findByTenantIdAndTypeOrderByCreatedAtDesc(Long tenantId, String type);
    Optional<HrWorkspaceRecord> findByIdAndTenantId(Long id, Long tenantId);
}
