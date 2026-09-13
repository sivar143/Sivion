package com.sivion.api.integration;

import com.sivion.api.inventory.service.InventoryService;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;
import java.math.BigDecimal;
import java.util.*;

@Component public class InventorySalesOrderListener {
 private final InventoryService inventory; private final RabbitTemplate rabbit;
 public InventorySalesOrderListener(InventoryService inventory,RabbitTemplate rabbit){this.inventory=inventory;this.rabbit=rabbit;}
 @RabbitListener(queues="sivion.inventory.sales-orders")
 public void onConfirmed(Map<String,Object> event){Long orderId=number(event.get("salesOrderId"));try{List<InventoryService.ReservationRequest> items=new ArrayList<>();Object raw=event.get("items");if(raw instanceof Collection<?> c)for(Object x:c)if(x instanceof Map<?,?> m)items.add(new InventoryService.ReservationRequest(number(m.get("productId")),null,decimal(m.get("quantity"))));inventory.reserve(orderId,items);}catch(RuntimeException ex){rabbit.convertAndSend(RabbitTopology.EXCHANGE,"inventory.order.reservation-failed",Map.of("salesOrderId",orderId,"reason",String.valueOf(ex.getMessage())));}}
 private Long number(Object v){return v==null?null:Long.valueOf(String.valueOf(v));} private BigDecimal decimal(Object v){return v==null?null:new BigDecimal(String.valueOf(v));}
}
