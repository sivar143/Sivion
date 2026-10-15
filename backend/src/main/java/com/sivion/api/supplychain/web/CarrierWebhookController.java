package com.sivion.api.supplychain.web;

import tools.jackson.databind.ObjectMapper;
import com.sivion.api.supplychain.service.ShipmentService;
import com.sivion.api.supplychain.service.ShipmentService.ShipmentDetail;
import com.sivion.api.supplychain.service.ShipmentService.StatusUpdate;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.HexFormat;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

/**
 * Carrier webhook endpoint. Each provider has its own HMAC secret at
 * sivion.supply-chain.carriers.<provider>.webhook-secret. Sign the exact raw
 * request bytes with HMAC-SHA256 and send "sha256=<lowercase hex>".
 */
@RestController
@RequestMapping("/api/v1/supply-chain/carrier-webhooks")
public class CarrierWebhookController {
    private final ShipmentService shipments;
    private final Environment environment;
    private final ObjectMapper mapper;
    private final String legacySecret;

    @Autowired
    public CarrierWebhookController(ShipmentService shipments, Environment environment, ObjectMapper mapper) {
        this.shipments = shipments;
        this.environment = environment;
        this.mapper = mapper;
        this.legacySecret = "";
    }

    /** Compatibility constructor retained for focused controller unit tests. */
    public CarrierWebhookController(ShipmentService shipments, String legacySecret) {
        this.shipments = shipments;
        this.environment = null;
        this.mapper = new ObjectMapper();
        this.legacySecret = legacySecret == null ? "" : legacySecret;
    }

    @PostMapping("/{shipmentId}")
    public ShipmentDetail accept(@PathVariable long shipmentId,
            @RequestHeader(name = "X-Carrier-Provider", required = false) String provider,
            @RequestHeader(name = "X-Carrier-Signature", required = false) String signature,
            @RequestHeader(name = "X-API-Key", required = false) String apiKey,
            @RequestBody byte[] rawBody) {
        return acceptInternal(shipmentId, provider, signature, apiKey, rawBody);
    }

    /** Provider-specific callback URL so Shiprocket need not send a custom provider header. */
    @PostMapping("/shiprocket/{shipmentId}")
    public ShipmentDetail acceptShiprocket(@PathVariable long shipmentId,
            @RequestHeader(name = "X-API-Key", required = false) String apiKey,
            @RequestBody byte[] rawBody) {
        return acceptInternal(shipmentId, "shiprocket", null, apiKey, rawBody);
    }

    private ShipmentDetail acceptInternal(long shipmentId, String provider, String signature,
            String apiKey, byte[] rawBody) {
        if (provider == null || !provider.matches("[a-zA-Z0-9_-]{2,24}")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "A valid X-Carrier-Provider header is required.");
        }
        String normalizedProvider = provider.toLowerCase(java.util.Locale.ROOT);
        String secret = environment == null ? legacySecret
                : environment.getProperty("sivion.supply-chain.carriers." + normalizedProvider + ".webhook-secret", "");
        if (secret.isBlank()) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,
                    "This carrier webhook is disabled until its provider-specific secret is configured.");
        }
        // Shiprocket documents an x-api-key callback token; other configured adapters use HMAC-SHA256.
        if ("shiprocket".equals(normalizedProvider)) {
            if (apiKey == null || !MessageDigest.isEqual(secret.getBytes(StandardCharsets.UTF_8),
                    apiKey.getBytes(StandardCharsets.UTF_8))) {
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid Shiprocket webhook API key.");
            }
        } else if (signature == null || !validSignature(secret, rawBody, signature)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid carrier webhook signature.");
        }

        final CarrierEvent event;
        try {
            if ("shiprocket".equals(normalizedProvider)) {
                @SuppressWarnings("unchecked")
                Map<String, Object> payload = mapper.readValue(rawBody, Map.class);
                String awb = text(payload, "awb", "awb_code", "shipment_id");
                String status = text(payload, "current_status", "shipment_status", "status");
                String statusId = text(payload, "current_status_id", "shipment_status_id");
                String timestamp = text(payload, "current_timestamp", "event_time", "timestamp");
                String location = text(payload, "location", "current_location", "destination");
                String remarks = text(payload, "remarks", "activity", "message");
                String eventId = text(payload, "event_id", "id");
                if (eventId == null) eventId = String.join(":", safe(awb), safe(statusId), safe(timestamp), safe(status));
                event = new CarrierEvent(eventId, normalizeCarrierStatus(status), parseCarrierTime(timestamp), location, remarks);
            } else {
                event = mapper.readValue(rawBody, CarrierEvent.class);
            }
        } catch (ResponseStatusException exception) {
            throw exception;
        } catch (Exception exception) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Carrier webhook body is not valid JSON.");
        }
        if (event == null || event.status() == null || event.status().isBlank()
                || event.externalEventId() == null || event.externalEventId().isBlank()
                || event.externalEventId().trim().length() > 80) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Carrier event status and externalEventId are required.");
        }
        return shipments.updateStatus(shipmentId, new StatusUpdate(event.status(),
                event.eventTime() == null ? Instant.now() : event.eventTime(), event.location(),
                event.remarks(), "carrier:" + normalizedProvider + ":" + event.externalEventId().trim(),
                "CARRIER:" + normalizedProvider.toUpperCase(java.util.Locale.ROOT)));
    }

    private String text(Map<String, Object> payload, String... keys) {
        for (String key : keys) {
            Object value = payload.get(key);
            if (value != null && !value.toString().isBlank()) return value.toString().trim();
        }
        return null;
    }

    private String safe(String value) { return value == null ? "" : value; }

    private String normalizeCarrierStatus(String status) {
        if (status == null) return null;
        String normalized = status.trim().toUpperCase(java.util.Locale.ROOT).replaceAll("[^A-Z0-9]+", "_");
        while (normalized.startsWith("_")) normalized = normalized.substring(1);
        while (normalized.endsWith("_")) normalized = normalized.substring(0, normalized.length() - 1);
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

    private Instant parseCarrierTime(String timestamp) {
        if (timestamp == null || timestamp.isBlank()) return null;
        try { return Instant.parse(timestamp); } catch (Exception ignored) { }
        try {
            return LocalDateTime.parse(timestamp, DateTimeFormatter.ofPattern("dd MM yyyy HH:mm:ss"))
                .atZone(ZoneId.of("Asia/Kolkata")).toInstant();
        } catch (Exception ignored) { return null; }
    }

    private boolean validSignature(String secret, byte[] rawBody, String supplied) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            String expected = "sha256=" + HexFormat.of().formatHex(mac.doFinal(rawBody));
            return MessageDigest.isEqual(expected.getBytes(StandardCharsets.US_ASCII),
                    supplied.trim().getBytes(StandardCharsets.US_ASCII));
        } catch (Exception exception) {
            throw new IllegalStateException("HMAC-SHA256 is unavailable.", exception);
        }
    }

    public record CarrierEvent(String externalEventId, String status, Instant eventTime, String location, String remarks) { }
}
