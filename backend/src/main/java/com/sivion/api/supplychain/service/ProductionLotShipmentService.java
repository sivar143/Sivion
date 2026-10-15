package com.sivion.api.supplychain.service;

import com.sivion.api.inventory.domain.Dispatch;
import com.sivion.api.inventory.domain.DispatchItem;
import com.sivion.api.inventory.repo.DispatchRepository;
import com.sivion.api.integration.DomainEventPublisher;
import com.sivion.api.supplychain.domain.ProductionLot;
import com.sivion.api.supplychain.domain.ProductionLotShipment;
import com.sivion.api.supplychain.domain.Shipment;
import com.sivion.api.supplychain.repo.ProductionLotRepository;
import com.sivion.api.supplychain.repo.ProductionLotShipmentRepository;
import com.sivion.api.supplychain.repo.ProofOfDeliveryRepository;
import com.sivion.api.supplychain.repo.QualityInspectionRepository;
import com.sivion.api.supplychain.repo.ShipmentRepository;
import java.math.BigDecimal;
import java.util.Map;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class ProductionLotShipmentService {
    private static final long TENANT_ID = 1L;
    private final ProductionLotRepository productionLots;
    private final ProductionLotShipmentRepository links;
    private final ShipmentRepository shipments;
    private final DispatchRepository dispatches;
    private final QualityInspectionRepository inspections;
    private final DomainEventPublisher events;

    public ProductionLotShipmentService(ProductionLotRepository productionLots,
            ProductionLotShipmentRepository links, ShipmentRepository shipments,
            DispatchRepository dispatches, QualityInspectionRepository inspections,
            DomainEventPublisher events) {
        this.productionLots=productionLots; this.links=links; this.shipments=shipments;
        this.dispatches=dispatches; this.inspections=inspections; this.events=events;
    }

    @Transactional(readOnly = true)
    public List<LinkView> list(String lotCode) {
        ProductionLot lot=productionLots.findByLotCodeAndTenantId(lotCode.trim(),TENANT_ID)
            .orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND,"Finished-product lot was not found."));
        return links.findAllByTenantIdAndProductionLotId(TENANT_ID,lot.getId()).stream()
            .map(link->view(link,lot.getLotCode())).toList();
    }

    @Transactional
    public LinkView allocate(String lotCode, AllocationRequest request) {
        if(request==null || request.shipmentId()==null || request.quantity()==null || request.quantity().signum()<=0)
            throw bad("Shipment ID and a positive shipped quantity are required.");
        ProductionLot lot=productionLots.findByLotCodeAndTenantId(lotCode.trim(),TENANT_ID)
            .orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND,"Finished-product lot was not found."));
        Shipment shipment=shipments.findByIdAndTenantId(request.shipmentId(),TENANT_ID)
            .orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND,"Shipment was not found."));
        if(!"OUTBOUND".equals(shipment.getDirection()))
            throw bad("Finished-product lots can only be allocated to outbound shipments.");
        if(shipment.getDispatchId()==null)
            throw new ResponseStatusException(HttpStatus.CONFLICT,"The outbound shipment must be linked to an inventory dispatch.");
        Dispatch dispatch=dispatches.findByIdAndTenantId(shipment.getDispatchId(),TENANT_ID)
            .orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND,"Linked dispatch was not found."));

        Optional<ProductionLotShipment> prior=links.findByTenantIdAndProductionLotIdAndShipmentId(
            TENANT_ID,lot.getId(),shipment.getId());
        if(prior.isPresent()) {
            if(prior.get().getQuantity().compareTo(request.quantity())!=0)
                throw new ResponseStatusException(HttpStatus.CONFLICT,"This lot is already allocated to the shipment with a different quantity.");
            return view(prior.get(),lot.getLotCode());
        }

        BigDecimal accepted=inspections.findByTenantIdAndLotCodeOrderByInspectedAtAsc(TENANT_ID,lot.getLotCode())
            .stream().filter(i->"FINISHED_PRODUCT".equals(i.getLotType()))
            .map(i->i.getAcceptedQuantity()).reduce(BigDecimal.ZERO,BigDecimal::add);
        if(accepted.signum()<=0) throw new ResponseStatusException(HttpStatus.CONFLICT,
            "The finished-product lot must have accepted quantity from quality inspection before dispatch.");
        BigDecimal alreadyAllocatedForLot=links.findAllByTenantIdAndProductionLotId(TENANT_ID,lot.getId())
            .stream().map(ProductionLotShipment::getQuantity).reduce(BigDecimal.ZERO,BigDecimal::add);
        if(alreadyAllocatedForLot.add(request.quantity()).compareTo(accepted)>0)
            throw new ResponseStatusException(HttpStatus.CONFLICT,"Allocated shipment quantity exceeds the accepted finished-product lot quantity.");

        BigDecimal dispatchQuantity=dispatch.getItems().stream()
            .filter(item->Objects.equals(item.getProductId(),lot.getProductId()))
            .map(DispatchItem::getQuantity).reduce(BigDecimal.ZERO,BigDecimal::add);
        if(dispatchQuantity.signum()<=0) throw bad("Finished product is not part of the linked inventory dispatch.");
        BigDecimal allocatedForDispatch=links.findAllByTenantIdAndShipmentId(TENANT_ID,shipment.getId()).stream()
            .filter(link->productionLots.findByIdAndTenantId(link.getProductionLotId(),TENANT_ID)
                .map(existingLot->Objects.equals(existingLot.getProductId(),lot.getProductId())).orElse(false))
            .map(ProductionLotShipment::getQuantity).reduce(BigDecimal.ZERO,BigDecimal::add);
        if(allocatedForDispatch.add(request.quantity()).compareTo(dispatchQuantity)>0)
            throw new ResponseStatusException(HttpStatus.CONFLICT,"Lot allocations exceed the dispatched quantity for this product.");

        String actor=Optional.ofNullable(SecurityContextHolder.getContext().getAuthentication())
            .map(Authentication::getName).orElse("system");
        ProductionLotShipment saved=links.save(new ProductionLotShipment(lot.getId(),shipment.getId(),request.quantity(),actor));
        events.publish("supply-chain.production-lot.shipped",Map.of("allocationId",saved.getId(),
            "lotCode",lot.getLotCode(),"shipmentId",shipment.getId(),"shipmentNumber",shipment.getShipmentNumber(),
            "quantity",saved.getQuantity(),"tenantId",TENANT_ID));
        return view(saved,lot.getLotCode());
    }

    private LinkView view(ProductionLotShipment link,String lotCode) {
        Shipment shipment=shipments.findByIdAndTenantId(link.getShipmentId(),TENANT_ID).orElseThrow();
        return new LinkView(link.getId(),lotCode,link.getShipmentId(),shipment.getShipmentNumber(),
            link.getQuantity(),link.getCreatedBy(),link.getCreatedAt());
    }
    private ResponseStatusException bad(String message){return new ResponseStatusException(HttpStatus.BAD_REQUEST,message);}
    public record AllocationRequest(Long shipmentId,BigDecimal quantity){}
    public record LinkView(Long id,String lotCode,Long shipmentId,String shipmentNumber,BigDecimal quantity,String createdBy,java.time.Instant createdAt){}
}
