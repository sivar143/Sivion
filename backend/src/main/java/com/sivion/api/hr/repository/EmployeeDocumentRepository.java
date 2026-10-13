package com.sivion.api.hr.repository;

import com.sivion.api.hr.domain.EmployeeDocument;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EmployeeDocumentRepository extends JpaRepository<EmployeeDocument, Long> {
    List<EmployeeDocument> findByTenantIdAndEmployeeIdInOrderByCreatedAtDesc(Long tenantId, List<Long> employeeIds);
    Optional<EmployeeDocument> findByIdAndTenantId(Long id, Long tenantId);
}
