package com.sivion.api.procurement.domain;

import jakarta.persistence.*;
import java.math.BigDecimal; import java.time.Instant;

@Entity @Table(name="procurement_requests")
public class PurchaseRequest {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
 @Column(name="tenant_id",nullable=false) private Long tenantId;
 @Column(nullable=false) private String requestNumber;
 @Column(nullable=false) private String description;
 @Column(nullable=false) private BigDecimal estimatedAmount=BigDecimal.ZERO;
 @Column(nullable=false) private String status="DRAFT";
 @Column(nullable=false) private String requestedBy;
 @Column(nullable=false) private Instant requestedAt=Instant.now();
 public Long getId(){return id;} public Long getTenantId(){return tenantId;} public void setTenantId(Long v){tenantId=v;} public String getRequestNumber(){return requestNumber;} public void setRequestNumber(String v){requestNumber=v;} public String getDescription(){return description;} public void setDescription(String v){description=v;} public BigDecimal getEstimatedAmount(){return estimatedAmount;} public void setEstimatedAmount(BigDecimal v){estimatedAmount=v;} public String getStatus(){return status;} public void setStatus(String v){status=v;} public String getRequestedBy(){return requestedBy;} public void setRequestedBy(String v){requestedBy=v;} public Instant getRequestedAt(){return requestedAt;}
}
