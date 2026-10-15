package com.sivion.api.supplychain.domain;

import jakarta.persistence.*;
import java.time.Instant;

/** Current shipment projection; immutable milestones are stored separately in ShipmentEvent. */
@Entity
@Table(name = "supply_chain_shipments", uniqueConstraints = {
    @UniqueConstraint(name = "uk_supply_shipment_tenant_number", columnNames = {"tenant_id", "shipment_number"}),
    @UniqueConstraint(name = "uk_supply_shipment_public_token", columnNames = {"public_tracking_token"})
})
public class Shipment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tenant_id", nullable = false)
    private Long tenantId = 1L;

    @Column(name = "shipment_number", nullable = false, length = 60)
    private String shipmentNumber;

    @Column(nullable = false, length = 20)
    private String direction;

    @Column(nullable = false, length = 30)
    private String status;

    @Column(name = "reference_number", length = 100)
    private String referenceNumber;

    @Column(name = "purchase_order_id")
    private Long purchaseOrderId;

    @Column(name = "goods_receipt_reference", length = 100)
    private String goodsReceiptReference;

    @Column(name = "sales_order_id")
    private Long salesOrderId;

    @Column(name = "dispatch_id")
    private Long dispatchId;

    @Column(name = "supplier_name", length = 180)
    private String supplierName;

    @Column(name = "customer_name", length = 180)
    private String customerName;

    @Column(name = "carrier_name", length = 120)
    private String carrierName;

    @Column(name = "carrier_tracking_number", length = 160)
    private String carrierTrackingNumber;

    @Column(name = "public_tracking_token", nullable = false, length = 64)
    private String publicTrackingToken;

    @Column(name = "origin_location", length = 250)
    private String originLocation;

    @Column(name = "destination_location", length = 250)
    private String destinationLocation;

    @Column(name = "dispatched_at")
    private Instant dispatchedAt;

    @Column(name = "estimated_arrival_at")
    private Instant estimatedArrivalAt;

    @Column(name = "delivered_at")
    private Instant deliveredAt;

    @Column(length = 1000)
    private String notes;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt = Instant.now();

    public Shipment() {
    }

    public Long getId() { return id; }
    public Long getTenantId() { return tenantId; }
    public String getShipmentNumber() { return shipmentNumber; }
    public void setShipmentNumber(String value) { shipmentNumber = value; }
    public String getDirection() { return direction; }
    public void setDirection(String value) { direction = value; }
    public String getStatus() { return status; }
    public void setStatus(String value) { status = value; }
    public String getReferenceNumber() { return referenceNumber; }
    public void setReferenceNumber(String value) { referenceNumber = value; }
    public Long getPurchaseOrderId() { return purchaseOrderId; }
    public void setPurchaseOrderId(Long value) { purchaseOrderId = value; }
    public String getGoodsReceiptReference() { return goodsReceiptReference; }
    public void setGoodsReceiptReference(String value) { goodsReceiptReference = value; }
    public Long getSalesOrderId() { return salesOrderId; }
    public void setSalesOrderId(Long value) { salesOrderId = value; }
    public Long getDispatchId() { return dispatchId; }
    public void setDispatchId(Long value) { dispatchId = value; }
    public String getSupplierName() { return supplierName; }
    public void setSupplierName(String value) { supplierName = value; }
    public String getCustomerName() { return customerName; }
    public void setCustomerName(String value) { customerName = value; }
    public String getCarrierName() { return carrierName; }
    public void setCarrierName(String value) { carrierName = value; }
    public String getCarrierTrackingNumber() { return carrierTrackingNumber; }
    public void setCarrierTrackingNumber(String value) { carrierTrackingNumber = value; }
    public String getPublicTrackingToken() { return publicTrackingToken; }
    public void setPublicTrackingToken(String value) { publicTrackingToken = value; }
    public String getOriginLocation() { return originLocation; }
    public void setOriginLocation(String value) { originLocation = value; }
    public String getDestinationLocation() { return destinationLocation; }
    public void setDestinationLocation(String value) { destinationLocation = value; }
    public Instant getDispatchedAt() { return dispatchedAt; }
    public void setDispatchedAt(Instant value) { dispatchedAt = value; }
    public Instant getEstimatedArrivalAt() { return estimatedArrivalAt; }
    public void setEstimatedArrivalAt(Instant value) { estimatedArrivalAt = value; }
    public Instant getDeliveredAt() { return deliveredAt; }
    public void setDeliveredAt(Instant value) { deliveredAt = value; }
    public String getNotes() { return notes; }
    public void setNotes(String value) { notes = value; }
    public Instant getCreatedAt() { return createdAt; }
}
