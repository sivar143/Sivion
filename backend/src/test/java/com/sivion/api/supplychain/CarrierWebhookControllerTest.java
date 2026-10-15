package com.sivion.api.supplychain;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.sivion.api.supplychain.service.ShipmentService;
import com.sivion.api.supplychain.web.CarrierWebhookController;
import org.junit.jupiter.api.Test;
import org.springframework.web.server.ResponseStatusException;

class CarrierWebhookControllerTest {
    @Test void refusesWebhooksWhenSecretIsNotConfigured() {
        CarrierWebhookController controller=new CarrierWebhookController(mock(ShipmentService.class),"");
        ResponseStatusException error=assertThrows(ResponseStatusException.class,()->controller.accept(1L,"anything",
            new CarrierWebhookController.CarrierEvent("evt-1","IN_TRANSIT",null,null,null)));
        assertEquals(503,error.getStatusCode().value());
    }

    @Test void refusesInvalidWebhookToken() {
        CarrierWebhookController controller=new CarrierWebhookController(mock(ShipmentService.class),"configured-secret");
        ResponseStatusException error=assertThrows(ResponseStatusException.class,()->controller.accept(1L,"wrong",
            new CarrierWebhookController.CarrierEvent("evt-1","IN_TRANSIT",null,null,null)));
        assertEquals(401,error.getStatusCode().value());
    }
}
