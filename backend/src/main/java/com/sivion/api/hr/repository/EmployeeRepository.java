package com.sivion.api.hr.repository;
import com.sivion.api.hr.domain.*; import org.springframework.data.jpa.repository.JpaRepository; import java.util.*;
public interface EmployeeRepository extends JpaRepository<Employee,Long>{ List<Employee> findByTenantIdOrderByLastNameAscFirstNameAsc(Long tenantId); long countByTenantIdAndStatus(Long tenantId,String status); }
