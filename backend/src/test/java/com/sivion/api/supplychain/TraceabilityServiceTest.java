package com.sivion.api.supplychain;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.sivion.api.supplychain.domain.MaterialLot;
import com.sivion.api.supplychain.domain.ProductionLot;
import com.sivion.api.supplychain.repo.*;
import com.sivion.api.supplychain.service.SupplyChainReferenceValidator;
import com.sivion.api.supplychain.service.TraceabilityService.InspectionRequest;
import com.sivion.api.supplychain.service.TraceabilityService;
import com.sivion.api.inventory.service.InventoryService;
import com.sivion.api.integration.DomainEventPublisher;
import com.sivion.api.supplychain.service.TraceabilityService.MaterialLotRequest;
import com.sivion.api.supplychain.service.TraceabilityService.ProductionRequest;
import java.math.BigDecimal;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.server.ResponseStatusException;

class TraceabilityServiceTest {
    private MaterialLotRepository materials;
    private ProductionLotRepository production;
    private LotConsumptionRepository consumptions;
    private ShipmentRepository shipments;
    private ProofOfDeliveryRepository proofs;
    private ShipmentEventRepository events;
    private QualityInspectionRepository inspections;
    private ManufacturingOrderRepository manufacturingOrders;
    private SupplyChainReferenceValidator referenceValidator;
    private InventoryService inventory;
    private TraceabilityService service;

    @BeforeEach void setUp() {
        materials=mock(MaterialLotRepository.class); production=mock(ProductionLotRepository.class);
        consumptions=mock(LotConsumptionRepository.class); shipments=mock(ShipmentRepository.class);
        proofs=mock(ProofOfDeliveryRepository.class); events=mock(ShipmentEventRepository.class);
        inspections=mock(QualityInspectionRepository.class); manufacturingOrders=mock(ManufacturingOrderRepository.class);
        referenceValidator=mock(SupplyChainReferenceValidator.class); inventory=mock(InventoryService.class);
        service=new TraceabilityService(materials,production,consumptions,shipments,proofs,events,inventory,
            mock(DomainEventPublisher.class),referenceValidator,inspections,manufacturingOrders);
    }

    @Test void rejectsMaterialLotWhenAcceptedAndRejectedExceedReceipt() {
        when(materials.existsByLotCodeAndTenantId("RAW-1",1L)).thenReturn(false);
        MaterialLotRequest request=new MaterialLotRequest("RAW-1",11L,null,"Supplier","SUP-LOT",null,22L,
            "GRN-1",null,new BigDecimal("10"),new BigDecimal("8"),new BigDecimal("3"),null,null);
        assertThrows(ResponseStatusException.class,()->service.receiveMaterial(request));
        verify(materials,never()).save(any(MaterialLot.class));
    }

    @Test void rejectsInspectionQuantitiesAboveTheRemainingUninspectedQuantity() {
        MaterialLot lot=new MaterialLot();
        lot.setLotCode("RAW-2");
        lot.setMaterialId(11L);
        lot.setWarehouseId(5L);
        lot.setReceivedQuantity(new BigDecimal("10"));
        lot.setAcceptedQuantity(new BigDecimal("4"));
        lot.setRejectedQuantity(new BigDecimal("1"));
        lot.setQualityStatus("INSPECTION_IN_PROGRESS");
        when(materials.findByLotCodeAndTenantId("RAW-2",1L)).thenReturn(Optional.of(lot));

        assertThrows(ResponseStatusException.class,()->service.inspect("RAW-2",
            new InspectionRequest(new BigDecimal("4"),new BigDecimal("2"),"over-receipt test")));
        verify(inspections,never()).save(any());
        verify(inventory,never()).stock(any(),any(),any(),any(),any(),any());
    }

    @Test void rejectsProductionWithZeroQuantity() {
        ProductionRequest request=new ProductionRequest("FG-1",21L,31L,BigDecimal.ZERO,BigDecimal.ZERO,null,null);
        assertThrows(ResponseStatusException.class,()->service.recordProduction(request));
        verify(production,never()).save(any(ProductionLot.class));
    }

    @Test void preventsDuplicateMaterialLotCodes() {
        when(materials.existsByLotCodeAndTenantId("RAW-1",1L)).thenReturn(true);
        MaterialLotRequest request=new MaterialLotRequest("RAW-1",11L,null,"Supplier","SUP-LOT",null,22L,
            "GRN-1",null,new BigDecimal("10"),new BigDecimal("10"),BigDecimal.ZERO,null,null);
        assertThrows(ResponseStatusException.class,()->service.receiveMaterial(request));
        verify(materials,never()).save(any(MaterialLot.class));
    }
}
