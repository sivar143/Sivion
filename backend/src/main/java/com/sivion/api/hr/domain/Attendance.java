package com.sivion.api.hr.domain;

import jakarta.persistence.*;
import java.time.*;

@Entity
@Table(name="hr_attendance", uniqueConstraints=@UniqueConstraint(name="uk_hr_attendance_employee_day", columnNames={"tenant_id","employee_id","attendance_date"}))
public class Attendance {
  @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
  @Column(name="tenant_id", nullable=false) private Long tenantId;
  @Column(name="employee_id", nullable=false) private Long employeeId;
  @Column(name="attendance_date", nullable=false) private LocalDate attendanceDate;
  @Column(nullable=false, length=20) private String status="PRESENT";
  @Column(name="check_in") private LocalDateTime checkIn;
  @Column(name="check_out") private LocalDateTime checkOut;
  public Long getId(){return id;} public Long getTenantId(){return tenantId;} public void setTenantId(Long v){tenantId=v;} public Long getEmployeeId(){return employeeId;} public void setEmployeeId(Long v){employeeId=v;} public LocalDate getAttendanceDate(){return attendanceDate;} public void setAttendanceDate(LocalDate v){attendanceDate=v;} public String getStatus(){return status;} public void setStatus(String v){status=v;} public LocalDateTime getCheckIn(){return checkIn;} public void setCheckIn(LocalDateTime v){checkIn=v;} public LocalDateTime getCheckOut(){return checkOut;} public void setCheckOut(LocalDateTime v){checkOut=v;}
}
