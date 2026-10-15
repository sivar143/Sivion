package com.sivion.api.supplychain;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.sivion.api.supplychain.service.ShipmentService;
import com.sivion.api.supplychain.web.CarrierWebhookController;
import com.sivion.api.supplychain.service.ShipmentService.StatusUpdate;
import java.util.HexFormat;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Test;
import org.springframework.web.server.ResponseStatusException;

class CarrierWebhookControllerTest {
    @Test void refusesWebhooksWhenSecretIsNotConfigured() {
        CarrierWebhookController controller = new CarrierWebhookController(mock(ShipmentService.class), "");
        ResponseStatusException error = assertThrows(ResponseStatusException.class,
                () -> controller.accept(1L, "test-carrier", "sha256=bad", null,
                        "{}".getBytes(StandardCharsets.UTF_8)));
        assertEquals(503, error.getStatusCode().value());
    }

    @Test void refusesInvalidProviderSignature() {
        CarrierWebhookController controller = new CarrierWebhookController(mock(ShipmentService.class), "configured-secret");
        byte[] body = "{\"externalEventId\":\"evt-1\",\"status\":\"IN_TRANSIT\"}".getBytes(StandardCharsets.UTF_8);
        ResponseStatusException error = assertThrows(ResponseStatusException.class,
                () -> controller.accept(1L, "test-carrier", "sha256=wrong", null, body));
        assertEquals(401, error.getStatusCode().value());
    }

    @Test void acceptsAValidProviderSpecificHmacSignatureAndTagsCarrierSource() throws Exception {
        ShipmentService shipments = mock(ShipmentService.class);
        CarrierWebhookController controller = new CarrierWebhookController(shipments, "configured-secret");
        byte[] body = "{\"externalEventId\":\"evt-42\",\"status\":\"IN_TRANSIT\",\"location\":\"Hub A\"}"
            .getBytes(StandardCharsets.UTF_8);
        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(new SecretKeySpec("configured-secret".getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
        String signature = "sha256=" + HexFormat.of().formatHex(mac.doFinal(body));

        controller.accept(7L, "test-carrier", signature, null, body);

        org.mockito.Mockito.verify(shipments).updateStatus(org.mockito.ArgumentMatchers.eq(7L),
            org.mockito.ArgumentMatchers.argThat((StatusUpdate update) ->
                update.idempotencyKey().equals("carrier:test-carrier:evt-42")
                    && update.sourceType().equals("CARRIER:TEST-CARRIER")
                    && update.status().equals("IN_TRANSIT")));
    }

    @Test void acceptsShiprocketApiKeyAndMapsItsTrackingPayload() {
        ShipmentService shipments = mock(ShipmentService.class);
        CarrierWebhookController controller = new CarrierWebhookController(shipments, "shiprocket-secret");
        byte[] body = "{\"awb\":\"AWB123\",\"current_status\":\"IN TRANSIT\",\"current_status_id\":20,\"current_timestamp\":\"15 10 2026 12:00:00\",\"location\":\"Hyderabad\"}"
            .getBytes(StandardCharsets.UTF_8);

        controller.accept(9L, "shiprocket", null, "shiprocket-secret", body);

        org.mockito.Mockito.verify(shipments).updateStatus(org.mockito.ArgumentMatchers.eq(9L),
            org.mockito.ArgumentMatchers.argThat((StatusUpdate update) ->
                update.status().equals("IN_TRANSIT")
                    && update.sourceType().equals("CARRIER:SHIPROCKET")
                    && update.idempotencyKey().startsWith("carrier:shiprocket:AWB123:20")));
    }

    @Test void refusesInvalidProviderName() {
        CarrierWebhookController controller = new CarrierWebhookController(mock(ShipmentService.class), "configured-secret");
        ResponseStatusException error = assertThrows(ResponseStatusException.class,
                () -> controller.accept(1L, "../bad", "sha256=wrong", null, "{}".getBytes(StandardCharsets.UTF_8)));
        assertEquals(400, error.getStatusCode().value());
    }
}
