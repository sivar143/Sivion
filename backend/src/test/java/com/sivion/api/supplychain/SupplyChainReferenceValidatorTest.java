package com.sivion.api.supplychain;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.sivion.api.inventory.repo.DispatchRepository;
import com.sivion.api.procurement.repo.GoodsReceiptRepository;
import com.sivion.api.procurement.repo.PurchaseOrderItemRepository;
import com.sivion.api.procurement.repo.PurchaseOrderRepository;
import com.sivion.api.sales.repo.SalesOrderRepository;
import com.sivion.api.sales.domain.SalesOrder;
import com.sivion.api.supplychain.service.SupplyChainReferenceValidator;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.server.ResponseStatusException;

class SupplyChainReferenceValidatorTest {
    private PurchaseOrderRepository purchaseOrders;
    private GoodsReceiptRepository goodsReceipts;
    private SalesOrderRepository salesOrders;
    private DispatchRepository dispatches;
    private SupplyChainReferenceValidator validator;

    @BeforeEach void setUp() {
        purchaseOrders=mock(PurchaseOrderRepository.class);
        goodsReceipts=mock(GoodsReceiptRepository.class);
        salesOrders=mock(SalesOrderRepository.class);
        dispatches=mock(DispatchRepository.class);
        validator=new SupplyChainReferenceValidator(purchaseOrders, mock(PurchaseOrderItemRepository.class),
            goodsReceipts, salesOrders, dispatches);
    }

    @Test void rejectsPurchaseOrderFromAnotherTenantOrMissingRecord() {
        when(purchaseOrders.findForUpdate(44L,1L)).thenReturn(Optional.empty());
        ResponseStatusException error=assertThrows(ResponseStatusException.class,
            ()->validator.validateShipment("INBOUND",44L,null,null,null));
        assertEquals(404,error.getStatusCode().value());
    }

    @Test void rejectsSalesOrderFromAnotherTenantOrMissingRecord() {
        when(salesOrders.findByIdAndTenantId(91L,1L)).thenReturn(Optional.empty());
        ResponseStatusException error=assertThrows(ResponseStatusException.class,
            ()->validator.validateShipment("OUTBOUND",null,null,91L,null));
        assertEquals(404,error.getStatusCode().value());
    }

    @Test void rejectsInboundShipmentLinkedToSalesOrder() {
        when(salesOrders.findByIdAndTenantId(91L,1L)).thenReturn(Optional.of(new SalesOrder()));
        ResponseStatusException error=assertThrows(ResponseStatusException.class,
            ()->validator.validateShipment("INBOUND",null,null,91L,null));
        assertEquals(400,error.getStatusCode().value());
        verify(salesOrders).findByIdAndTenantId(91L,1L);
    }
}
