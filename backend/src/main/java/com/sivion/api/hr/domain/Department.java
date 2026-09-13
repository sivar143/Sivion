package com.sivion.api.hr.domain;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name="hr_departments", uniqueConstraints=@UniqueConstraint(name="uk_hr_dept_tenant_code", columnNames={"tenant_id","code"}))
public class Department {
  @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
  @Column(name="tenant_id", nullable=false) private Long tenantId;
  @Column(nullable=false, length=40) private String code;
  @Column(nullable=false, length=160) private String name;
  @Column(name="parent_id") private Long parentId;
  @Column(nullable=false, length=20) private String status="ACTIVE";
  @Column(name="created_at", nullable=false) private Instant createdAt=Instant.now();
  public Long getId(){return id;} public Long getTenantId(){return tenantId;} public void setTenantId(Long v){tenantId=v;} public String getCode(){return code;} public void setCode(String v){code=v;} public String getName(){return name;} public void setName(String v){name=v;} public Long getParentId(){return parentId;} public void setParentId(Long v){parentId=v;} public String getStatus(){return status;} public void setStatus(String v){status=v;} public Instant getCreatedAt(){return createdAt;}
}
