package com.sivion.api.notifications;

import com.sivion.api.hr.domain.Employee;
import com.sivion.api.hr.repository.EmployeeRepository;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

/**
 * Converts existing business integration events into inbox notifications.
 * Uses a dedicated RabbitMQ queue so it cannot compete with inventory or sales consumers.
 */
@Component
public class DomainNotificationListener {
    private static final Logger LOGGER = LoggerFactory.getLogger(DomainNotificationListener.class);
    private static final Long TENANT_ID = 1L;

    private final EmployeeRepository employees;
    private final NotificationService notifications;

    public DomainNotificationListener(EmployeeRepository employees, NotificationService notifications) {
        this.employees = employees;
        this.notifications = notifications;
    }

    @RabbitListener(queues = "sivion.notifications.domain-events")
    public void onDomainEvent(
        @Header(AmqpHeaders.RECEIVED_ROUTING_KEY) String routingKey,
        @Payload Map<String, Object> payload
    ) {
        Set<String> recipientRoles = recipientRoles(routingKey);
        if (recipientRoles.isEmpty()) return;

        String reference = referenceFor(routingKey, payload);
        String title = titleFor(routingKey);
        String message = messageFor(routingKey, reference);
        String eventKey = eventKeyFor(routingKey, payload);

        List<Employee> recipients = employees.findByTenantIdOrderByLastNameAscFirstNameAsc(TENANT_ID).stream()
            .filter(employee -> employee.getUsername() != null && !employee.getUsername().isBlank())
            .filter(employee -> recipientRoles.contains(employee.getRole()))
            .toList();

        for (Employee recipient : recipients) {
            try {
                notifications.sendOnce(TENANT_ID, eventKey, recipient.getUsername(), title, message);
            } catch (RuntimeException exception) {
                // Do not discard the rest of the recipient fan-out because one inbox write failed.
                LOGGER.warn("Unable to create event notification for recipient {}", recipient.getUsername(), exception);
                throw exception; // Let RabbitMQ retry and dead-letter the event rather than silently lose it.
            }
        }
    }

    private Set<String> recipientRoles(String routingKey) {
        return switch (routingKey) {
            case "sales.order.confirmed", "sales.order.cancelled" ->
                Set.of("INVENTORY_MANAGER", "INVENTORY_USER", "WAREHOUSE_MANAGER", "WAREHOUSE_USER");
            case "inventory.order.reserved", "inventory.order.reservation-failed" ->
                Set.of("SALES_MANAGER", "SALES_USER");
            case "inventory.order.dispatched" ->
                Set.of("SALES_MANAGER", "SALES_USER", "FINANCE_MANAGER", "FINANCE_USER");
            case "procurement.goods-received" ->
                Set.of("INVENTORY_MANAGER", "INVENTORY_USER", "WAREHOUSE_MANAGER", "WAREHOUSE_USER",
                    "FINANCE_MANAGER", "FINANCE_USER");
            case "supply-chain.shipment.created", "supply-chain.shipment.status" ->
                Set.of("PROCUREMENT_MANAGER", "WAREHOUSE_MANAGER", "WAREHOUSE_USER", "INVENTORY_MANAGER",
                    "INVENTORY_USER", "MANUFACTURING_MANAGER", "SALES_MANAGER", "SALES_USER");
            case "supply-chain.material-lot.received", "supply-chain.quality-inspection.completed",
                 "supply-chain.production-lot.created", "supply-chain.material.consumed" ->
                Set.of("PROCUREMENT_MANAGER", "WAREHOUSE_MANAGER", "INVENTORY_MANAGER", "INVENTORY_USER",
                    "MANUFACTURING_MANAGER", "MANUFACTURING_USER", "QUALITY_MANAGER");
            case "supply-chain.delivery.proof-recorded", "supply-chain.return.received" ->
                Set.of("WAREHOUSE_MANAGER", "INVENTORY_MANAGER", "SALES_MANAGER", "SALES_USER");
            default -> Set.of();
        };
    }

    private String eventKeyFor(String routingKey, Map<String, Object> payload) {
        Object eventId = payload.get("eventId");
        if (eventId != null) return routingKey + ":" + eventId;
        for (String key : List.of("inspectionId", "consumptionId", "proofId", "returnId", "lotId", "shipmentId", "dispatchId", "goodsReceiptId", "salesOrderId", "purchaseOrderId")) {
            Object value = payload.get(key);
            if (value != null) return routingKey + ":" + key + ":" + value;
        }
        throw new IllegalArgumentException("Cannot derive a stable event key for " + routingKey);
    }

    private String titleFor(String routingKey) {
        return switch (routingKey) {
            case "sales.order.confirmed" -> "Sales order confirmed";
            case "sales.order.cancelled" -> "Sales order cancelled";
            case "inventory.order.reserved" -> "Inventory reserved";
            case "inventory.order.reservation-failed" -> "Inventory reservation failed";
            case "inventory.order.dispatched" -> "Inventory dispatch completed";
            case "procurement.goods-received" -> "Goods received";
            case "supply-chain.shipment.created" -> "Shipment created";
            case "supply-chain.shipment.status" -> "Shipment status updated";
            case "supply-chain.material-lot.received" -> "Raw material lot received";
            case "supply-chain.quality-inspection.completed" -> "Quality inspection completed";
            case "supply-chain.production-lot.created" -> "Production lot recorded";
            case "supply-chain.material.consumed" -> "Material consumed in production";
            case "supply-chain.delivery.proof-recorded" -> "Proof of delivery recorded";
            case "supply-chain.return.received" -> "Shipment return received";
            default -> "Business update";
        };
    }

    private String referenceFor(String routingKey, Map<String, Object> payload) {
        for (String key : List.of("shipmentNumber", "lotCode", "returnReference", "salesOrderId", "purchaseOrderId", "dispatchId", "goodsReceiptId")) {
            Object value = payload.get(key);
            if (value != null) return key + " #" + value;
        }
        return routingKey;
    }

    private String messageFor(String routingKey, String reference) {
        return switch (routingKey) {
            case "sales.order.confirmed" -> reference + " is confirmed and ready for inventory processing.";
            case "sales.order.cancelled" -> reference + " was cancelled; review any related stock reservations.";
            case "inventory.order.reserved" -> reference + " has a successful inventory reservation.";
            case "inventory.order.reservation-failed" -> reference + " could not be reserved in inventory.";
            case "inventory.order.dispatched" -> reference + " has a dispatch update.";
            case "procurement.goods-received" -> reference + " was recorded as received.";
            case "supply-chain.shipment.created" -> reference + " was created and is ready for tracking.";
            case "supply-chain.shipment.status" -> reference + " has a new shipment milestone.";
            case "supply-chain.material-lot.received" -> reference + " was received for inspection and traceability.";
            case "supply-chain.quality-inspection.completed" -> reference + " has an updated inspection result.";
            case "supply-chain.production-lot.created" -> reference + " was recorded against a manufacturing order.";
            case "supply-chain.material.consumed" -> reference + " was linked to a finished-product lot.";
            case "supply-chain.delivery.proof-recorded" -> reference + " has proof of delivery recorded.";
            case "supply-chain.return.received" -> reference + " was recorded in the returns workflow.";
            default -> "A business workflow was updated.";
        };
    }
}
