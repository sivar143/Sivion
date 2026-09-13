package com.sivion.api.inventory.domain;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name="inventory_reservations", uniqueConstraints=@UniqueConstraint(name="uk_inventory_reservation", columnNames={"tenant_id","sales_order_id","product_id","warehouse_id"}))
public class InventoryReservation {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
    @Column(name="tenant_id", nullable=false) private Long tenantId;
    @Column(name="sales_order_id", nullable=false) private Long salesOrderId;
    @Column(name="product_id", nullable=false) private Long productId;
    @Column(name="warehouse_id", nullable=false) private Long warehouseId;
    @Column(nullable=false, precision=18, scale=4) private BigDecimal quantity;
    @Column(nullable=false, length=20) private String status="RESERVED";
    @Column(name="created_at", nullable=false) private Instant createdAt;
    @Column(name="updated_at", nullable=false) private Instant updatedAt;
    @PrePersist void prePersist(){createdAt=Instant.now();updatedAt=createdAt;}
    @PreUpdate void preUpdate(){updatedAt=Instant.now();}
    public Long getId(){return id;} public Long getTenantId(){return tenantId;} public void setTenantId(Long v){tenantId=v;}
    public Long getSalesOrderId(){return salesOrderId;} public void setSalesOrderId(Long v){salesOrderId=v;}
    public Long getProductId(){return productId;} public void setProductId(Long v){productId=v;}
    public Long getWarehouseId(){return warehouseId;} public void setWarehouseId(Long v){warehouseId=v;}
    public BigDecimal getQuantity(){return quantity;} public void setQuantity(BigDecimal v){quantity=v;}
    public String getStatus(){return status;} public void setStatus(String v){status=v;}
    public Instant getCreatedAt(){return createdAt;} public Instant getUpdatedAt(){return updatedAt;}
}
