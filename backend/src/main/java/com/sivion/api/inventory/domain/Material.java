package com.sivion.api.inventory.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name="products", uniqueConstraints=@UniqueConstraint(name="uk_product_tenant_sku", columnNames={"tenant_id","sku"}))
public class Material {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
    @Column(name="tenant_id", nullable=false) private Long tenantId;
    @NotBlank @Column(nullable=false, length=80) private String sku;
    @NotBlank @Column(nullable=false, length=200) private String name;
    @Column(columnDefinition="TEXT") private String description;
    @NotBlank @Column(nullable=false, length=30) private String unit = "EA";
    @NotNull @Column(name="reorder_level", nullable=false, precision=18, scale=4) private BigDecimal reorderLevel = BigDecimal.ZERO;
    @Column(nullable=false, length=20) private String status = "ACTIVE";
    @Column(name="created_at", nullable=false) private Instant createdAt;
    @Column(name="updated_at", nullable=false) private Instant updatedAt;
    @PrePersist void create(){createdAt=Instant.now();updatedAt=createdAt;}
    @PreUpdate void update(){updatedAt=Instant.now();}
    public Long getId(){return id;} public Long getTenantId(){return tenantId;} public void setTenantId(Long v){tenantId=v;}
    public String getSku(){return sku;} public void setSku(String v){sku=v;} public String getName(){return name;} public void setName(String v){name=v;}
    public String getDescription(){return description;} public void setDescription(String v){description=v;} public String getUnit(){return unit;} public void setUnit(String v){unit=v;}
    public BigDecimal getReorderLevel(){return reorderLevel;} public void setReorderLevel(BigDecimal v){reorderLevel=v;} public String getStatus(){return status;} public void setStatus(String v){status=v;}
}
