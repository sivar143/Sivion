package com.sivion.api.supplychain.domain;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "supply_chain_proof_of_delivery")
public class ProofOfDelivery {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(name = "tenant_id", nullable = false) private Long tenantId = 1L;
    @OneToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "shipment_id", nullable = false, unique = true) private Shipment shipment;
    @Column(name = "recipient_name", nullable = false, length = 180) private String recipientName;
    @Column(name = "delivered_at", nullable = false) private Instant deliveredAt;
    @Column(length = 1000) private String notes;
    @Column(name = "document_reference", length = 500) private String documentReference;
    @Column(name = "recorded_by", length = 160) private String recordedBy;
    @Column(name = "recorded_at", nullable = false) private Instant recordedAt = Instant.now();

    protected ProofOfDelivery() { }
    public ProofOfDelivery(Shipment shipment, String recipientName, Instant deliveredAt, String notes, String documentReference, String recordedBy) {
        this.shipment=shipment; this.tenantId=shipment.getTenantId(); this.recipientName=recipientName;
        this.deliveredAt=deliveredAt; this.notes=notes; this.documentReference=documentReference; this.recordedBy=recordedBy;
    }
    public Long getId(){return id;} public Shipment getShipment(){return shipment;} public String getRecipientName(){return recipientName;}
    public Instant getDeliveredAt(){return deliveredAt;} public String getNotes(){return notes;}
    public String getDocumentReference(){return documentReference;} public String getRecordedBy(){return recordedBy;}
    public Instant getRecordedAt(){return recordedAt;}
}
