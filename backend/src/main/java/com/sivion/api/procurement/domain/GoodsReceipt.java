package com.sivion.api.procurement.domain;

import jakarta.persistence.*;
import java.math.BigDecimal; import java.time.Instant;

@Entity @Table(name="procurement_goods_receipts")
public class GoodsReceipt {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
 @Column(name="tenant_id",nullable=false) private Long tenantId;
 @Column(nullable=false) private String receiptNumber;
 @Column(nullable=false) private Long purchaseOrderId;
 @Column(nullable=false) private BigDecimal quantity=BigDecimal.ZERO;
 @Column(nullable=false) private String status="RECEIVED";
 @Column(nullable=false) private Instant receivedAt=Instant.now();
 public Long getId(){return id;} public Long getTenantId(){return tenantId;} public void setTenantId(Long v){tenantId=v;} public String getReceiptNumber(){return receiptNumber;} public void setReceiptNumber(String v){receiptNumber=v;} public Long getPurchaseOrderId(){return purchaseOrderId;} public void setPurchaseOrderId(Long v){purchaseOrderId=v;} public BigDecimal getQuantity(){return quantity;} public void setQuantity(BigDecimal v){quantity=v;} public String getStatus(){return status;} public void setStatus(String v){status=v;} public Instant getReceivedAt(){return receivedAt;}
}
