package com.sivion.api.crm.domain;

import jakarta.persistence.*;
import java.math.BigDecimal; import java.time.Instant;

@Entity @Table(name="crm_leads")
public class Lead {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) Long id; @Column(name="tenant_id",nullable=false) Long tenantId; @Column(nullable=false,length=40) String leadNumber;
 @Column(nullable=false,length=200) String name; @Column(length=255) String email; @Column(length=40) String phone; @Column(name="company_name",length=200) String companyName;
 @Column(length=40,nullable=false) String source="OTHER"; @Column(length=40,nullable=false) String status="NEW"; @Column(length=40,nullable=false) String rating="WARM";
 @Column(precision=18,scale=4) BigDecimal estimatedValue; @Column(name="assigned_to",length=200) String assignedTo; @Column(name="expected_close_date") java.time.LocalDate expectedCloseDate;
 @Column(name="created_at",nullable=false) Instant createdAt; @Column(name="updated_at",nullable=false) Instant updatedAt;
 @PrePersist void p(){createdAt=Instant.now();updatedAt=createdAt;} @PreUpdate void u(){updatedAt=Instant.now();}
 public Long getId(){return id;} public Long getTenantId(){return tenantId;} public void setTenantId(Long v){tenantId=v;} public String getLeadNumber(){return leadNumber;} public void setLeadNumber(String v){leadNumber=v;} public String getName(){return name;} public void setName(String v){name=v;} public String getEmail(){return email;} public void setEmail(String v){email=v;} public String getPhone(){return phone;} public void setPhone(String v){phone=v;} public String getCompanyName(){return companyName;} public void setCompanyName(String v){companyName=v;} public String getSource(){return source;} public void setSource(String v){source=v;} public String getStatus(){return status;} public void setStatus(String v){status=v;} public String getRating(){return rating;} public void setRating(String v){rating=v;} public BigDecimal getEstimatedValue(){return estimatedValue;} public void setEstimatedValue(BigDecimal v){estimatedValue=v;} public String getAssignedTo(){return assignedTo;} public void setAssignedTo(String v){assignedTo=v;} public java.time.LocalDate getExpectedCloseDate(){return expectedCloseDate;} public void setExpectedCloseDate(java.time.LocalDate v){expectedCloseDate=v;}
}
