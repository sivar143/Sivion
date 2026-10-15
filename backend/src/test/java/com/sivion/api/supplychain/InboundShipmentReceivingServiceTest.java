package com.sivion.api.supplychain;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.sivion.api.procurement.domain.GoodsReceipt;
import com.sivion.api.procurement.domain.PurchaseOrder;
import com.sivion.api.procurement.domain.PurchaseOrderItem;
import com.sivion.api.procurement.repo.GoodsReceiptRepository;
import com.sivion.api.procurement.repo.PurchaseOrderItemRepository;
import com.sivion.api.procurement.repo.PurchaseOrderRepository;
import com.sivion.api.supplychain.domain.InboundShipmentReceipt;
import com.sivion.api.supplychain.domain.Shipment;
import com.sivion.api.supplychain.repo.InboundShipmentReceiptRepository;
import com.sivion.api.supplychain.repo.ShipmentRepository;
import com.sivion.api.supplychain.service.InboundShipmentReceivingService;
import com.sivion.api.supplychain.service.ShipmentService;
import com.sivion.api.supplychain.service.TraceabilityService;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.server.ResponseStatusException;

class InboundShipmentReceivingServiceTest {
    private ShipmentRepository shipments;
    private InboundShipmentReceiptRepository receiptLinks;
    private GoodsReceiptRepository goodsReceipts;
    private PurchaseOrderRepository purchaseOrders;
    private PurchaseOrderItemRepository orderItems;
    private TraceabilityService traceability;
    private ShipmentService shipmentService;
    private InboundShipmentReceivingService service;
    private Shipment shipment;

    @BeforeEach void setUp() {
        shipments=mock(ShipmentRepository.class);
        receiptLinks=mock(InboundShipmentReceiptRepository.class);
        goodsReceipts=mock(GoodsReceiptRepository.class);
        purchaseOrders=mock(PurchaseOrderRepository.class);
        orderItems=mock(PurchaseOrderItemRepository.class);
        traceability=mock(TraceabilityService.class);
        shipmentService=mock(ShipmentService.class);
        service=new InboundShipmentReceivingService(shipments,receiptLinks,goodsReceipts,purchaseOrders,
            orderItems,traceability,shipmentService);

        shipment=new Shipment();
        shipment.setDirection("INBOUND");
        shipment.setStatus("IN_TRANSIT");
        shipment.setPurchaseOrderId(22L);
        when(shipments.findByIdAndTenantId(7L,1L)).thenReturn(Optional.of(shipment));
        when(receiptLinks.findByTenantIdAndShipmentIdAndReceiptReference(1L,7L,"REC-1"))
            .thenReturn(Optional.empty());
        PurchaseOrder order=mock(PurchaseOrder.class);
        when(order.getId()).thenReturn(22L);
        when(order.getSupplierId()).thenReturn(8L);
        when(purchaseOrders.findForUpdate(22L,1L)).thenReturn(Optional.of(order));
        PurchaseOrderItem item=new PurchaseOrderItem();
        item.setProductId(31L); item.setWarehouseId(4L); item.setQuantity(new BigDecimal("10"));
        when(orderItems.findByPurchaseOrderId(22L)).thenReturn(List.of(item));
    }

    @Test void rejectsReceiptThatWouldExceedThePurchaseOrderLine() {
        when(goodsReceipts.receivedQuantity(1L,22L,31L,4L)).thenReturn(new BigDecimal("8"));
        var request=new InboundShipmentReceivingService.ReceiptRequest("REC-1","RAW-LOT-1",31L,4L,
            new BigDecimal("3"),"Supplier","SUP-LOT-1",null,null,false);

        ResponseStatusException error=assertThrows(ResponseStatusException.class,()->service.receive(7L,request));

        assertEquals(409,error.getStatusCode().value());
        verify(goodsReceipts,never()).save(any(GoodsReceipt.class));
        verify(traceability,never()).receiveMaterial(any());
    }

    @Test void createsGoodsReceiptAndPendingInspectionLotForPartialReceipt() {
        when(goodsReceipts.receivedQuantity(1L,22L,31L,4L)).thenReturn(new BigDecimal("2"));
        when(goodsReceipts.save(any(GoodsReceipt.class))).thenAnswer(invocation->invocation.getArgument(0));
        when(traceability.receiveMaterial(any())).thenReturn(new TraceabilityService.MaterialLotView(
            55L,"RAW-LOT-1",31L,8L,"Supplier","SUP-LOT-1",7L,22L,"GRN-TEST",4L,
            new BigDecimal("3"),BigDecimal.ZERO,BigDecimal.ZERO,"PENDING_INSPECTION",null,null,null));
        when(receiptLinks.save(any(InboundShipmentReceipt.class))).thenAnswer(invocation->invocation.getArgument(0));

        var request=new InboundShipmentReceivingService.ReceiptRequest("REC-1","RAW-LOT-1",31L,4L,
            new BigDecimal("3"),"Supplier","SUP-LOT-1",null,null,false);
        var result=service.receive(7L,request);

        assertEquals(7L,result.shipmentId());
        assertEquals(31L,result.productId());
        assertEquals(new BigDecimal("3"),result.quantity());
        verify(traceability).receiveMaterial(argThat(lot -> lot.acceptedQuantity().signum()==0
            && lot.rejectedQuantity().signum()==0 && "GRN-".equals(lot.goodsReceiptReference().substring(0,4))));
        verify(shipmentService).updateStatus(eq(7L),argThat(update->"ARRIVED".equals(update.status())));
        verify(shipmentService).updateStatus(eq(7L),argThat(update->"PARTIALLY_RECEIVED".equals(update.status())));
    }
}
