package com.sivion.api.supplychain.web;

import com.sivion.api.supplychain.service.ShipmentService;
import com.sivion.api.supplychain.service.ShipmentService.ShipmentDetail;
import com.sivion.api.supplychain.service.ShipmentService.StatusUpdate;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

/**
 * Minimal carrier callback adapter. Configure a high-entropy secret per deployment;
 * carrier-specific HMAC/signature verification should replace the shared-token mode
 * when a carrier integration is selected.
 */
@RestController
@RequestMapping("/api/v1/supply-chain/carrier-webhooks")
public class CarrierWebhookController {
    private final ShipmentService shipments;
    private final String sharedSecret;

    public CarrierWebhookController(ShipmentService shipments,
            @Value("${sivion.supply-chain.webhook-secret:}") String sharedSecret) {
        this.shipments=shipments;
        this.sharedSecret=sharedSecret;
    }

    @PostMapping("/{shipmentId}")
    public ShipmentDetail accept(@PathVariable long shipmentId,
            @RequestHeader(name="X-Sivion-Webhook-Token", required=false) String suppliedToken,
            @RequestBody CarrierEvent event) {
        if(sharedSecret.isBlank()) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,
                "Carrier webhooks are disabled until SIVION_SUPPLY_CHAIN_WEBHOOK_SECRET is configured.");
        }
        if(suppliedToken==null || !MessageDigest.isEqual(
                sharedSecret.getBytes(StandardCharsets.UTF_8),
                suppliedToken.getBytes(StandardCharsets.UTF_8))) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED,"Invalid carrier webhook token.");
        }
        if(event==null || event.status()==null || event.status().isBlank()
                || event.externalEventId()==null || event.externalEventId().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                "Carrier event status and externalEventId are required.");
        }
        return shipments.updateStatus(shipmentId, new StatusUpdate(event.status(),
            event.eventTime()==null?Instant.now():event.eventTime(),event.location(),
            event.remarks(),"carrier:"+event.externalEventId().trim()));
    }

    public record CarrierEvent(String externalEventId,String status,Instant eventTime,String location,String remarks) {}
}
