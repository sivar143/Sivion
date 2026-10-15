package com.sivion.api.supplychain.domain;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "supply_chain_inbound_receipts", uniqueConstraints = {
    @UniqueConstraint(name = "uk_sc_inbound_receipt_reference", columnNames = {"tenant_id", "shipment_id", "receipt_reference"}),
    @UniqueConstraint(name = "uk_sc_inbound_goods_receipt", columnNames = {"tenant_id", "goods_receipt_id"})
})
public class InboundShipmentReceipt {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "tenant_id", nullable = false) private Long tenantId = 1L;
    @Column(name = "shipment_id", nullable = false) private Long shipmentId;
    @Column(name = "goods_receipt_id", nullable = false) private Long goodsReceiptId;
    @Column(name = "goods_receipt_number", nullable = false, length = 100) private String goodsReceiptNumber;
    @Column(name = "receipt_reference", nullable = false, length = 80) private String receiptReference;
    @Column(name = "product_id", nullable = false) private Long productId;
    @Column(name = "warehouse_id", nullable = false) private Long warehouseId;
    @Column(name = "quantity", nullable = false, precision = 19, scale = 4) private BigDecimal quantity;
    @Column(name = "material_lot_id", nullable = false) private Long materialLotId;
    @Column(name = "lot_code", nullable = false, length = 80) private String lotCode;
    @Column(name = "final_receipt", nullable = false) private boolean finalReceipt;
    @Column(name = "recorded_at", nullable = false) private Instant recordedAt = Instant.now();

    protected InboundShipmentReceipt() { }
    public InboundShipmentReceipt(Long shipmentId, Long goodsReceiptId, String goodsReceiptNumber,
            String receiptReference, Long productId, Long warehouseId, BigDecimal quantity,
            Long materialLotId, String lotCode, boolean finalReceipt) {
        this.shipmentId=shipmentId; this.goodsReceiptId=goodsReceiptId; this.goodsReceiptNumber=goodsReceiptNumber;
        this.receiptReference=receiptReference; this.productId=productId; this.warehouseId=warehouseId;
        this.quantity=quantity; this.materialLotId=materialLotId; this.lotCode=lotCode; this.finalReceipt=finalReceipt;
    }
    public Long getId(){return id;} public Long getTenantId(){return tenantId;}
    public Long getShipmentId(){return shipmentId;} public Long getGoodsReceiptId(){return goodsReceiptId;}
    public String getGoodsReceiptNumber(){return goodsReceiptNumber;} public String getReceiptReference(){return receiptReference;}
    public Long getProductId(){return productId;} public Long getWarehouseId(){return warehouseId;}
    public BigDecimal getQuantity(){return quantity;} public Long getMaterialLotId(){return materialLotId;}
    public String getLotCode(){return lotCode;} public boolean isFinalReceipt(){return finalReceipt;}
    public Instant getRecordedAt(){return recordedAt;}
}