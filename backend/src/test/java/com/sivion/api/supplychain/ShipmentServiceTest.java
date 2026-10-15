package com.sivion.api.supplychain;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.sivion.api.integration.DomainEventPublisher;
import com.sivion.api.supplychain.domain.Shipment;
import com.sivion.api.supplychain.domain.ShipmentEvent;
import com.sivion.api.supplychain.repo.ShipmentEventRepository;
import com.sivion.api.supplychain.repo.ShipmentRepository;
import com.sivion.api.supplychain.service.ShipmentService;
import com.sivion.api.supplychain.service.SupplyChainReferenceValidator;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class ShipmentServiceTest {
    @Test
    void publicTrackingRedactsInternalMilestoneRemarksAndPrivateShipmentFields() {
        ShipmentRepository shipments = mock(ShipmentRepository.class);
        ShipmentEventRepository events = mock(ShipmentEventRepository.class);
        ShipmentService service = new ShipmentService(shipments, events,
            mock(DomainEventPublisher.class), mock(SupplyChainReferenceValidator.class));

        Shipment shipment = new Shipment();
        shipment.setShipmentNumber("OUT-100");
        shipment.setDirection("OUTBOUND");
        shipment.setStatus("IN_TRANSIT");
        shipment.setPublicTrackingToken("012345678901234567890123456789012345678901234567");
        shipment.setCarrierName("Shiprocket");
        shipment.setCarrierTrackingNumber("AWB-123");
        shipment.setCustomerName("Private Customer Name");
        shipment.setNotes("Internal warehouse instructions");
        shipment.setOriginLocation("Hyderabad");
        shipment.setDestinationLocation("Bengaluru");
        ShipmentEvent event = new ShipmentEvent(shipment, "DISPATCHED", "IN_TRANSIT",
            "STATUS_UPDATE", Instant.parse("2026-10-15T08:00:00Z"), "Transit Hub",
            "Internal note: customer requested a special delivery window",
            "CARRIER:SHIPROCKET", "internal-user", "carrier:shiprocket:event-1");

        when(shipments.findByPublicTrackingToken(shipment.getPublicTrackingToken())).thenReturn(Optional.of(shipment));
        when(events.findAllByShipmentOrderByEventTimeAscIdAsc(shipment)).thenReturn(List.of(event));

        ShipmentService.PublicTracking result = service.publicTracking(shipment.getPublicTrackingToken());

        assertEquals("OUT-100", result.shipmentNumber());
        assertEquals("IN_TRANSIT", result.status());
        assertEquals("AWB-123", result.carrierTrackingNumber());
        assertEquals(1, result.milestones().size());
        assertNull(result.milestones().get(0).remarks());
        assertFalse(result.toString().contains("Private Customer Name"));
        assertFalse(result.toString().contains("Internal warehouse instructions"));
        assertFalse(result.toString().contains("special delivery window"));
    }
}
