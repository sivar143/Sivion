package com.sivion.api.inventory.repo;
import com.sivion.api.inventory.domain.Dispatch;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.*;
public interface DispatchRepository extends JpaRepository<Dispatch,Long>{ List<Dispatch> findByTenantIdOrderByDispatchDateDesc(Long tenantId); Optional<Dispatch> findByIdAndTenantId(Long id,Long tenantId); }
