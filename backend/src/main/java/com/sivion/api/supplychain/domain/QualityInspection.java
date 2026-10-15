package com.sivion.api.supplychain.domain;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;

/** Immutable audit record for each raw-material or finished-product quality inspection. */
@Entity
@Table(name = "supply_chain_quality_inspections")
public class QualityInspection {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(name = "tenant_id", nullable = false) private Long tenantId = 1L;
    @Column(name = "lot_code", nullable = false, length = 80) private String lotCode;
    @Column(name = "lot_type", nullable = false, length = 24) private String lotType;
    @Column(name = "accepted_quantity", nullable = false, precision = 19, scale = 4) private BigDecimal acceptedQuantity;
    @Column(name = "rejected_quantity", nullable = false, precision = 19, scale = 4) private BigDecimal rejectedQuantity;
    @Column(name = "inspector", nullable = false, length = 180) private String inspector;
    @Column(length = 1000) private String notes;
    @Column(name = "inspected_at", nullable = false) private Instant inspectedAt = Instant.now();

    protected QualityInspection() { }
    public QualityInspection(String lotCode, String lotType, BigDecimal acceptedQuantity,
            BigDecimal rejectedQuantity, String inspector, String notes) {
        this.lotCode = lotCode; this.lotType = lotType; this.acceptedQuantity = acceptedQuantity;
        this.rejectedQuantity = rejectedQuantity; this.inspector = inspector; this.notes = notes;
    }
    public Long getId() { return id; } public Long getTenantId() { return tenantId; }
    public String getLotCode() { return lotCode; } public String getLotType() { return lotType; }
    public BigDecimal getAcceptedQuantity() { return acceptedQuantity; }
    public BigDecimal getRejectedQuantity() { return rejectedQuantity; }
    public String getInspector() { return inspector; } public String getNotes() { return notes; }
    public Instant getInspectedAt() { return inspectedAt; }
}