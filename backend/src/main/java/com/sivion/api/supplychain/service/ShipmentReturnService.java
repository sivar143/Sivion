package com.sivion.api.supplychain.service;

import com.sivion.api.inventory.service.InventoryService;
import com.sivion.api.inventory.domain.Dispatch;
import com.sivion.api.inventory.domain.DispatchItem;
import com.sivion.api.inventory.repo.DispatchRepository;
import com.sivion.api.integration.DomainEventPublisher;
import com.sivion.api.supplychain.domain.Shipment;
import com.sivion.api.supplychain.domain.ShipmentReturn;
import com.sivion.api.supplychain.repo.ShipmentRepository;
import com.sivion.api.supplychain.repo.ShipmentReturnRepository;
import java.math.BigDecimal;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class ShipmentReturnService {
    private static final long TENANT_ID = 1L;
    private final ShipmentRepository shipments;
    private final ShipmentReturnRepository returns;
    private final DispatchRepository dispatches;
    private final InventoryService inventory;
    private final DomainEventPublisher events;

    public ShipmentReturnService(ShipmentRepository shipments, ShipmentReturnRepository returns,
            DispatchRepository dispatches, InventoryService inventory, DomainEventPublisher events) {
        this.shipments=shipments; this.returns=returns; this.dispatches=dispatches; this.inventory=inventory; this.events=events;
    }

    @Transactional
    public ReturnView receive(long shipmentId, ReturnRequest request) {
        if (request == null || request.returnReference() == null || request.returnReference().isBlank())
            throw bad("A unique return reference is required.");
        String reference=request.returnReference().trim();
        Optional<ShipmentReturn> previous=returns.findByTenantIdAndReturnReference(TENANT_ID, reference);
        if(previous.isPresent()) {
            ShipmentReturn existing=previous.get();
            String requestedDisposition=request.disposition()==null?"QUARANTINE":request.disposition().trim().toUpperCase(java.util.Locale.ROOT);
            if(!Objects.equals(existing.getShipmentId(), shipmentId)
                    || !Objects.equals(existing.getProductId(), request.productId())
                    || !Objects.equals(existing.getWarehouseId(), request.warehouseId())
                    || !Objects.equals(existing.getQuantity(), request.quantity())
                    || !Objects.equals(existing.getDisposition(), requestedDisposition)
                    || !Objects.equals(existing.getReason(), request.reason()==null?null:request.reason().trim()))
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Return reference was already used with different return details.");
            return view(existing);
        }
        Shipment shipment=shipments.findByIdAndTenantId(shipmentId,TENANT_ID)
            .orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND,"Shipment was not found."));
        if(!"OUTBOUND".equals(shipment.getDirection()) || !"RETURNED".equals(shipment.getStatus()))
            throw new ResponseStatusException(HttpStatus.CONFLICT,"Returns can be received only after an outbound shipment reaches RETURNED status.");
        if(request.productId()==null || request.warehouseId()==null || request.quantity()==null || request.quantity().signum()<=0)
            throw bad("Product, receiving warehouse, and a positive return quantity are required.");
        if (shipment.getDispatchId() == null)
            throw new ResponseStatusException(HttpStatus.CONFLICT, "A linked inventory dispatch is required to reconcile returned quantities.");
        Dispatch dispatch = dispatches.findByIdAndTenantId(shipment.getDispatchId(), TENANT_ID)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Linked inventory dispatch was not found."));
        BigDecimal dispatchedQuantity = dispatch.getItems().stream()
            .filter(item -> Objects.equals(item.getProductId(), request.productId()))
            .map(DispatchItem::getQuantity).reduce(BigDecimal.ZERO, BigDecimal::add);
        if (dispatchedQuantity.signum() <= 0)
            throw bad("The returned product is not part of the linked dispatch.");
        BigDecimal alreadyReturned = returns.findAllByTenantIdAndShipmentId(TENANT_ID, shipmentId).stream()
            .filter(item -> Objects.equals(item.getProductId(), request.productId()))
            .map(ShipmentReturn::getQuantity).reduce(BigDecimal.ZERO, BigDecimal::add);
        if (alreadyReturned.add(request.quantity()).compareTo(dispatchedQuantity) > 0)
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Cumulative returned quantity exceeds the dispatched quantity.");
        String disposition=request.disposition()==null?"QUARANTINE":request.disposition().trim().toUpperCase(java.util.Locale.ROOT);
        if(!java.util.Set.of("RESTOCK","QUARANTINE","SCRAP").contains(disposition))
            throw bad("Disposition must be RESTOCK, QUARANTINE, or SCRAP.");
        if(request.reason()==null || request.reason().isBlank()) throw bad("A return reason is required.");
        String actor=Optional.ofNullable(SecurityContextHolder.getContext().getAuthentication())
            .map(Authentication::getName).orElse("system");
        ShipmentReturn saved=returns.save(new ShipmentReturn(shipmentId,reference,request.productId(),
            request.warehouseId(),request.quantity(),disposition,request.reason().trim(),actor));
        if("RESTOCK".equals(disposition)) {
            inventory.stock(saved.getProductId(),saved.getWarehouseId(),saved.getQuantity(),
                "STOCK_IN","SHIPMENT_RETURN",saved.getId());
        }
        events.publish("supply-chain.return.received", Map.of("returnId",saved.getId(),"shipmentId",shipmentId,
            "returnReference",reference,"disposition",disposition,"quantity",saved.getQuantity(),"tenantId",TENANT_ID));
        return view(saved);
    }

    private ReturnView view(ShipmentReturn r) {
        return new ReturnView(r.getId(),r.getShipmentId(),r.getReturnReference(),r.getProductId(),
            r.getWarehouseId(),r.getQuantity(),r.getDisposition(),r.getReason(),r.getRecordedBy(),r.getRecordedAt());
    }
    private ResponseStatusException bad(String message){return new ResponseStatusException(HttpStatus.BAD_REQUEST,message);}
    public record ReturnRequest(String returnReference,Long productId,Long warehouseId,BigDecimal quantity,String disposition,String reason){}
    public record ReturnView(Long id,Long shipmentId,String returnReference,Long productId,Long warehouseId,
            BigDecimal quantity,String disposition,String reason,String recordedBy,java.time.Instant recordedAt){}
}
