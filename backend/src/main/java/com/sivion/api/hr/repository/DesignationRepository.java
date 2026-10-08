package com.sivion.api.hr.repository;
import com.sivion.api.hr.domain.*;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.*;
public interface DesignationRepository extends JpaRepository<Designation,Long> {
    List<Designation> findByTenantIdOrderByName(Long tenantId);
    boolean existsByTenantIdAndCodeAndIdNot(Long tenantId,String code,Long id);
    boolean existsByTenantIdAndCode(Long tenantId,String code);
    Designation findByTenantIdAndCode(Long tenantId,String code);
}
