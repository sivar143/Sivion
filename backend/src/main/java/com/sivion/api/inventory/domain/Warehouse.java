package com.sivion.api.inventory.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;

@Entity
@Table(name="warehouses", uniqueConstraints=@UniqueConstraint(name="uk_warehouse_tenant_code", columnNames={"tenant_id","code"}))
public class Warehouse {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
    @Column(name="tenant_id", nullable=false) private Long tenantId;
    @NotBlank @Column(nullable=false, length=50) private String code;
    @NotBlank @Column(nullable=false, length=200) private String name;
    @Column(nullable=false, length=20) private String status="ACTIVE";
    public Long getId(){return id;} public Long getTenantId(){return tenantId;} public void setTenantId(Long v){tenantId=v;}
    public String getCode(){return code;} public void setCode(String v){code=v;} public String getName(){return name;} public void setName(String v){name=v;} public String getStatus(){return status;} public void setStatus(String v){status=v;}
}
