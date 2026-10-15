package com.sivion.api.supplychain.service;

import com.sivion.api.supplychain.domain.Shipment;
import com.sivion.api.supplychain.domain.ShipmentEvent;
import com.sivion.api.supplychain.repo.ShipmentEventRepository;
import com.sivion.api.supplychain.repo.ShipmentRepository;
import com.sivion.api.integration.DomainEventPublisher;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.HexFormat;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class ShipmentService {
    private static final long TENANT_ID = 1L;
    private static final SecureRandom RANDOM = new SecureRandom();

    private static final Map<String, Set<String>> ALLOWED_TRANSITIONS = Map.of(
        "INBOUND", Set.of("CREATED", "READY_FOR_DISPATCH", "DISPATCHED", "IN_TRANSIT",
            "ARRIVED", "PARTIALLY_RECEIVED", "RECEIVED", "INSPECTION_PENDING",
            "ACCEPTED", "REJECTED", "DELAYED", "CANCELLED"),
        "OUTBOUND", Set.of("CREATED", "PACKED", "READY_FOR_DISPATCH", "DISPATCHED",
            "IN_TRANSIT", "OUT_FOR_DELIVERY", "DELIVERED", "PARTIALLY_DELIVERED",
            "DELIVERY_FAILED", "RETURN_IN_TRANSIT", "RETURNED", "DELAYED", "CANCELLED"),
        "TRANSFER", Set.of("CREATED", "READY_FOR_DISPATCH", "DISPATCHED", "IN_TRANSIT",
            "ARRIVED", "RECEIVED", "DELAYED", "CANCELLED")
    );

    private final ShipmentRepository shipments;
    private final ShipmentEventRepository events;
    private final DomainEventPublisher domainEvents;
    private final SupplyChainReferenceValidator referenceValidator;

    public ShipmentService(ShipmentRepository shipments, ShipmentEventRepository events, DomainEventPublisher domainEvents, SupplyChainReferenceValidator referenceValidator) {
        this.shipments = shipments;
        this.events = events;
        this.domainEvents = domainEvents;
        this.referenceValidator = referenceValidator;
    }

    @Transactional(readOnly = true)
    public List<ShipmentView> list() {
        return shipments.findAllByTenantIdOrderByCreatedAtDesc(TENANT_ID)
            .stream().map(this::toView).toList();
    }

    @Transactional(readOnly = true)
    public ShipmentDetail detail(long id) {
        Shipment shipment = findShipment(id);
        return new ShipmentDetail(toView(shipment), events
            .findAllByShipmentOrderByEventTimeAscIdAsc(shipment)
            .stream().map(this::toEventView).toList());
    }

    @Transactional
    public ShipmentDetail create(CreateShipment request) {
        String direction = normalize(request.direction());
        if (!ALLOWED_TRANSITIONS.containsKey(direction)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                "Direction must be INBOUND, OUTBOUND, or TRANSFER.");
        }
        referenceValidator.validateShipment(direction, request.purchaseOrderId(), request.goodsReceiptReference(), request.salesOrderId(), request.dispatchId());
        if (shipments.existsByShipmentNumberAndTenantId(request.shipmentNumber().trim(), TENANT_ID)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                "A shipment with this number already exists.");
        }

        Shipment shipment = new Shipment();
        shipment.setShipmentNumber(request.shipmentNumber().trim());
        shipment.setDirection(direction);
        shipment.setStatus("CREATED");
        shipment.setReferenceNumber(trimToNull(request.referenceNumber()));
        shipment.setPurchaseOrderId(request.purchaseOrderId());
        shipment.setGoodsReceiptReference(trimToNull(request.goodsReceiptReference()));
        shipment.setSalesOrderId(request.salesOrderId());
        shipment.setDispatchId(request.dispatchId());
        shipment.setSupplierName(trimToNull(request.supplierName()));
        shipment.setCustomerName(trimToNull(request.customerName()));
        shipment.setCarrierName(trimToNull(request.carrierName()));
        shipment.setCarrierTrackingNumber(trimToNull(request.carrierTrackingNumber()));
        shipment.setOriginLocation(trimToNull(request.originLocation()));
        shipment.setDestinationLocation(trimToNull(request.destinationLocation()));
        shipment.setDispatchedAt(request.dispatchedAt());
        shipment.setEstimatedArrivalAt(request.estimatedArrivalAt());
        shipment.setNotes(trimToNull(request.notes()));
        shipment.setPublicTrackingToken(newTrackingToken());

        Shipment saved = shipments.save(shipment);
        appendEvent(saved, null, "CREATED", "SHIPMENT_CREATED", Instant.now(),
            null, "Shipment created", "MANUAL", null);
        domainEvents.publish("supply-chain.shipment.created", Map.of("shipmentId", saved.getId(), "shipmentNumber", saved.getShipmentNumber(), "direction", saved.getDirection(), "status", saved.getStatus(), "tenantId", TENANT_ID));
        return detail(saved.getId());
    }

    @Transactional
    public ShipmentDetail updateStatus(long id, StatusUpdate request) {
        Shipment shipment = findShipment(id);
        String next = normalize(request.status());
        if (!ALLOWED_TRANSITIONS.get(shipment.getDirection()).contains(next)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                "Status is not valid for a " + shipment.getDirection().toLowerCase() + " shipment.");
        }

        // Status transitions are deliberately explicit to prevent arbitrary status rewrites.
        if (!isTransitionAllowed(shipment.getDirection(), shipment.getStatus(), next)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                "Cannot change shipment status from " + shipment.getStatus() + " to " + next + ".");
        }
        if (request.idempotencyKey() != null && !request.idempotencyKey().isBlank()
                && events.findByTenantIdAndIdempotencyKey(TENANT_ID, request.idempotencyKey().trim()).isPresent()) {
            return detail(id);
        }

        String previous = shipment.getStatus();
        shipment.setStatus(next);
        if ("DISPATCHED".equals(next) && shipment.getDispatchedAt() == null) {
            shipment.setDispatchedAt(request.eventTime() == null ? Instant.now() : request.eventTime());
        }
        if ("DELIVERED".equals(next) || "RECEIVED".equals(next)) {
            shipment.setDeliveredAt(request.eventTime() == null ? Instant.now() : request.eventTime());
        }
        shipments.save(shipment);
        appendEvent(shipment, previous, next, "STATUS_UPDATE",
            request.eventTime() == null ? Instant.now() : request.eventTime(),
            trimToNull(request.location()), trimToNull(request.remarks()),
            "MANUAL", trimToNull(request.idempotencyKey()));
        domainEvents.publish("supply-chain.shipment.status", Map.of("shipmentId", shipment.getId(), "shipmentNumber", shipment.getShipmentNumber(), "direction", shipment.getDirection(), "previousStatus", previous, "status", next, "tenantId", TENANT_ID));
        return detail(id);
    }

    @Transactional(readOnly = true)
    public PublicTracking publicTracking(String token) {
        Shipment shipment = shipments.findByPublicTrackingToken(token)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Tracking link was not found."));
        // Deliberately exclude supplier/customer private fields, internal notes, and database identifiers.
        List<PublicMilestone> milestones = events.findAllByShipmentOrderByEventTimeAscIdAsc(shipment)
            .stream().map(event -> new PublicMilestone(event.getNewStatus(), event.getEventTime(),
                event.getLocation(), null)).toList();
        return new PublicTracking(shipment.getShipmentNumber(), shipment.getDirection(),
            shipment.getStatus(), shipment.getCarrierName(), shipment.getCarrierTrackingNumber(),
            shipment.getOriginLocation(), shipment.getDestinationLocation(),
            shipment.getEstimatedArrivalAt(), shipment.getDeliveredAt(), milestones);
    }

    private Shipment findShipment(long id) {
        return shipments.findByIdAndTenantId(id, TENANT_ID)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Shipment was not found."));
    }

    private boolean isTransitionAllowed(String direction, String current, String next) {
        if (current.equals(next)) return true;
        if ("CANCELLED".equals(current) || "DELIVERED".equals(current)
                || "ACCEPTED".equals(current) || "REJECTED".equals(current)
                || "RETURNED".equals(current)) return false;
        if ("DELAYED".equals(next)) return !Set.of("CANCELLED", "DELIVERED", "ACCEPTED", "REJECTED", "RETURNED").contains(current);
        if ("CANCELLED".equals(next)) return Set.of("CREATED", "READY_FOR_DISPATCH", "PACKED").contains(current);
        if ("INBOUND".equals(direction)) {
            return switch (current) {
                case "CREATED" -> Set.of("READY_FOR_DISPATCH", "DISPATCHED").contains(next);
                case "READY_FOR_DISPATCH" -> "DISPATCHED".equals(next);
                case "DISPATCHED" -> "IN_TRANSIT".equals(next) || "ARRIVED".equals(next);
                case "IN_TRANSIT" -> "ARRIVED".equals(next);
                case "ARRIVED" -> Set.of("PARTIALLY_RECEIVED", "RECEIVED").contains(next);
                case "PARTIALLY_RECEIVED" -> "RECEIVED".equals(next);
                case "RECEIVED" -> "INSPECTION_PENDING".equals(next) || "ACCEPTED".equals(next) || "REJECTED".equals(next);
                case "INSPECTION_PENDING" -> "ACCEPTED".equals(next) || "REJECTED".equals(next);
                default -> false;
            };
        }
        if ("OUTBOUND".equals(direction)) {
            return switch (current) {
                case "CREATED" -> "PACKED".equals(next) || "READY_FOR_DISPATCH".equals(next);
                case "PACKED" -> "READY_FOR_DISPATCH".equals(next);
                case "READY_FOR_DISPATCH" -> "DISPATCHED".equals(next);
                case "DISPATCHED" -> "IN_TRANSIT".equals(next) || "OUT_FOR_DELIVERY".equals(next);
                case "IN_TRANSIT" -> "OUT_FOR_DELIVERY".equals(next);
                case "OUT_FOR_DELIVERY" -> Set.of("DELIVERED", "PARTIALLY_DELIVERED", "DELIVERY_FAILED").contains(next);
                case "PARTIALLY_DELIVERED" -> Set.of("OUT_FOR_DELIVERY", "DELIVERED", "DELIVERY_FAILED").contains(next);
                case "DELIVERY_FAILED" -> Set.of("IN_TRANSIT", "RETURN_IN_TRANSIT", "OUT_FOR_DELIVERY").contains(next);
                case "RETURN_IN_TRANSIT" -> "RETURNED".equals(next);
                default -> false;
            };
        }
        return switch (current) {
            case "CREATED" -> Set.of("READY_FOR_DISPATCH", "DISPATCHED").contains(next);
            case "READY_FOR_DISPATCH" -> "DISPATCHED".equals(next);
            case "DISPATCHED" -> "IN_TRANSIT".equals(next) || "ARRIVED".equals(next);
            case "IN_TRANSIT" -> "ARRIVED".equals(next);
            case "ARRIVED" -> "RECEIVED".equals(next);
            default -> false;
        };
    }

    private void appendEvent(Shipment shipment, String previous, String next, String type,
            Instant time, String location, String remarks, String source, String idempotencyKey) {
        events.save(new ShipmentEvent(shipment, previous, next, type, time, location, remarks,
            source, actorName(), idempotencyKey));
    }

    private String actorName() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication == null ? "system" : authentication.getName();
    }

    private String newTrackingToken() {
        byte[] bytes = new byte[24];
        RANDOM.nextBytes(bytes);
        return HexFormat.of().formatHex(bytes);
    }

    private String normalize(String value) {
        return value == null ? "" : value.trim().toUpperCase(java.util.Locale.ROOT);
    }

    private String trimToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    private ShipmentView toView(Shipment s) {
        return new ShipmentView(s.getId(), s.getShipmentNumber(), s.getDirection(), s.getStatus(),
            s.getReferenceNumber(), s.getPurchaseOrderId(), s.getGoodsReceiptReference(), s.getSalesOrderId(), s.getDispatchId(), s.getSupplierName(), s.getCustomerName(), s.getCarrierName(),
            s.getCarrierTrackingNumber(), s.getOriginLocation(), s.getDestinationLocation(),
            s.getDispatchedAt(), s.getEstimatedArrivalAt(), s.getDeliveredAt(), s.getNotes(),
            "/api/v1/supply-chain/track/" + s.getPublicTrackingToken(), s.getCreatedAt());
    }

    private EventView toEventView(ShipmentEvent e) {
        return new EventView(e.getId(), e.getPreviousStatus(), e.getNewStatus(), e.getEventType(),
            e.getEventTime(), e.getRecordedAt(), e.getLocation(), e.getRemarks(), e.getSourceType(), e.getActor());
    }

    public record CreateShipment(String shipmentNumber, String direction, String referenceNumber,
        Long purchaseOrderId, String goodsReceiptReference, Long salesOrderId, Long dispatchId,
        String supplierName, String customerName, String carrierName, String carrierTrackingNumber,
        String originLocation, String destinationLocation, Instant dispatchedAt,
        Instant estimatedArrivalAt, String notes) { }
    public record StatusUpdate(String status, Instant eventTime, String location, String remarks,
        String idempotencyKey) { }
    public record ShipmentView(Long id, String shipmentNumber, String direction, String status,
        String referenceNumber, Long purchaseOrderId, String goodsReceiptReference, Long salesOrderId, Long dispatchId, String supplierName, String customerName, String carrierName,
        String carrierTrackingNumber, String originLocation, String destinationLocation,
        Instant dispatchedAt, Instant estimatedArrivalAt, Instant deliveredAt, String notes,
        String trackingPath, Instant createdAt) { }
    public record EventView(Long id, String previousStatus, String newStatus, String eventType,
        Instant eventTime, Instant recordedAt, String location, String remarks, String sourceType, String actor) { }
    public record ShipmentDetail(ShipmentView shipment, List<EventView> events) { }
    public record PublicMilestone(String status, Instant time, String location, String remarks) { }
    public record PublicTracking(String shipmentNumber, String direction, String status, String carrier,
        String carrierTrackingNumber, String origin, String destination, Instant estimatedArrival,
        Instant deliveredAt, List<PublicMilestone> milestones) { }
}
