package com.sivion.api.procurement.domain;

import jakarta.persistence.*;
import java.math.BigDecimal; import java.time.Instant; import java.util.*;

@Entity @Table(name="procurement_orders")
public class PurchaseOrder {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
 @Column(name="tenant_id",nullable=false) private Long tenantId;
 @Column(nullable=false) private String orderNumber;
 @Column(nullable=false) private Long supplierId;
 @Column(nullable=false) private BigDecimal amount=BigDecimal.ZERO;
 @Column(nullable=false) private String status="DRAFT";
 @Column(nullable=false) private Instant orderDate=Instant.now();
 @OneToMany @JoinColumn(name="purchase_order_id",referencedColumnName="id",insertable=false,updatable=false) private List<PurchaseOrderItem> items=new ArrayList<>();
 public Long getId(){return id;} public Long getTenantId(){return tenantId;} public void setTenantId(Long v){tenantId=v;} public String getOrderNumber(){return orderNumber;} public void setOrderNumber(String v){orderNumber=v;} public Long getSupplierId(){return supplierId;} public void setSupplierId(Long v){supplierId=v;} public BigDecimal getAmount(){return amount;} public void setAmount(BigDecimal v){amount=v;} public String getStatus(){return status;} public void setStatus(String v){status=v;} public Instant getOrderDate(){return orderDate;} public List<PurchaseOrderItem> getItems(){return items;}
}
