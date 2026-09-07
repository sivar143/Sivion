package com.sivion.api.crm.domain;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name="crm_contacts")
public class Contact {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
 @Column(name="tenant_id",nullable=false) private Long tenantId;
 @Column(name="customer_id",nullable=false) private Long customerId;
 @Column(name="first_name",nullable=false,length=100) private String firstName;
 @Column(name="last_name",length=100) private String lastName;
 @Column(length=120) private String jobTitle;
 @Column(length=255) private String email;
 @Column(length=40) private String phone;
 @Column(length=40) private String mobile;
 @Column(nullable=false) private Boolean primaryContact=false;
 @Column(length=20,nullable=false) private String status="ACTIVE";
 @Column(name="created_at",nullable=false) private Instant createdAt;
 @Column(name="updated_at",nullable=false) private Instant updatedAt;
 @PrePersist void prePersist(){createdAt=Instant.now();updatedAt=createdAt;} @PreUpdate void preUpdate(){updatedAt=Instant.now();}
 public Long getId(){return id;} public Long getTenantId(){return tenantId;} public void setTenantId(Long v){tenantId=v;} public Long getCustomerId(){return customerId;} public void setCustomerId(Long v){customerId=v;}
 public String getFirstName(){return firstName;} public void setFirstName(String v){firstName=v;} public String getLastName(){return lastName;} public void setLastName(String v){lastName=v;} public String getJobTitle(){return jobTitle;} public void setJobTitle(String v){jobTitle=v;}
 public String getEmail(){return email;} public void setEmail(String v){email=v;} public String getPhone(){return phone;} public void setPhone(String v){phone=v;} public String getMobile(){return mobile;} public void setMobile(String v){mobile=v;} public Boolean getPrimaryContact(){return primaryContact;} public void setPrimaryContact(Boolean v){primaryContact=v;} public String getStatus(){return status;} public void setStatus(String v){status=v;}
}
