package com.sivion.api.supplychain.domain;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;

/** Traceable supplier or warehouse lot for a raw material. */
@Entity
@Table(name = "supply_chain_material_lots", uniqueConstraints = {
    @UniqueConstraint(name = "uk_sc_material_lot_tenant_code", columnNames = {"tenant_id", "lot_code"})
})
public class MaterialLot {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(name = "tenant_id", nullable = false) private Long tenantId = 1L;
    @Column(name = "lot_code", nullable = false, length = 80) private String lotCode;
    @Column(name = "material_id", nullable = false) private Long materialId;
    @Column(name = "supplier_id") private Long supplierId;
    @Column(name = "supplier_name", length = 180) private String supplierName;
    @Column(name = "supplier_lot_code", length = 100) private String supplierLotCode;
    @Column(name = "shipment_id") private Long shipmentId;
    @Column(name = "purchase_order_id") private Long purchaseOrderId;
    @Column(name = "goods_receipt_reference", length = 100) private String goodsReceiptReference;
    @Column(name = "warehouse_id") private Long warehouseId;
    @Column(nullable = false, precision = 19, scale = 4) private BigDecimal receivedQuantity;
    @Column(nullable = false, precision = 19, scale = 4) private BigDecimal acceptedQuantity = BigDecimal.ZERO;
    @Column(nullable = false, precision = 19, scale = 4) private BigDecimal rejectedQuantity = BigDecimal.ZERO;
    @Column(nullable = false, length = 30) private String qualityStatus = "PENDING_INSPECTION";
    @Column(name = "manufactured_at") private Instant manufacturedAt;
    @Column(name = "expires_at") private Instant expiresAt;
    @Column(name = "created_at", nullable = false) private Instant createdAt = Instant.now();

    protected MaterialLot() { }
    public Long getId(){return id;} public Long getTenantId(){return tenantId;}
    public String getLotCode(){return lotCode;} public void setLotCode(String v){lotCode=v;}
    public Long getMaterialId(){return materialId;} public void setMaterialId(Long v){materialId=v;}
    public Long getSupplierId(){return supplierId;} public void setSupplierId(Long v){supplierId=v;}
    public String getSupplierName(){return supplierName;} public void setSupplierName(String v){supplierName=v;}
    public String getSupplierLotCode(){return supplierLotCode;} public void setSupplierLotCode(String v){supplierLotCode=v;}
    public Long getShipmentId(){return shipmentId;} public void setShipmentId(Long v){shipmentId=v;}
    public Long getPurchaseOrderId(){return purchaseOrderId;} public void setPurchaseOrderId(Long v){purchaseOrderId=v;}
    public String getGoodsReceiptReference(){return goodsReceiptReference;} public void setGoodsReceiptReference(String v){goodsReceiptReference=v;}
    public Long getWarehouseId(){return warehouseId;} public void setWarehouseId(Long v){warehouseId=v;}
    public BigDecimal getReceivedQuantity(){return receivedQuantity;} public void setReceivedQuantity(BigDecimal v){receivedQuantity=v;}
    public BigDecimal getAcceptedQuantity(){return acceptedQuantity;} public void setAcceptedQuantity(BigDecimal v){acceptedQuantity=v;}
    public BigDecimal getRejectedQuantity(){return rejectedQuantity;} public void setRejectedQuantity(BigDecimal v){rejectedQuantity=v;}
    public String getQualityStatus(){return qualityStatus;} public void setQualityStatus(String v){qualityStatus=v;}
    public Instant getManufacturedAt(){return manufacturedAt;} public void setManufacturedAt(Instant v){manufacturedAt=v;}
    public Instant getExpiresAt(){return expiresAt;} public void setExpiresAt(Instant v){expiresAt=v;}
    public Instant getCreatedAt(){return createdAt;}
}
