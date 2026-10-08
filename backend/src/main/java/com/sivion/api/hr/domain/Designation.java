package com.sivion.api.hr.domain;
import jakarta.persistence.*;
@Entity @Table(name="hr_designations",uniqueConstraints=@UniqueConstraint(name="uk_hr_designation_tenant_code",columnNames={"tenant_id","code"}))
public class Designation{
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id; @Column(name="tenant_id",nullable=false) private Long tenantId; @Column(nullable=false,length=40) private String code; @Column(nullable=false,length=160) private String name; @Column(nullable=false,length=20) private String status="ACTIVE";
 public Long getId(){return id;} public Long getTenantId(){return tenantId;} public void setTenantId(Long v){tenantId=v;} public String getCode(){return code;} public void setCode(String v){code=v;} public String getName(){return name;} public void setName(String v){name=v;} public String getStatus(){return status;} public void setStatus(String v){status=v;}
}