package com.sivion.api.hr.domain;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name="hr_goals")
public class Goal {
  @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
  @Column(name="tenant_id", nullable=false) private Long tenantId;
  @Column(name="employee_id", nullable=false) private Long employeeId;
  @Column(nullable=false, length=200) private String title;
  @Column(length=1000) private String description;
  @Column(name="target_value") private Double targetValue;
  @Column(name="current_value") private Double currentValue=0d;
  @Column(nullable=false, length=20) private String status="ACTIVE";
  @Column(name="due_date") private java.time.LocalDate dueDate;
  @Column(name="created_at", nullable=false) private Instant createdAt=Instant.now();
  public Long getId(){return id;} public Long getTenantId(){return tenantId;} public void setTenantId(Long v){tenantId=v;} public Long getEmployeeId(){return employeeId;} public void setEmployeeId(Long v){employeeId=v;} public String getTitle(){return title;} public void setTitle(String v){title=v;} public String getDescription(){return description;} public void setDescription(String v){description=v;} public Double getTargetValue(){return targetValue;} public void setTargetValue(Double v){targetValue=v;} public Double getCurrentValue(){return currentValue;} public void setCurrentValue(Double v){currentValue=v;} public String getStatus(){return status;} public void setStatus(String v){status=v;} public java.time.LocalDate getDueDate(){return dueDate;} public void setDueDate(java.time.LocalDate v){dueDate=v;} public Instant getCreatedAt(){return createdAt;}
}
