package com.sivion.api.supplychain.domain;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;

/** Quantity-aware genealogy edge between a raw-material lot and a production lot/order. */
@Entity
@Table(name = "supply_chain_lot_consumptions")
public class LotConsumption {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(name = "tenant_id", nullable = false) private Long tenantId = 1L;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "material_lot_id", nullable = false) private MaterialLot materialLot;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "production_lot_id", nullable = false) private ProductionLot productionLot;
    @Column(name = "manufacturing_order_id", nullable = false) private Long manufacturingOrderId;
    @Column(nullable = false, precision = 19, scale = 4) private BigDecimal quantityConsumed;
    @Column(nullable = false, length = 30) private String unit;
    @Column(name = "recorded_at", nullable = false) private Instant recordedAt = Instant.now();
    @Column(length = 500) private String remarks;

    protected LotConsumption() { }
    public LotConsumption(MaterialLot materialLot, ProductionLot productionLot, Long manufacturingOrderId, BigDecimal quantity, String unit, String remarks) {
        this.materialLot=materialLot; this.productionLot=productionLot; this.manufacturingOrderId=manufacturingOrderId;
        this.quantityConsumed=quantity; this.unit=unit; this.remarks=remarks; this.tenantId=materialLot.getTenantId();
    }
    public Long getId(){return id;} public Long getTenantId(){return tenantId;}
    public MaterialLot getMaterialLot(){return materialLot;} public ProductionLot getProductionLot(){return productionLot;}
    public Long getManufacturingOrderId(){return manufacturingOrderId;} public BigDecimal getQuantityConsumed(){return quantityConsumed;}
    public String getUnit(){return unit;} public Instant getRecordedAt(){return recordedAt;} public String getRemarks(){return remarks;}
}
