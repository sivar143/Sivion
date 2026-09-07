package com.sivion.api.crm.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.Instant;

@Entity
@Table(name="customers", uniqueConstraints=@UniqueConstraint(name="uk_customer_tenant_code", columnNames={"tenant_id","code"}))
public class Customer {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
    @Column(name="tenant_id", nullable=false) private Long tenantId;
    @NotBlank @Size(max=50) @Column(nullable=false, length=50) private String code;
    @NotBlank @Size(max=200) @Column(nullable=false, length=200) private String name;
    @Email @Size(max=255) @Column(length=255) private String email;
    @Size(max=40) @Column(length=40) private String phone;
    @NotBlank @Size(max=20) @Column(length=20, nullable=false) private String status = "ACTIVE";
    @Column(name="created_at", nullable=false) private Instant createdAt;
    @Column(name="updated_at", nullable=false) private Instant updatedAt;
    @PrePersist void prePersist(){ createdAt=Instant.now(); updatedAt=createdAt; }
    @PreUpdate void preUpdate(){ updatedAt=Instant.now(); }
    public Long getId(){return id;} public Long getTenantId(){return tenantId;} public void setTenantId(Long v){tenantId=v;}
    public String getCode(){return code;} public void setCode(String v){code=v;} public String getName(){return name;} public void setName(String v){name=v;}
    public String getEmail(){return email;} public void setEmail(String v){email=v;} public String getPhone(){return phone;} public void setPhone(String v){phone=v;}
    public String getStatus(){return status;} public void setStatus(String v){status=v;} public Instant getCreatedAt(){return createdAt;} public Instant getUpdatedAt(){return updatedAt;}
}
