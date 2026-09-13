package com.sivion.api.hr.domain;

import jakarta.persistence.*;
import java.time.*;

@Entity
@Table(name="hr_leave_requests")
public class LeaveRequest {
  @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
  @Column(name="tenant_id", nullable=false) private Long tenantId;
  @Column(name="employee_id", nullable=false) private Long employeeId;
  @Column(name="leave_type", nullable=false, length=40) private String leaveType;
  @Column(name="start_date", nullable=false) private LocalDate startDate;
  @Column(name="end_date", nullable=false) private LocalDate endDate;
  @Column(nullable=false, length=20) private String status="PENDING";
  @Column(length=500) private String reason;
  @Column(name="approved_by") private Long approvedBy;
  @Column(name="created_at", nullable=false) private Instant createdAt=Instant.now();
  public Long getId(){return id;} public Long getTenantId(){return tenantId;} public void setTenantId(Long v){tenantId=v;} public Long getEmployeeId(){return employeeId;} public void setEmployeeId(Long v){employeeId=v;} public String getLeaveType(){return leaveType;} public void setLeaveType(String v){leaveType=v;} public LocalDate getStartDate(){return startDate;} public void setStartDate(LocalDate v){startDate=v;} public LocalDate getEndDate(){return endDate;} public void setEndDate(LocalDate v){endDate=v;} public String getStatus(){return status;} public void setStatus(String v){status=v;} public String getReason(){return reason;} public void setReason(String v){reason=v;} public Long getApprovedBy(){return approvedBy;} public void setApprovedBy(Long v){approvedBy=v;} public Instant getCreatedAt(){return createdAt;}
}
