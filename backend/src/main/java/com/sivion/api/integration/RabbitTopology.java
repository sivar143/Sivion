package com.sivion.api.integration;

import org.springframework.amqp.core.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitTopology {
    public static final String EXCHANGE = "sivion.events";
    public static final String DLX = "sivion.events.dlx";

    @Bean TopicExchange sivionEvents() { return new TopicExchange(EXCHANGE, true, false); }
    @Bean DirectExchange sivionDeadLetterExchange() { return new DirectExchange(DLX, true, false); }

    private Queue eventQueue(String name, String dlqRoutingKey) {
        return QueueBuilder.durable(name)
                .withArgument("x-dead-letter-exchange", DLX)
                .withArgument("x-dead-letter-routing-key", dlqRoutingKey)
                .build();
    }

    private Queue deadLetterQueue(String name) { return QueueBuilder.durable(name).build(); }

    @Bean Queue inventoryOrdersQueue() { return eventQueue("sivion.inventory.sales-orders", "sivion.inventory.sales-orders.dlq"); }
    @Bean Binding inventoryOrdersConfirmedBinding(Queue inventoryOrdersQueue, TopicExchange sivionEvents) { return BindingBuilder.bind(inventoryOrdersQueue).to(sivionEvents).with("sales.order.confirmed"); }
    @Bean Binding inventoryOrdersCancelledBinding(Queue inventoryOrdersQueue, TopicExchange sivionEvents) { return BindingBuilder.bind(inventoryOrdersQueue).to(sivionEvents).with("sales.order.cancelled"); }

    @Bean Queue salesReservationsQueue() { return eventQueue("sivion.sales.reservations", "sivion.sales.reservations.dlq"); }
    @Bean Binding salesReservationsBinding(Queue salesReservationsQueue, TopicExchange sivionEvents) { return BindingBuilder.bind(salesReservationsQueue).to(sivionEvents).with("inventory.order.reserved"); }

    @Bean Queue salesReservationFailuresQueue() { return eventQueue("sivion.sales.reservation-failures", "sivion.sales.reservation-failures.dlq"); }
    @Bean Binding salesReservationFailuresBinding(Queue salesReservationFailuresQueue, TopicExchange sivionEvents) { return BindingBuilder.bind(salesReservationFailuresQueue).to(sivionEvents).with("inventory.order.reservation-failed"); }

    @Bean Queue financeDispatchQueue() { return eventQueue("sivion.finance.dispatches", "sivion.finance.dispatches.dlq"); }
    @Bean Binding financeDispatchBinding(Queue financeDispatchQueue, TopicExchange sivionEvents) { return BindingBuilder.bind(financeDispatchQueue).to(sivionEvents).with("sales.order.dispatched"); }

    @Bean Queue salesDispatchQueue() { return eventQueue("sivion.sales.dispatches", "sivion.sales.dispatches.dlq"); }
    @Bean Binding salesDispatchBinding(Queue salesDispatchQueue, TopicExchange sivionEvents) { return BindingBuilder.bind(salesDispatchQueue).to(sivionEvents).with("inventory.order.dispatched"); }

    @Bean Queue inventoryGoodsReceivedQueue() { return eventQueue("sivion.inventory.goods-received", "sivion.inventory.goods-received.dlq"); }
    @Bean Binding inventoryGoodsReceivedBinding(Queue inventoryGoodsReceivedQueue, TopicExchange sivionEvents) { return BindingBuilder.bind(inventoryGoodsReceivedQueue).to(sivionEvents).with("procurement.goods-received"); }

    @Bean Queue inventoryOrdersDlq() { return deadLetterQueue("sivion.inventory.sales-orders.dlq"); }
    @Bean Queue salesReservationsDlq() { return deadLetterQueue("sivion.sales.reservations.dlq"); }
    @Bean Queue salesReservationFailuresDlq() { return deadLetterQueue("sivion.sales.reservation-failures.dlq"); }
    @Bean Queue financeDispatchDlq() { return deadLetterQueue("sivion.finance.dispatches.dlq"); }
    @Bean Queue salesDispatchDlq() { return deadLetterQueue("sivion.sales.dispatches.dlq"); }
    @Bean Queue inventoryGoodsReceivedDlq() { return deadLetterQueue("sivion.inventory.goods-received.dlq"); }

    @Bean Binding inventoryOrdersDlqBinding(Queue inventoryOrdersDlq, DirectExchange sivionDeadLetterExchange) { return BindingBuilder.bind(inventoryOrdersDlq).to(sivionDeadLetterExchange).with("sivion.inventory.sales-orders.dlq"); }
    @Bean Binding salesReservationsDlqBinding(Queue salesReservationsDlq, DirectExchange sivionDeadLetterExchange) { return BindingBuilder.bind(salesReservationsDlq).to(sivionDeadLetterExchange).with("sivion.sales.reservations.dlq"); }
    @Bean Binding salesReservationFailuresDlqBinding(Queue salesReservationFailuresDlq, DirectExchange sivionDeadLetterExchange) { return BindingBuilder.bind(salesReservationFailuresDlq).to(sivionDeadLetterExchange).with("sivion.sales.reservation-failures.dlq"); }
    @Bean Binding financeDispatchDlqBinding(Queue financeDispatchDlq, DirectExchange sivionDeadLetterExchange) { return BindingBuilder.bind(financeDispatchDlq).to(sivionDeadLetterExchange).with("sivion.finance.dispatches.dlq"); }
    @Bean Binding salesDispatchDlqBinding(Queue salesDispatchDlq, DirectExchange sivionDeadLetterExchange) { return BindingBuilder.bind(salesDispatchDlq).to(sivionDeadLetterExchange).with("sivion.sales.dispatches.dlq"); }
    @Bean Binding inventoryGoodsReceivedDlqBinding(Queue inventoryGoodsReceivedDlq, DirectExchange sivionDeadLetterExchange) { return BindingBuilder.bind(inventoryGoodsReceivedDlq).to(sivionDeadLetterExchange).with("sivion.inventory.goods-received.dlq"); }
}
