package com.sivion.api.inventory.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name="inventory_dispatches", uniqueConstraints=@UniqueConstraint(name="uk_dispatch_tenant_number", columnNames={"tenant_id","dispatch_number"}))
public class Dispatch {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
    @Column(name="tenant_id", nullable=false) private Long tenantId;
    @NotBlank @Column(name="dispatch_number", nullable=false, length=50) private String dispatchNumber;
    @NotNull @Column(name="customer_id", nullable=false) private Long customerId;
    @Column(name="reference_number", length=80) private String referenceNumber;
    @Column(name="dispatch_date", nullable=false) private Instant dispatchDate;
    @Column(name="delivery_address", length=500) private String deliveryAddress;
    @Column(length=1000) private String notes;
    @Column(nullable=false, length=20) private String status="DISPATCHED";
    @OneToMany(mappedBy="dispatch", cascade=CascadeType.ALL, orphanRemoval=true) private List<DispatchItem> items=new ArrayList<>();
    public Long getId(){return id;} public Long getTenantId(){return tenantId;} public void setTenantId(Long v){tenantId=v;} public String getDispatchNumber(){return dispatchNumber;} public void setDispatchNumber(String v){dispatchNumber=v;}
    public Long getCustomerId(){return customerId;} public void setCustomerId(Long v){customerId=v;} public String getReferenceNumber(){return referenceNumber;} public void setReferenceNumber(String v){referenceNumber=v;}
    public Instant getDispatchDate(){return dispatchDate;} public void setDispatchDate(Instant v){dispatchDate=v;} public String getDeliveryAddress(){return deliveryAddress;} public void setDeliveryAddress(String v){deliveryAddress=v;} public String getNotes(){return notes;} public void setNotes(String v){notes=v;} public String getStatus(){return status;} public void setStatus(String v){status=v;} public List<DispatchItem> getItems(){return items;}
    public void addItem(DispatchItem item){items.add(item);item.setDispatch(this);}
}
