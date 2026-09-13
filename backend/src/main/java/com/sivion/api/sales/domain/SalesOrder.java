package com.sivion.api.sales.domain;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name="sales_orders", uniqueConstraints=@UniqueConstraint(name="uk_sales_order_tenant_number", columnNames={"tenant_id","order_number"}))
public class SalesOrder {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
    @Column(name="tenant_id", nullable=false) private Long tenantId;
    @Column(name="order_number", nullable=false, length=50) private String orderNumber;
    @Column(name="customer_id", nullable=false) private Long customerId;
    @Column(name="opportunity_id") private Long opportunityId;
    @Column(nullable=false, length=30) private String status="DRAFT";
    @Column(nullable=false, precision=18, scale=4) private BigDecimal totalAmount=BigDecimal.ZERO;
    @Column(name="order_date", nullable=false) private Instant orderDate;
    @Column(length=500) private String deliveryAddress;
    @Column(length=1000) private String notes;
    @OneToMany(mappedBy="order", cascade=CascadeType.ALL, orphanRemoval=true) private List<SalesOrderItem> items=new ArrayList<>();

    @PrePersist void create(){ if(orderDate==null) orderDate=Instant.now(); }
    public Long getId(){return id;} public Long getTenantId(){return tenantId;} public void setTenantId(Long v){tenantId=v;}
    public String getOrderNumber(){return orderNumber;} public void setOrderNumber(String v){orderNumber=v;}
    public Long getCustomerId(){return customerId;} public void setCustomerId(Long v){customerId=v;}
    public Long getOpportunityId(){return opportunityId;} public void setOpportunityId(Long v){opportunityId=v;}
    public String getStatus(){return status;} public void setStatus(String v){status=v;}
    public BigDecimal getTotalAmount(){return totalAmount;} public void setTotalAmount(BigDecimal v){totalAmount=v;}
    public Instant getOrderDate(){return orderDate;} public void setOrderDate(Instant v){orderDate=v;}
    public String getDeliveryAddress(){return deliveryAddress;} public void setDeliveryAddress(String v){deliveryAddress=v;}
    public String getNotes(){return notes;} public void setNotes(String v){notes=v;}
    public List<SalesOrderItem> getItems(){return items;}
    public void addItem(SalesOrderItem item){items.add(item);item.setOrder(this);}
}
