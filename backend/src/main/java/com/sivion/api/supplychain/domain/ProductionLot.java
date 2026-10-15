package com.sivion.api.supplychain.domain;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;

/** Finished-product lot produced by a manufacturing order. */
@Entity
@Table(name = "supply_chain_production_lots", uniqueConstraints = {
    @UniqueConstraint(name = "uk_sc_production_lot_tenant_code", columnNames = {"tenant_id", "lot_code"})
})
public class ProductionLot {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(name = "tenant_id", nullable = false) private Long tenantId = 1L;
    @Column(name = "lot_code", nullable = false, length = 80) private String lotCode;
    @Column(name = "product_id", nullable = false) private Long productId;
    @Column(name = "manufacturing_order_id", nullable = false) private Long manufacturingOrderId;
    @Column(name = "shipment_id") private Long shipmentId;
    @Column(nullable = false, precision = 19, scale = 4) private BigDecimal producedQuantity;
    @Column(nullable = false, precision = 19, scale = 4) private BigDecimal rejectedQuantity = BigDecimal.ZERO;
    @Column(nullable = false, length = 30) private String qualityStatus = "PENDING_INSPECTION";
    @Column(name = "warehouse_id") private Long warehouseId;
    @Column(name = "produced_at", nullable = false) private Instant producedAt = Instant.now();

    public ProductionLot() { }
    public Long getId(){return id;} public Long getTenantId(){return tenantId;}
    public String getLotCode(){return lotCode;} public void setLotCode(String v){lotCode=v;}
    public Long getProductId(){return productId;} public void setProductId(Long v){productId=v;}
    public Long getManufacturingOrderId(){return manufacturingOrderId;} public void setManufacturingOrderId(Long v){manufacturingOrderId=v;}
    public Long getShipmentId(){return shipmentId;} public void setShipmentId(Long v){shipmentId=v;}
    public BigDecimal getProducedQuantity(){return producedQuantity;} public void setProducedQuantity(BigDecimal v){producedQuantity=v;}
    public BigDecimal getRejectedQuantity(){return rejectedQuantity;} public void setRejectedQuantity(BigDecimal v){rejectedQuantity=v;}
    public String getQualityStatus(){return qualityStatus;} public void setQualityStatus(String v){qualityStatus=v;}
    public Long getWarehouseId(){return warehouseId;} public void setWarehouseId(Long v){warehouseId=v;}
    public Instant getProducedAt(){return producedAt;} public void setProducedAt(Instant v){producedAt=v;}
}
