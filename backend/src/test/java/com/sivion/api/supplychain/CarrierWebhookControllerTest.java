package com.sivion.api.supplychain;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.sivion.api.supplychain.service.ShipmentService;
import com.sivion.api.supplychain.web.CarrierWebhookController;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Test;
import org.springframework.web.server.ResponseStatusException;

class CarrierWebhookControllerTest {
    @Test void refusesWebhooksWhenSecretIsNotConfigured() {
        CarrierWebhookController controller = new CarrierWebhookController(mock(ShipmentService.class), "");
        ResponseStatusException error = assertThrows(ResponseStatusException.class,
                () -> controller.accept(1L, "test-carrier", "sha256=bad",
                        "{}".getBytes(StandardCharsets.UTF_8)));
        assertEquals(503, error.getStatusCode().value());
    }

    @Test void refusesInvalidProviderSignature() {
        CarrierWebhookController controller = new CarrierWebhookController(mock(ShipmentService.class), "configured-secret");
        byte[] body = "{\"externalEventId\":\"evt-1\",\"status\":\"IN_TRANSIT\"}".getBytes(StandardCharsets.UTF_8);
        ResponseStatusException error = assertThrows(ResponseStatusException.class,
                () -> controller.accept(1L, "test-carrier", "sha256=wrong", body));
        assertEquals(401, error.getStatusCode().value());
    }

    @Test void refusesInvalidProviderName() {
        CarrierWebhookController controller = new CarrierWebhookController(mock(ShipmentService.class), "configured-secret");
        ResponseStatusException error = assertThrows(ResponseStatusException.class,
                () -> controller.accept(1L, "../bad", "sha256=wrong", "{}".getBytes(StandardCharsets.UTF_8)));
        assertEquals(400, error.getStatusCode().value());
    }
}
