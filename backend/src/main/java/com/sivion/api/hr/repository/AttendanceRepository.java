package com.sivion.api.hr.repository;
import com.sivion.api.hr.domain.*; import org.springframework.data.jpa.repository.JpaRepository; import java.time.LocalDate; import java.util.*;
public interface AttendanceRepository extends JpaRepository<Attendance,Long>{ List<Attendance> findByTenantIdAndAttendanceDateBetweenOrderByAttendanceDateDesc(Long tenantId,LocalDate from,LocalDate to); Optional<Attendance> findByTenantIdAndEmployeeIdAndAttendanceDate(Long tenantId,Long employeeId,LocalDate date); }
