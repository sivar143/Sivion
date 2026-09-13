package com.sivion.api.sales.domain;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name="sales_order_items")
public class SalesOrderItem {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
    @ManyToOne(fetch=FetchType.LAZY, optional=false) @JoinColumn(name="order_id", nullable=false) private SalesOrder order;
    @Column(name="material_id", nullable=false) private Long materialId;
    @Column(nullable=false, precision=18, scale=4) private BigDecimal quantity;
    @Column(name="unit_price", nullable=false, precision=18, scale=4) private BigDecimal unitPrice;
    @Column(nullable=false, precision=18, scale=4) private BigDecimal lineTotal=BigDecimal.ZERO;
    @Column(length=200) private String description;

    public Long getId(){return id;} public SalesOrder getOrder(){return order;} public void setOrder(SalesOrder v){order=v;}
    public Long getMaterialId(){return materialId;} public void setMaterialId(Long v){materialId=v;}
    public BigDecimal getQuantity(){return quantity;} public void setQuantity(BigDecimal v){quantity=v;}
    public BigDecimal getUnitPrice(){return unitPrice;} public void setUnitPrice(BigDecimal v){unitPrice=v;}
    public BigDecimal getLineTotal(){return lineTotal;} public void setLineTotal(BigDecimal v){lineTotal=v;}
    public String getDescription(){return description;} public void setDescription(String v){description=v;}
}
