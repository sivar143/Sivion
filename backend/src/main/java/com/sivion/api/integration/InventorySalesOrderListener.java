package com.sivion.api.integration;

import com.sivion.api.inventory.service.InventoryService;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import java.math.BigDecimal;
import java.util.*;

@Component public class InventorySalesOrderListener {
 private final InventoryService inventory;
 public InventorySalesOrderListener(InventoryService inventory){this.inventory=inventory;}
 @RabbitListener(queues="sivion.inventory.sales-orders")
 public void onConfirmed(Map<String,Object> event){Long orderId=number(event.get("salesOrderId"));List<InventoryService.ReservationRequest> items=new ArrayList<>();Object raw=event.get("items");if(raw instanceof Collection<?> c)for(Object x:c)if(x instanceof Map<?,?> m)items.add(new InventoryService.ReservationRequest(number(m.get("productId")),null,decimal(m.get("quantity"))));inventory.reserve(orderId,items);}
 private Long number(Object v){return v==null?null:Long.valueOf(String.valueOf(v));} private BigDecimal decimal(Object v){return v==null?null:new BigDecimal(String.valueOf(v));}
}
