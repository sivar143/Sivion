package com.sivion.api.integration;

import com.sivion.api.sales.service.SalesOrderService;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import java.util.Map;

@Component public class SalesOrderInventoryListener {
 private final SalesOrderService sales;
 public SalesOrderInventoryListener(SalesOrderService sales){this.sales=sales;}
 @RabbitListener(queues="sivion.sales.reservations") public void reserved(Map<String,Object> event){sales.markReserved(number(event.get("salesOrderId")));}
 @RabbitListener(queues="sivion.sales.reservation-failures") public void failed(Map<String,Object> event){sales.markReservationFailed(number(event.get("salesOrderId")));}
 @RabbitListener(queues="sivion.sales.dispatches") public void dispatched(Map<String,Object> event){sales.markDispatched(number(event.get("salesOrderId")),Boolean.TRUE.equals(event.get("complete")));}
 private Long number(Object v){return v==null?null:Long.valueOf(String.valueOf(v));}
}