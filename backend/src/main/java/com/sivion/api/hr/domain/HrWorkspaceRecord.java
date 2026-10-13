package com.sivion.api.hr.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import java.time.Instant;
import java.time.LocalDate;

@Entity
@Table(name = "hr_workspace_records", indexes = @Index(
    name = "idx_hr_workspace_tenant_type_employee",
    columnList = "tenant_id, record_type, employee_id"
))
public class HrWorkspaceRecord {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tenant_id", nullable = false)
    private Long tenantId;

    @Column(name = "record_type", nullable = false, length = 20)
    private String type;

    @Column(name = "employee_id")
    private Long employeeId;

    @Column(nullable = false, length = 180)
    private String title;

    @Column(length = 3000)
    private String description;

    @Column
    private Integer score;

    @Column(name = "resource_url", length = 1000)
    private String resourceUrl;

    @Column(name = "due_date")
    private LocalDate dueDate;

    @Column(nullable = false, length = 20)
    private String status = "ACTIVE";

    @Column(name = "created_by", nullable = false, length = 120)
    private String createdBy;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt = Instant.now();

    public HrWorkspaceRecord() {
    }

    public Long getId() { return id; }
    public Long getTenantId() { return tenantId; }
    public void setTenantId(Long tenantId) { this.tenantId = tenantId; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public Long getEmployeeId() { return employeeId; }
    public void setEmployeeId(Long employeeId) { this.employeeId = employeeId; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public Integer getScore() { return score; }
    public void setScore(Integer score) { this.score = score; }
    public String getResourceUrl() { return resourceUrl; }
    public void setResourceUrl(String resourceUrl) { this.resourceUrl = resourceUrl; }
    public LocalDate getDueDate() { return dueDate; }
    public void setDueDate(LocalDate dueDate) { this.dueDate = dueDate; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getCreatedBy() { return createdBy; }
    public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }
    public Instant getCreatedAt() { return createdAt; }
}
