package com.sivion.api.supplychain.domain;

import jakarta.persistence.*;
import java.time.Instant;

/** Append-only shipment milestone; corrections should be recorded as new events. */
@Entity
@Table(name = "supply_chain_shipment_events", uniqueConstraints = {
    @UniqueConstraint(name = "uk_supply_event_idempotency", columnNames = {"tenant_id", "idempotency_key"})
})
public class ShipmentEvent {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tenant_id", nullable = false)
    private Long tenantId = 1L;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "shipment_id", nullable = false)
    private Shipment shipment;

    @Column(name = "previous_status", length = 30)
    private String previousStatus;

    @Column(name = "new_status", nullable = false, length = 30)
    private String newStatus;

    @Column(name = "event_type", nullable = false, length = 40)
    private String eventType;

    @Column(name = "event_time", nullable = false)
    private Instant eventTime;

    @Column(name = "recorded_at", nullable = false)
    private Instant recordedAt = Instant.now();

    @Column(length = 250)
    private String location;

    @Column(length = 1000)
    private String remarks;

    @Column(name = "source_type", nullable = false, length = 20)
    private String sourceType;

    @Column(name = "actor", length = 160)
    private String actor;

    @Column(name = "idempotency_key", length = 120)
    private String idempotencyKey;

    protected ShipmentEvent() {
    }

    public ShipmentEvent(Shipment shipment, String previousStatus, String newStatus,
            String eventType, Instant eventTime, String location, String remarks,
            String sourceType, String actor, String idempotencyKey) {
        this.shipment = shipment;
        this.previousStatus = previousStatus;
        this.newStatus = newStatus;
        this.eventType = eventType;
        this.eventTime = eventTime;
        this.location = location;
        this.remarks = remarks;
        this.sourceType = sourceType;
        this.actor = actor;
        this.idempotencyKey = idempotencyKey;
        this.tenantId = shipment.getTenantId();
    }

    public Long getId() { return id; }
    public Shipment getShipment() { return shipment; }
    public String getPreviousStatus() { return previousStatus; }
    public String getNewStatus() { return newStatus; }
    public String getEventType() { return eventType; }
    public Instant getEventTime() { return eventTime; }
    public Instant getRecordedAt() { return recordedAt; }
    public String getLocation() { return location; }
    public String getRemarks() { return remarks; }
    public String getSourceType() { return sourceType; }
    public String getActor() { return actor; }
    public String getIdempotencyKey() { return idempotencyKey; }
}
