package com.sivion.api.finance.repo;
import com.sivion.api.finance.domain.Invoice;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.*; import org.springframework.data.repository.query.Param; import java.util.*;
public interface InvoiceRepository extends JpaRepository<Invoice,Long>{List<Invoice> findByTenantIdOrderByInvoiceDateDesc(Long t);Optional<Invoice> findByIdAndTenantId(Long id,Long t);Optional<Invoice> findByTenantIdAndSalesOrderId(Long tenant,Long salesOrderId);@Lock(LockModeType.PESSIMISTIC_WRITE) @Query("select i from Invoice i where i.id=:id and i.tenantId=:tenant") Optional<Invoice> findForUpdate(@Param("id")Long id,@Param("tenant")Long tenant);}
