package com.sivion.api.hr.domain;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.*;
@Entity
@Table(name="hr_payslips", uniqueConstraints=@UniqueConstraint(name="uk_hr_payslip_employee_period", columnNames= {
    "tenant_id","employee_id","period_start","period_end"
}))
public class Payslip  {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
    @Column(name="tenant_id",nullable=false) private Long tenantId;
    @Column(name="employee_id",nullable=false) private Long employeeId;
    @Column(name="period_start",nullable=false) private LocalDate periodStart;
    @Column(name="period_end",nullable=false) private LocalDate periodEnd;
    @Column(name="gross_pay",nullable=false,precision=18,scale=2) private BigDecimal grossPay=BigDecimal.ZERO;
    @Column(nullable=false,precision=18,scale=2) private BigDecimal deductions=BigDecimal.ZERO;
    @Column(name="net_pay",nullable=false,precision=18,scale=2) private BigDecimal netPay=BigDecimal.ZERO;
    @Column(nullable=false,length=20) private String status="DRAFT";
    @Column(length=1000) private String notes;
    @Column(name="created_at",nullable=false) private Instant createdAt=Instant.now();
    @Column(name="updated_at",nullable=false) private Instant updatedAt=Instant.now();
    public Long getId() {
        return id;
    }
    public Long getTenantId() {
        return tenantId;
    }
    public void setTenantId(Long v) {
        tenantId=v;
    }
    public Long getEmployeeId() {
        return employeeId;
    }
    public void setEmployeeId(Long v) {
        employeeId=v;
    }
    public LocalDate getPeriodStart() {
        return periodStart;
    }
    public void setPeriodStart(LocalDate v) {
        periodStart=v;
    }
    public LocalDate getPeriodEnd() {
        return periodEnd;
    }
    public void setPeriodEnd(LocalDate v) {
        periodEnd=v;
    }
    public BigDecimal getGrossPay() {
        return grossPay;
    }
    public void setGrossPay(BigDecimal v) {
        grossPay=v;
    }
    public BigDecimal getDeductions() {
        return deductions;
    }
    public void setDeductions(BigDecimal v) {
        deductions=v;
    }
    public BigDecimal getNetPay() {
        return netPay;
    }
    public void setNetPay(BigDecimal v) {
        netPay=v;
    }
    public String getStatus() {
        return status;
    }
    public void setStatus(String v) {
        status=v;
    }
    public String getNotes() {
        return notes;
    }
    public void setNotes(String v) {
        notes=v;
    }
    public Instant getCreatedAt() {
        return createdAt;
    }
    public Instant getUpdatedAt() {
        return updatedAt;
    }
    public void touch() {
        updatedAt=Instant.now();
    }
}
