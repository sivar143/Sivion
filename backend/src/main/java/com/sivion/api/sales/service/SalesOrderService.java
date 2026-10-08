package com.sivion.api.sales.service;

import com.sivion.api.integration.DomainEventPublisher;
import com.sivion.api.sales.domain.SalesOrder;
import com.sivion.api.sales.domain.SalesOrderItem;
import com.sivion.api.sales.repo.SalesOrderRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.UUID;

@Service
public class SalesOrderService {

    private static final long TENANT = 1L;

    private static final Set<String> STATUSES = Set.of(
            "DRAFT",
            "CONFIRMED",
            "RESERVATION_FAILED",
            "RESERVED",
            "PARTIALLY_DISPATCHED",
            "DISPATCHED",
            "CANCELLED"
    );

    private final SalesOrderRepository repository;
    private final DomainEventPublisher events;

    public SalesOrderService(
            SalesOrderRepository repository,
            DomainEventPublisher events
    ) {
        this.repository = repository;
        this.events = events;
    }

    public List<SalesOrder> list() {
        return repository.findByTenantIdOrderByOrderDateDesc(TENANT);
    }

    @Transactional
    public SalesOrder create(OrderRequest request) {
        if (request.customerId() == null) {
            throw new IllegalArgumentException("customerId is required");
        }

        if (request.items() == null || request.items().isEmpty()) {
            throw new IllegalArgumentException("At least one order item is required");
        }

        SalesOrder order = new SalesOrder();
        order.setTenantId(TENANT);
        order.setCustomerId(request.customerId());
        order.setOpportunityId(request.opportunityId());
        order.setDeliveryAddress(request.deliveryAddress());
        order.setNotes(request.notes());
        order.setOrderNumber(nextNumber());

        BigDecimal total = BigDecimal.ZERO;

        for (ItemRequest itemRequest : request.items()) {
            validateItem(itemRequest);

            SalesOrderItem item = new SalesOrderItem();
            item.setMaterialId(itemRequest.materialId());
            item.setQuantity(itemRequest.quantity());
            item.setUnitPrice(itemRequest.unitPrice());
            item.setDescription(itemRequest.description());
            item.setLineTotal(
                    itemRequest.quantity().multiply(itemRequest.unitPrice())
            );

            order.addItem(item);
            total = total.add(item.getLineTotal());
        }

        order.setTotalAmount(total);

        return repository.save(order);
    }

    @Transactional
    public SalesOrder updateStatus(Long id, String status) {
        String next = normalizeStatus(status);

        if (!STATUSES.contains(next)) {
            throw new IllegalArgumentException(
                    "Unsupported order status: " + status
            );
        }

        SalesOrder order = repository.findByIdAndTenantId(id, TENANT)
                .orElseThrow(() -> new NoSuchElementException(
                        "Sales order not found"
                ));

        String current = order.getStatus();

        if ("CANCELLED".equals(current) || "DISPATCHED".equals(current)) {
            throw new IllegalStateException(
                    "Order cannot be changed from " + current
            );
        }

        if (Set.of(
                "RESERVED",
                "RESERVATION_FAILED",
                "PARTIALLY_DISPATCHED",
                "DISPATCHED"
        ).contains(next)) {
            throw new IllegalStateException(
                    "This order status is managed by domain events"
            );
        }

        if ("DRAFT".equals(current)
                && !("CONFIRMED".equals(next) || "CANCELLED".equals(next))) {
            throw new IllegalStateException(
                    "Invalid transition from DRAFT to " + next
            );
        }

        if ("CONFIRMED".equals(current) && !"CANCELLED".equals(next)) {
            throw new IllegalStateException(
                    "Confirmed orders are awaiting inventory reservation"
            );
        }

        if ("RESERVATION_FAILED".equals(current)
                && !("CONFIRMED".equals(next) || "CANCELLED".equals(next))) {
            throw new IllegalStateException(
                    "Invalid transition from RESERVATION_FAILED to " + next
            );
        }

        if ("RESERVED".equals(current) && !"CANCELLED".equals(next)) {
            throw new IllegalStateException(
                    "Reserved orders cannot be manually changed"
            );
        }

        order.setStatus(next);

        if ("CONFIRMED".equals(next) && !"CONFIRMED".equals(current)) {
            events.publish(
                    "sales.order.confirmed",
                    Map.of(
                            "salesOrderId", order.getId(),
                            "items", order.getItems().stream()
                                    .map(item -> Map.of(
                                            "productId", item.getMaterialId(),
                                            "quantity", item.getQuantity()
                                    ))
                                    .toList()
                    )
            );
        }

        if ("CANCELLED".equals(next)) {
            events.publish(
                    "sales.order.cancelled",
                    Map.of(
                            "salesOrderId", order.getId(),
                            "cancelled", true
                    )
            );
        }

        return order;
    }

    @Transactional
    public void markReserved(Long id) {
        repository.findByIdAndTenantId(id, TENANT).ifPresent(order -> {
            if ("CONFIRMED".equals(order.getStatus())) {
                order.setStatus("RESERVED");
            }
        });
    }

    @Transactional
    public void markReservationFailed(Long id) {
        repository.findByIdAndTenantId(id, TENANT).ifPresent(order -> {
            if ("CONFIRMED".equals(order.getStatus())) {
                order.setStatus("RESERVATION_FAILED");
            }
        });
    }

    @Transactional
    public void markDispatched(Long id, boolean complete) {
        repository.findByIdAndTenantId(id, TENANT).ifPresent(order -> {
            if ("CANCELLED".equals(order.getStatus())
                    || "DISPATCHED".equals(order.getStatus())) {
                return;
            }

            if (!"RESERVED".equals(order.getStatus())
                    && !"PARTIALLY_DISPATCHED".equals(order.getStatus())) {
                return;
            }

            order.setStatus(
                    complete ? "DISPATCHED" : "PARTIALLY_DISPATCHED"
            );

            if (complete) {
                events.publish(
                        "sales.order.dispatched",
                        Map.of(
                                "salesOrderId", order.getId(),
                                "customerId", order.getCustomerId(),
                                "amount", order.getTotalAmount()
                        )
                );
            }
        });
    }

    private void validateItem(ItemRequest request) {
        if (request.materialId() == null
                || request.quantity() == null
                || request.quantity().signum() <= 0) {
            throw new IllegalArgumentException(
                    "Each item requires a positive materialId and quantity"
            );
        }

        if (request.unitPrice() == null || request.unitPrice().signum() < 0) {
            throw new IllegalArgumentException(
                    "Each item requires a non-negative unitPrice"
            );
        }
    }

    private String normalizeStatus(String status) {
        return status == null
                ? ""
                : status.trim().toUpperCase(Locale.ROOT);
    }

    private String nextNumber() {
        return "SO-"
                + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    public record ItemRequest(
            Long materialId,
            BigDecimal quantity,
            BigDecimal unitPrice,
            String description
    ) {
    }

    public record OrderRequest(
            Long customerId,
            Long opportunityId,
            String deliveryAddress,
            String notes,
            List<ItemRequest> items
    ) {
    }
}
