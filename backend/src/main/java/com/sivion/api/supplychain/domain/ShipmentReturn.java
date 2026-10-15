package com.sivion.api.supplychain.domain;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "supply_chain_returns", uniqueConstraints = {
    @UniqueConstraint(name = "uk_sc_return_tenant_reference", columnNames = {"tenant_id", "return_reference"})
})
public class ShipmentReturn {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(name = "tenant_id", nullable = false) private Long tenantId = 1L;
    @Column(name = "shipment_id", nullable = false) private Long shipmentId;
    @Column(name = "return_reference", nullable = false, length = 100) private String returnReference;
    @Column(name = "product_id", nullable = false) private Long productId;
    @Column(name = "warehouse_id", nullable = false) private Long warehouseId;
    @Column(nullable = false, precision = 19, scale = 4) private BigDecimal quantity;
    @Column(nullable = false, length = 30) private String disposition;
    @Column(nullable = false, length = 500) private String reason;
    @Column(name = "recorded_by", nullable = false, length = 180) private String recordedBy;
    @Column(name = "recorded_at", nullable = false) private Instant recordedAt = Instant.now();

    protected ShipmentReturn() { }
    public ShipmentReturn(Long shipmentId, String returnReference, Long productId, Long warehouseId,
            BigDecimal quantity, String disposition, String reason, String recordedBy) {
        this.shipmentId=shipmentId; this.returnReference=returnReference; this.productId=productId;
        this.warehouseId=warehouseId; this.quantity=quantity; this.disposition=disposition;
        this.reason=reason; this.recordedBy=recordedBy;
    }
    public Long getId(){return id;} public Long getTenantId(){return tenantId;}
    public Long getShipmentId(){return shipmentId;} public String getReturnReference(){return returnReference;}
    public Long getProductId(){return productId;} public Long getWarehouseId(){return warehouseId;}
    public BigDecimal getQuantity(){return quantity;} public String getDisposition(){return disposition;}
    public String getReason(){return reason;} public String getRecordedBy(){return recordedBy;}
    public Instant getRecordedAt(){return recordedAt;}
}