package com.sivion.api.inventory.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

@Entity
@Table(name="inventory_dispatch_items")
public class DispatchItem {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
    @ManyToOne(fetch=FetchType.LAZY, optional=false) @JoinColumn(name="dispatch_id", nullable=false) private Dispatch dispatch;
    @NotNull @Column(name="product_id", nullable=false) private Long productId;
    @NotNull @Column(nullable=false, precision=18, scale=4) private BigDecimal quantity;
    @Column(name="warehouse_id", nullable=false) private Long warehouseId;
    public Long getId(){return id;} public Dispatch getDispatch(){return dispatch;} public void setDispatch(Dispatch v){dispatch=v;} public Long getProductId(){return productId;} public void setProductId(Long v){productId=v;} public BigDecimal getQuantity(){return quantity;} public void setQuantity(BigDecimal v){quantity=v;} public Long getWarehouseId(){return warehouseId;} public void setWarehouseId(Long v){warehouseId=v;}
}
