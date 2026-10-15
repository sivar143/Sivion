package com.sivion.api.supplychain;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.sivion.api.inventory.domain.Dispatch;
import com.sivion.api.inventory.repo.DispatchRepository;
import com.sivion.api.integration.DomainEventPublisher;
import com.sivion.api.supplychain.domain.ProductionLot;
import com.sivion.api.supplychain.domain.Shipment;
import com.sivion.api.supplychain.repo.*;
import com.sivion.api.supplychain.service.ProductionLotShipmentService;
import java.math.BigDecimal;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.web.server.ResponseStatusException;

class ProductionLotShipmentServiceTest {
    @Test void refusesToAllocateFinishedLotsBeforeQualityAcceptance() {
        ProductionLotRepository productionLots=mock(ProductionLotRepository.class);
        ProductionLotShipmentRepository links=mock(ProductionLotShipmentRepository.class);
        ShipmentRepository shipments=mock(ShipmentRepository.class);
        DispatchRepository dispatches=mock(DispatchRepository.class);
        QualityInspectionRepository inspections=mock(QualityInspectionRepository.class);
        ProductionLot lot=new ProductionLot();
        lot.setLotCode("FG-LOT-1");
        lot.setProductId(10L);
        lot.setManufacturingOrderId(20L);
        lot.setProducedQuantity(new BigDecimal("10"));
        lot.setRejectedQuantity(BigDecimal.ZERO);
        Shipment shipment=new Shipment();
        shipment.setDirection("OUTBOUND");
        shipment.setDispatchId(30L);
        when(productionLots.findByLotCodeAndTenantId("FG-LOT-1",1L)).thenReturn(Optional.of(lot));
        when(shipments.findByIdAndTenantId(40L,1L)).thenReturn(Optional.of(shipment));
        when(dispatches.findByIdAndTenantId(30L,1L)).thenReturn(Optional.of(new Dispatch()));
        when(links.findByTenantIdAndProductionLotIdAndShipmentId(1L,null,40L)).thenReturn(Optional.empty());
        when(inspections.findByTenantIdAndLotCodeOrderByInspectedAtAsc(1L,"FG-LOT-1")).thenReturn(java.util.List.of());

        ProductionLotShipmentService service=new ProductionLotShipmentService(productionLots,links,shipments,
            dispatches,inspections,mock(DomainEventPublisher.class));
        ResponseStatusException error=assertThrows(ResponseStatusException.class,()->service.allocate(
            "FG-LOT-1",new ProductionLotShipmentService.AllocationRequest(40L,new BigDecimal("2"))));
        assertEquals(409,error.getStatusCode().value());
        verify(links,never()).save(any());
    }
}
