package com.sivion.api.supplychain.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sivion.api.supplychain.service.ShipmentService;
import com.sivion.api.supplychain.service.ShipmentService.ShipmentDetail;
import com.sivion.api.supplychain.service.ShipmentService.StatusUpdate;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
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
            @RequestBody byte[] rawBody) {
        if (provider == null || !provider.matches("[a-zA-Z0-9_-]{2,40}")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "A valid X-Carrier-Provider header is required.");
        }
        String secret = environment == null ? legacySecret
                : environment.getProperty("sivion.supply-chain.carriers." + provider.toLowerCase(java.util.Locale.ROOT) + ".webhook-secret", "");
        if (secret.isBlank()) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,
                    "This carrier webhook is disabled until its provider-specific secret is configured.");
        }
        if (signature == null || !validSignature(secret, rawBody, signature)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid carrier webhook signature.");
        }
        final CarrierEvent event;
        try {
            event = mapper.readValue(rawBody, CarrierEvent.class);
        } catch (Exception exception) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Carrier webhook body is not valid JSON.");
        }
        if (event == null || event.status() == null || event.status().isBlank()
                || event.externalEventId() == null || event.externalEventId().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Carrier event status and externalEventId are required.");
        }
        return shipments.updateStatus(shipmentId, new StatusUpdate(event.status(),
                event.eventTime() == null ? Instant.now() : event.eventTime(), event.location(),
                event.remarks(), "carrier:" + provider.toLowerCase(java.util.Locale.ROOT) + ":" + event.externalEventId().trim(), "CARRIER:" + provider.toUpperCase(java.util.Locale.ROOT)));
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
