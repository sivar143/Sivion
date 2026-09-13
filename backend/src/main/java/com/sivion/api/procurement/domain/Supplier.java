package com.sivion.api.procurement.domain;

import jakarta.persistence.*;
import java.time.Instant;

@Entity @Table(name="procurement_suppliers", uniqueConstraints=@UniqueConstraint(columnNames={"tenant_id","code"}))
public class Supplier {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
 @Column(name="tenant_id",nullable=false) private Long tenantId;
 @Column(nullable=false) private String code;
 @Column(nullable=false) private String name;
 private String email; private String phone; private String address;
 @Column(nullable=false) private String status="ACTIVE";
 @Column(nullable=false) private Instant createdAt=Instant.now();
 public Long getId(){return id;} public Long getTenantId(){return tenantId;} public void setTenantId(Long v){tenantId=v;} public String getCode(){return code;} public void setCode(String v){code=v;} public String getName(){return name;} public void setName(String v){name=v;} public String getEmail(){return email;} public void setEmail(String v){email=v;} public String getPhone(){return phone;} public void setPhone(String v){phone=v;} public String getAddress(){return address;} public void setAddress(String v){address=v;} public String getStatus(){return status;} public void setStatus(String v){status=v;} public Instant getCreatedAt(){return createdAt;}
}
