package com.sivion.api.hr.repository;
import com.sivion.api.hr.domain.Payslip;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDate;
import java.util.*;
public interface PayslipRepository extends JpaRepository<Payslip,Long>{
  List<Payslip> findByTenantIdOrderByPeriodEndDesc(Long tenantId);
  Optional<Payslip> findByTenantIdAndEmployeeIdAndPeriodStartAndPeriodEnd(Long tenantId,Long employeeId,LocalDate start,LocalDate end);
}
