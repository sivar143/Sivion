package com.sivion.api.hr.repository;
import com.sivion.api.hr.domain.*; import org.springframework.data.jpa.repository.JpaRepository; import java.util.*;
public interface DepartmentRepository extends JpaRepository<Department,Long>{ List<Department> findByTenantIdOrderByName(Long tenantId); }
