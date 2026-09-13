package com.sivion.api.integration;
import org.springframework.amqp.core.*; import org.springframework.context.annotation.Bean; import org.springframework.context.annotation.Configuration;
@Configuration public class RabbitTopology {
 public static final String EXCHANGE="sivion.events";
 @Bean TopicExchange sivionEvents(){return new TopicExchange(EXCHANGE,true,false);}
 @Bean Queue inventoryOrdersQueue(){return QueueBuilder.durable("sivion.inventory.sales-orders").build();}
 @Bean Binding inventoryOrdersBinding(Queue inventoryOrdersQueue,TopicExchange sivionEvents){return BindingBuilder.bind(inventoryOrdersQueue).to(sivionEvents).with("sales.order.confirmed");}
 @Bean Queue salesReservationsQueue(){return QueueBuilder.durable("sivion.sales.reservations").build();}
 @Bean Binding salesReservationsBinding(Queue salesReservationsQueue,TopicExchange sivionEvents){return BindingBuilder.bind(salesReservationsQueue).to(sivionEvents).with("inventory.order.reserved");}
 @Bean Queue salesReservationFailuresQueue(){return QueueBuilder.durable("sivion.sales.reservation-failures").build();}
 @Bean Binding salesReservationFailuresBinding(Queue salesReservationFailuresQueue,TopicExchange sivionEvents){return BindingBuilder.bind(salesReservationFailuresQueue).to(sivionEvents).with("inventory.order.reservation-failed");}
 @Bean Queue financeDispatchQueue(){return QueueBuilder.durable("sivion.finance.dispatches").build();}
 @Bean Binding financeDispatchBinding(Queue financeDispatchQueue,TopicExchange sivionEvents){return BindingBuilder.bind(financeDispatchQueue).to(sivionEvents).with("inventory.order.dispatched");}
 @Bean Queue salesDispatchQueue(){return QueueBuilder.durable("sivion.sales.dispatches").build();}
 @Bean Binding salesDispatchBinding(Queue salesDispatchQueue,TopicExchange sivionEvents){return BindingBuilder.bind(salesDispatchQueue).to(sivionEvents).with("inventory.order.dispatched");}
 @Bean Queue inventoryGoodsReceivedQueue(){return QueueBuilder.durable("sivion.inventory.goods-received").build();}
 @Bean Binding inventoryGoodsReceivedBinding(Queue inventoryGoodsReceivedQueue,TopicExchange sivionEvents){return BindingBuilder.bind(inventoryGoodsReceivedQueue).to(sivionEvents).with("procurement.goods-received");}
}
