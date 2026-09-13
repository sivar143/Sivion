package com.sivion.api.inventory.domain;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name="inventory_transactions", indexes=@Index(name="ix_inventory_product", columnList="tenant_id,warehouse_id,product_id"))
public class InventoryTransaction {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
    @Column(name="tenant_id", nullable=false) private Long tenantId;
    @Column(name="warehouse_id", nullable=false) private Long warehouseId;
    @Column(name="product_id", nullable=false) private Long productId;
    @Column(name="transaction_type", nullable=false, length=40) private String transactionType;
    @Column(nullable=false, precision=18, scale=4) private BigDecimal quantity;
    @Column(name="reference_type", length=40) private String referenceType;
    @Column(name="reference_id") private Long referenceId;
    @Column(name="correlation_id", length=100) private String correlationId;
    @Column(name="created_at", nullable=false) private Instant createdAt;
    @PrePersist void create(){createdAt=Instant.now();}
    public Long getId(){return id;} public Long getTenantId(){return tenantId;} public void setTenantId(Long v){tenantId=v;}
    public Long getWarehouseId(){return warehouseId;} public void setWarehouseId(Long v){warehouseId=v;} public Long getProductId(){return productId;} public void setProductId(Long v){productId=v;}
    public String getTransactionType(){return transactionType;} public void setTransactionType(String v){transactionType=v;} public BigDecimal getQuantity(){return quantity;} public void setQuantity(BigDecimal v){quantity=v;}
    public String getReferenceType(){return referenceType;} public void setReferenceType(String v){referenceType=v;} public Long getReferenceId(){return referenceId;} public void setReferenceId(Long v){referenceId=v;}
    public String getCorrelationId(){return correlationId;} public void setCorrelationId(String v){correlationId=v;} public Instant getCreatedAt(){return createdAt;}
}
