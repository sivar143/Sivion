package com.sivion.api.integration;

import com.sivion.api.sales.service.SalesOrderService;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import java.util.Map;

@Component public class SalesReservationListener {
 private final SalesOrderService sales;
 public SalesReservationListener(SalesOrderService sales){this.sales=sales;}
 @RabbitListener(queues="sivion.sales.reservations")
 public void onReserved(Map<String,Object> event){Long orderId=number(event.get("salesOrderId"));if(orderId!=null)sales.markReserved(orderId);}
 @RabbitListener(queues="sivion.sales.reservation-failures")
 public void onFailure(Map<String,Object> event){Long orderId=number(event.get("salesOrderId"));if(orderId!=null)sales.markReservationFailed(orderId);}
 private Long number(Object v){return v==null?null:Long.valueOf(String.valueOf(v));}
}
