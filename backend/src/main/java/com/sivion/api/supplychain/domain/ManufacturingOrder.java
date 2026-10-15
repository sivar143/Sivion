package com.sivion.api.supplychain.domain;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "supply_chain_manufacturing_orders", uniqueConstraints = {
    @UniqueConstraint(name = "uk_sc_mfg_order_tenant_number", columnNames = {"tenant_id", "order_number"})
})
public class ManufacturingOrder {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(name = "tenant_id", nullable = false) private Long tenantId = 1L;
    @Column(name = "order_number", nullable = false, length = 80) private String orderNumber;
    @Column(name = "product_id", nullable = false) private Long productId;
    @Column(name = "planned_quantity", nullable = false, precision = 19, scale = 4) private BigDecimal plannedQuantity;
    @Column(nullable = false, length = 24) private String status = "PLANNED";
    @Column(name = "created_at", nullable = false) private Instant createdAt = Instant.now();

    protected ManufacturingOrder() { }
    public ManufacturingOrder(String orderNumber, Long productId, BigDecimal plannedQuantity) {
        this.orderNumber=orderNumber; this.productId=productId; this.plannedQuantity=plannedQuantity;
    }
    public Long getId(){return id;} public Long getTenantId(){return tenantId;}
    public String getOrderNumber(){return orderNumber;} public Long getProductId(){return productId;}
    public BigDecimal getPlannedQuantity(){return plannedQuantity;} public String getStatus(){return status;}
    public void setStatus(String value){status=value;} public Instant getCreatedAt(){return createdAt;}
}