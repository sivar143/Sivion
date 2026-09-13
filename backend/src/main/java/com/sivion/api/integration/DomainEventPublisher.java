package com.sivion.api.integration;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.Map;

@Component
public class DomainEventPublisher {
    private final RabbitTemplate rabbit;

    public DomainEventPublisher(RabbitTemplate rabbit) {
        this.rabbit = rabbit;
    }

    public void publish(String routingKey, Map<String, Object> payload) {
        if (TransactionSynchronizationManager.isSynchronizationActive()
                && TransactionSynchronizationManager.isActualTransactionActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    rabbit.convertAndSend(RabbitTopology.EXCHANGE, routingKey, payload);
                }
            });
            return;
        }
        rabbit.convertAndSend(RabbitTopology.EXCHANGE, routingKey, payload);
    }
}
