package com.sivion.api.supplychain.domain;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "supply_chain_production_lot_shipments", uniqueConstraints = {
    @UniqueConstraint(name = "uk_sc_production_lot_shipment", columnNames = {"tenant_id", "production_lot_id", "shipment_id"})
})
public class ProductionLotShipment {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(name = "tenant_id", nullable = false) private Long tenantId = 1L;
    @Column(name = "production_lot_id", nullable = false) private Long productionLotId;
    @Column(name = "shipment_id", nullable = false) private Long shipmentId;
    @Column(nullable = false, precision = 19, scale = 4) private BigDecimal quantity;
    @Column(name = "created_by", nullable = false, length = 180) private String createdBy;
    @Column(name = "created_at", nullable = false) private Instant createdAt = Instant.now();

    protected ProductionLotShipment() { }
    public ProductionLotShipment(Long productionLotId, Long shipmentId, BigDecimal quantity, String createdBy) {
        this.productionLotId=productionLotId; this.shipmentId=shipmentId; this.quantity=quantity; this.createdBy=createdBy;
    }
    public Long getId(){return id;} public Long getTenantId(){return tenantId;}
    public Long getProductionLotId(){return productionLotId;} public Long getShipmentId(){return shipmentId;}
    public BigDecimal getQuantity(){return quantity;} public String getCreatedBy(){return createdBy;}
    public Instant getCreatedAt(){return createdAt;}
}