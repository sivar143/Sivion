package com.sivion.api.supplychain.service;

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
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class InboundShipmentReceivingService {
    private static final long TENANT_ID = 1L;
    private final ShipmentRepository shipments;
    private final InboundShipmentReceiptRepository receiptLinks;
    private final GoodsReceiptRepository goodsReceipts;
    private final PurchaseOrderRepository purchaseOrders;
    private final PurchaseOrderItemRepository purchaseOrderItems;
    private final TraceabilityService traceability;
    private final ShipmentService shipmentService;

    public InboundShipmentReceivingService(ShipmentRepository shipments, InboundShipmentReceiptRepository receiptLinks,
            GoodsReceiptRepository goodsReceipts, PurchaseOrderRepository purchaseOrders,
            PurchaseOrderItemRepository purchaseOrderItems, TraceabilityService traceability,
            ShipmentService shipmentService) {
        this.shipments=shipments; this.receiptLinks=receiptLinks; this.goodsReceipts=goodsReceipts;
        this.purchaseOrders=purchaseOrders; this.purchaseOrderItems=purchaseOrderItems;
        this.traceability=traceability; this.shipmentService=shipmentService;
    }

    @Transactional
    public ReceiptView receive(long shipmentId, ReceiptRequest request) {
        if (request == null || blank(request.receiptReference()) || blank(request.lotCode())
                || request.productId()==null || request.warehouseId()==null
                || request.quantity()==null || request.quantity().signum()<=0) {
            throw bad("Receipt reference, material lot code, product, warehouse, and positive quantity are required.");
        }
        String reference=request.receiptReference().trim();
        String lotCode=request.lotCode().trim();
        Optional<InboundShipmentReceipt> previous=receiptLinks.findByTenantIdAndShipmentIdAndReceiptReference(
            TENANT_ID,shipmentId,reference);
        if(previous.isPresent()) {
            InboundShipmentReceipt prior=previous.get();
            if(!Objects.equals(prior.getProductId(),request.productId())
                    || !Objects.equals(prior.getWarehouseId(),request.warehouseId())
                    || prior.getQuantity().compareTo(request.quantity())!=0
                    || !Objects.equals(prior.getLotCode(),lotCode)
                    || prior.isFinalReceipt()!=request.finalReceipt())
                throw new ResponseStatusException(HttpStatus.CONFLICT,"Receipt reference was already used with different receipt details.");
            return view(prior);
        }

        Shipment shipment=shipments.findByIdAndTenantId(shipmentId,TENANT_ID)
            .orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND,"Inbound shipment was not found."));
        if(!"INBOUND".equals(shipment.getDirection()))
            throw bad("Goods receipts can only be recorded against inbound shipments.");
        if("RECEIVED".equals(shipment.getStatus()) || "CANCELLED".equals(shipment.getStatus()))
            throw new ResponseStatusException(HttpStatus.CONFLICT,"This shipment is already closed for receiving.");
        if(shipment.getPurchaseOrderId()==null)
            throw new ResponseStatusException(HttpStatus.CONFLICT,"Link the inbound shipment to an existing purchase order before receiving goods.");

        PurchaseOrder order=purchaseOrders.findForUpdate(shipment.getPurchaseOrderId(),TENANT_ID)
            .orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND,"Linked purchase order was not found."));
        List<PurchaseOrderItem> matchingItems=purchaseOrderItems.findByPurchaseOrderId(order.getId()).stream()
            .filter(item->Objects.equals(item.getProductId(),request.productId())
                && Objects.equals(item.getWarehouseId(),request.warehouseId())).toList();
        if(matchingItems.isEmpty())
            throw bad("The received product and warehouse do not match a line on the linked purchase order.");
        BigDecimal ordered=matchingItems.stream().map(PurchaseOrderItem::getQuantity).reduce(BigDecimal.ZERO,BigDecimal::add);
        BigDecimal alreadyReceived=goodsReceipts.receivedQuantity(TENANT_ID,order.getId(),request.productId(),request.warehouseId());
        if(alreadyReceived.add(request.quantity()).compareTo(ordered)>0)
            throw new ResponseStatusException(HttpStatus.CONFLICT,"This receipt would exceed the remaining purchase-order quantity.");

        String receiptNumber="GRN-"+UUID.randomUUID().toString().replace("-","").substring(0,16).toUpperCase(java.util.Locale.ROOT);
        GoodsReceipt goodsReceipt=new GoodsReceipt();
        goodsReceipt.setTenantId(TENANT_ID);
        goodsReceipt.setReceiptNumber(receiptNumber);
        goodsReceipt.setPurchaseOrderId(order.getId());
        goodsReceipt.setProductId(request.productId());
        goodsReceipt.setWarehouseId(request.warehouseId());
        goodsReceipt.setQuantity(request.quantity());
        goodsReceipt.setStatus("RECEIVED");
        goodsReceipt.setReceivedAt(Instant.now());
        GoodsReceipt savedReceipt=goodsReceipts.save(goodsReceipt);

        // A shipment receipt creates a traceable lot but deliberately does not stock it yet.
        // Usable inventory is added only when accepted quantities pass the quality-inspection workflow.
        TraceabilityService.MaterialLotView lot=traceability.receiveMaterial(new TraceabilityService.MaterialLotRequest(
            lotCode,request.productId(),order.getSupplierId(),request.supplierName(),request.supplierLotCode(),
            shipmentId,order.getId(),savedReceipt.getReceiptNumber(),request.warehouseId(),request.quantity(),
            BigDecimal.ZERO,BigDecimal.ZERO,request.manufacturedAt(),request.expiresAt()));

        InboundShipmentReceipt link=receiptLinks.save(new InboundShipmentReceipt(shipmentId,savedReceipt.getId(),
            savedReceipt.getReceiptNumber(),reference,request.productId(),request.warehouseId(),request.quantity(),
            lot.id(),lot.lotCode(),request.finalReceipt()));

        // Receiving is a physical milestone. Carrier adapters can legitimately skip earlier scan events.
        Shipment refreshed=shipments.findByIdAndTenantId(shipmentId,TENANT_ID).orElseThrow();
        if(!"ARRIVED".equals(refreshed.getStatus()) && !"PARTIALLY_RECEIVED".equals(refreshed.getStatus())) {
            shipmentService.updateStatus(shipmentId,new ShipmentService.StatusUpdate("ARRIVED",Instant.now(),
                null,"Goods receipt recorded: "+savedReceipt.getReceiptNumber(),"inbound-receipt:"+reference+":arrived","CARRIER:INBOUND_RECEIPT"));
        }
        shipmentService.updateStatus(shipmentId,new ShipmentService.StatusUpdate(
            request.finalReceipt()?"RECEIVED":"PARTIALLY_RECEIVED",Instant.now(),null,
            "Goods receipt recorded: "+savedReceipt.getReceiptNumber(),"inbound-receipt:"+reference+":received","SYSTEM"));
        return view(link);
    }

    @Transactional(readOnly=true)
    public List<ReceiptView> list(long shipmentId) {
        if(!shipments.findByIdAndTenantId(shipmentId,TENANT_ID).isPresent())
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,"Shipment was not found.");
        return receiptLinks.findAllByTenantIdAndShipmentIdOrderByRecordedAtAsc(TENANT_ID,shipmentId).stream().map(this::view).toList();
    }

    private ReceiptView view(InboundShipmentReceipt link) {
        return new ReceiptView(link.getId(),link.getShipmentId(),link.getGoodsReceiptId(),link.getGoodsReceiptNumber(),
            link.getReceiptReference(),link.getProductId(),link.getWarehouseId(),link.getQuantity(),link.getMaterialLotId(),
            link.getLotCode(),link.isFinalReceipt(),link.getRecordedAt());
    }
    private boolean blank(String value){return value==null||value.isBlank();}
    private ResponseStatusException bad(String message){return new ResponseStatusException(HttpStatus.BAD_REQUEST,message);}
    public record ReceiptRequest(String receiptReference,String lotCode,Long productId,Long warehouseId,BigDecimal quantity,
        String supplierName,String supplierLotCode,Instant manufacturedAt,Instant expiresAt,boolean finalReceipt){}
    public record ReceiptView(Long id,Long shipmentId,Long goodsReceiptId,String goodsReceiptNumber,String receiptReference,
        Long productId,Long warehouseId,BigDecimal quantity,Long materialLotId,String lotCode,boolean finalReceipt,Instant recordedAt){}
}
