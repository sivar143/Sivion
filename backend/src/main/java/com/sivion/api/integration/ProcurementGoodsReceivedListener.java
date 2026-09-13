package com.sivion.api.integration;

import com.sivion.api.inventory.service.InventoryService;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import java.math.BigDecimal; import java.util.Map;

@Component public class ProcurementGoodsReceivedListener {
 private final InventoryService inventory;
 public ProcurementGoodsReceivedListener(InventoryService inventory){this.inventory=inventory;}
 @RabbitListener(queues="sivion.inventory.goods-received")
 public void onGoodsReceived(Map<String,Object> event){Long productId=number(event.get("productId"));Long warehouseId=number(event.get("warehouseId"));BigDecimal quantity=decimal(event.get("quantity"));String reference=String.valueOf(event.getOrDefault("purchaseOrderId","PROCUREMENT"));if(productId==null||warehouseId==null||quantity==null||quantity.signum()<=0)return;inventory.stock(productId,warehouseId,quantity,"GOODS_RECEIVED",reference);}
 private Long number(Object value){return value==null?null:Long.valueOf(String.valueOf(value));}
 private BigDecimal decimal(Object value){return value==null?null:new BigDecimal(String.valueOf(value));}
}
