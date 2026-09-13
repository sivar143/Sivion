package com.sivion.api.integration;

import com.sivion.api.inventory.domain.InventoryReservation;
import com.sivion.api.inventory.repo.InventoryReservationRepository;
import com.sivion.api.inventory.service.InventoryService;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.util.*;

@Component public class InventorySalesOrderListener {
 private static final long TENANT=1L;
 private final InventoryService inventory; private final InventoryReservationRepository reservations; private final RabbitTemplate rabbit;
 public InventorySalesOrderListener(InventoryService inventory,InventoryReservationRepository reservations,RabbitTemplate rabbit){this.inventory=inventory;this.reservations=reservations;this.rabbit=rabbit;}
 @RabbitListener(queues="sivion.inventory.sales-orders") @Transactional
 public void onSalesOrderEvent(Map<String,Object> event){Long orderId=number(event.get("salesOrderId"));if(orderId==null)return;if(Boolean.TRUE.equals(event.get("cancelled"))){release(orderId);return;}try{List<InventoryService.ReservationRequest> items=new ArrayList<>();Object raw=event.get("items");if(raw instanceof Collection<?> c)for(Object x:c)if(x instanceof Map<?,?> m)items.add(new InventoryService.ReservationRequest(number(m.get("productId")),null,decimal(m.get("quantity"))));inventory.reserve(orderId,items);}catch(RuntimeException ex){rabbit.convertAndSend(RabbitTopology.EXCHANGE,"inventory.order.reservation-failed",Map.of("salesOrderId",orderId,"reason",String.valueOf(ex.getMessage())));}}
 private void release(Long orderId){List<InventoryReservation> active=reservations.findByTenantIdAndSalesOrderIdAndStatus(TENANT,orderId,"RESERVED");for(InventoryReservation r:active)r.setStatus("RELEASED");}
 private Long number(Object v){return v==null?null:Long.valueOf(String.valueOf(v));} private BigDecimal decimal(Object v){return v==null?null:new BigDecimal(String.valueOf(v));}
}
