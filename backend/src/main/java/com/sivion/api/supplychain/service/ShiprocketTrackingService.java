package com.sivion.api.supplychain.service;

import com.sivion.api.supplychain.domain.Shipment;
import com.sivion.api.supplychain.repo.ShipmentRepository;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.server.ResponseStatusException;

/** Shiprocket bearer-token authentication and live AWB tracking adapter. */
@Service
public class ShiprocketTrackingService {
    private static final long TENANT_ID = 1L;
    private static final String BASE_URL = "https://apiv2.shiprocket.in/v1/external";
    private final RestClient client;
    private final Environment environment;
    private final ShipmentRepository shipments;
    private final ShipmentService shipmentService;
    private volatile String token;
    private volatile Instant tokenExpiresAt = Instant.EPOCH;

    public ShiprocketTrackingService(RestClient.Builder builder, Environment environment,
            ShipmentRepository shipments, ShipmentService shipmentService) {
        this.client = builder.baseUrl(BASE_URL).build();
        this.environment = environment;
        this.shipments = shipments;
        this.shipmentService = shipmentService;
    }

    public ShipmentService.ShipmentDetail refresh(long shipmentId) {
        Shipment shipment = shipments.findByIdAndTenantId(shipmentId, TENANT_ID)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Shipment was not found."));
        if (shipment.getCarrierName() == null || !shipment.getCarrierName().toLowerCase(Locale.ROOT).contains("shiprocket")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Live API refresh is currently configured for Shiprocket shipments only.");
        }
        String awb = shipment.getCarrierTrackingNumber();
        if (awb == null || awb.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "A Shiprocket AWB / tracking number is required before refresh.");
        }
        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> response = client.get()
                .uri("/courier/track/awb/{awb}", awb.trim())
                .header("Authorization", "Bearer " + token())
                .retrieve().body(Map.class);
            if (response == null) throw new IllegalStateException("Shiprocket returned an empty tracking response.");
            String status = text(find(response, Set.of("current_status", "shipment_status", "status")));
            if (status == null) throw new IllegalStateException("Shiprocket response did not include a tracking status.");
            String timestamp = text(find(response, Set.of("current_timestamp", "event_time", "timestamp")));
            String location = text(find(response, Set.of("location", "current_location", "destination")));
            String remarks = text(find(response, Set.of("activity", "remarks", "message")));
            String eventId = ("api:" + awb.trim() + ":" + status + ":" + (timestamp == null ? "" : timestamp));
            if (eventId.length() > 95) eventId = eventId.substring(0, 95);
            return shipmentService.updateStatus(shipmentId, new ShipmentService.StatusUpdate(
                normalizeStatus(status), parseTime(timestamp), location, remarks,
                "carrier:shiprocket:" + eventId, "CARRIER:SHIPROCKET_API"));
        } catch (ResponseStatusException exception) {
            throw exception;
        } catch (Exception exception) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY,
                "Unable to refresh tracking from Shiprocket. Verify credentials, AWB and provider availability.");
        }
    }

    private synchronized String token() {
        if (token != null && Instant.now().isBefore(tokenExpiresAt)) return token;
        String email = environment.getProperty("sivion.supply-chain.carriers.shiprocket.email", "");
        String password = environment.getProperty("sivion.supply-chain.carriers.shiprocket.password", "");
        if (email.isBlank() || password.isBlank()) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,
                "Configure Shiprocket API credentials before requesting live tracking refresh.");
        }
        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> response = client.post().uri("/auth/login")
                .body(Map.of("email", email, "password", password))
                .retrieve().body(Map.class);
            Object value = response == null ? null : response.get("token");
            if (value == null || value.toString().isBlank()) throw new IllegalStateException("Shiprocket login did not return a token.");
            token = value.toString();
            tokenExpiresAt = Instant.now().plusSeconds(9L * 24 * 60 * 60);
            return token;
        } catch (Exception exception) {
            token = null;
            tokenExpiresAt = Instant.EPOCH;
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY,
                "Unable to authenticate with Shiprocket. Verify the configured API-user credentials.");
        }
    }

    private Object find(Object node, Set<String> names) {
        if (node instanceof Map<?, ?> map) {
            for (Map.Entry<?, ?> entry : map.entrySet()) {
                if (entry.getKey() != null && names.contains(entry.getKey().toString().toLowerCase(Locale.ROOT))) {
                    Object value = entry.getValue();
                    if (value != null && !(value instanceof Map<?, ?>) && !(value instanceof java.util.List<?>)) return value;
                }
            }
            for (Object value : map.values()) {
                Object nested = find(value, names);
                if (nested != null) return nested;
            }
        } else if (node instanceof java.util.List<?> list) {
            for (Object value : list) {
                Object nested = find(value, names);
                if (nested != null) return nested;
            }
        }
        return null;
    }

    private String text(Object value) { return value == null || value.toString().isBlank() ? null : value.toString().trim(); }

    private String normalizeStatus(String status) {
        String normalized = status.trim().toUpperCase(Locale.ROOT).replaceAll("[^A-Z0-9]+", "_");
        return switch (normalized) {
            case "PICKUP_SCHEDULED", "PICKUP_PENDING", "PICKUP_QUEUED" -> "DISPATCHED";
            case "PICKED_UP", "IN_TRANSIT", "SHIPPED", "REACHED_DESTINATION_HUB" -> "IN_TRANSIT";
            case "OUT_FOR_DELIVERY", "OUT_FOR_DELIVERY_ATTEMPT" -> "OUT_FOR_DELIVERY";
            case "DELIVERED", "DELIVERY_COMPLETED" -> "DELIVERED";
            case "UNDELIVERED", "DELIVERY_FAILED", "NDR" -> "DELIVERY_FAILED";
            case "RTO_INITIATED", "RETURN_TO_ORIGIN", "RETURN_IN_TRANSIT" -> "RETURN_IN_TRANSIT";
            case "RTO_DELIVERED", "RETURNED" -> "RETURNED";
            case "CANCELLED", "CANCELED" -> "CANCELLED";
            default -> normalized;
        };
    }

    private Instant parseTime(String timestamp) {
        if (timestamp == null) return Instant.now();
        try { return Instant.parse(timestamp); } catch (Exception ignored) { }
        try { return LocalDateTime.parse(timestamp, DateTimeFormatter.ofPattern("dd MM yyyy HH:mm:ss"))
            .atZone(ZoneId.of("Asia/Kolkata")).toInstant(); } catch (Exception ignored) { return Instant.now(); }
    }
}
