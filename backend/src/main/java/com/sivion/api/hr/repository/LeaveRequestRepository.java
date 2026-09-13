package com.sivion.api.hr.repository;
import com.sivion.api.hr.domain.*; import org.springframework.data.jpa.repository.JpaRepository; import java.util.*;
public interface LeaveRequestRepository extends JpaRepository<LeaveRequest,Long>{ List<LeaveRequest> findByTenantIdOrderByCreatedAtDesc(Long tenantId); }
