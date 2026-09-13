package com.sivion.api.procurement.domain;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name="procurement_order_items")
public class PurchaseOrderItem {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
 @Column(name="purchase_order_id",nullable=false) private Long purchaseOrderId;
 @Column(name="product_id",nullable=false) private Long productId;
 @Column(name="warehouse_id",nullable=false) private Long warehouseId;
 @Column(nullable=false) private BigDecimal quantity=BigDecimal.ZERO;
 @Column(nullable=false) private BigDecimal unitPrice=BigDecimal.ZERO;
 public Long getId(){return id;} public Long getPurchaseOrderId(){return purchaseOrderId;} public void setPurchaseOrderId(Long v){purchaseOrderId=v;} public Long getProductId(){return productId;} public void setProductId(Long v){productId=v;} public Long getWarehouseId(){return warehouseId;} public void setWarehouseId(Long v){warehouseId=v;} public BigDecimal getQuantity(){return quantity;} public void setQuantity(BigDecimal v){quantity=v;} public BigDecimal getUnitPrice(){return unitPrice;} public void setUnitPrice(BigDecimal v){unitPrice=v;}
}
