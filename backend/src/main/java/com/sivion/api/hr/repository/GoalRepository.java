package com.sivion.api.hr.repository;
import com.sivion.api.hr.domain.*; import org.springframework.data.jpa.repository.JpaRepository; import java.util.*;
public interface GoalRepository extends JpaRepository<Goal,Long>{ List<Goal> findByTenantIdOrderByDueDateAsc(Long tenantId); }
