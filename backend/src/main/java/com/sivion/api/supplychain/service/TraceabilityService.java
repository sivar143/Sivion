package com.sivion.api.supplychain.service;

import com.sivion.api.supplychain.domain.*;
import com.sivion.api.inventory.service.InventoryService;
import com.sivion.api.integration.DomainEventPublisher;
import com.sivion.api.supplychain.repo.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.*;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

/** Lot receipt, manufacturing genealogy and proof-of-delivery use one tenant scope. */
@Service
public class TraceabilityService {
    private static final long TENANT = 1L;
    private final MaterialLotRepository materialLots;
    private final ProductionLotRepository productionLots;
    private final LotConsumptionRepository consumptions;
    private final ShipmentRepository shipments;
    private final ProofOfDeliveryRepository proofs;
    private final ShipmentEventRepository events;
    private final InventoryService inventory;
    private final DomainEventPublisher domainEvents;

    public TraceabilityService(MaterialLotRepository materialLots, ProductionLotRepository productionLots,
            LotConsumptionRepository consumptions, ShipmentRepository shipments,
            ProofOfDeliveryRepository proofs, ShipmentEventRepository events, InventoryService inventory, DomainEventPublisher domainEvents) {
        this.materialLots=materialLots; this.productionLots=productionLots; this.consumptions=consumptions;
        this.shipments=shipments; this.proofs=proofs; this.events=events; this.inventory=inventory; this.domainEvents=domainEvents;
    }

    @Transactional
    public MaterialLotView receiveMaterial(MaterialLotRequest r) {
        if (r.receivedQuantity()==null || r.receivedQuantity().signum()<=0)
            throw bad("Received quantity must be greater than zero.");
        if (r.materialId()==null) throw bad("A material ID is required.");
        if (r.lotCode()==null || r.lotCode().isBlank()) throw bad("A lot code is required.");
        String code=r.lotCode().trim();
        if(materialLots.existsByLotCodeAndTenantId(code,TENANT)) throw conflict("This material lot code already exists.");
        BigDecimal accepted=nz(r.acceptedQuantity()), rejected=nz(r.rejectedQuantity());
        if(accepted.signum()>0 && r.warehouseId()==null)
            throw bad("A warehouse is required when accepted material is added to inventory.");
        if(accepted.signum()<0||rejected.signum()<0||accepted.add(rejected).compareTo(r.receivedQuantity())>0)
            throw bad("Accepted and rejected quantities must be non-negative and cannot exceed received quantity.");
        MaterialLot lot=new MaterialLot();
        lot.setLotCode(code); lot.setMaterialId(r.materialId()); lot.setSupplierId(r.supplierId());
        lot.setSupplierName(trim(r.supplierName())); lot.setSupplierLotCode(trim(r.supplierLotCode()));
        lot.setShipmentId(r.shipmentId()); lot.setPurchaseOrderId(r.purchaseOrderId());
        lot.setGoodsReceiptReference(trim(r.goodsReceiptReference())); lot.setWarehouseId(r.warehouseId());
        lot.setReceivedQuantity(r.receivedQuantity()); lot.setAcceptedQuantity(accepted); lot.setRejectedQuantity(rejected);
        lot.setQualityStatus(rejected.signum()>0 ? (accepted.signum()>0 ? "PARTIALLY_REJECTED" : "REJECTED")
            : accepted.compareTo(r.receivedQuantity())==0 ? "ACCEPTED" : "PENDING_INSPECTION");
        lot.setManufacturedAt(r.manufacturedAt()); lot.setExpiresAt(r.expiresAt());
        MaterialLot saved = materialLots.save(lot);
        // Accepted material enters stock through the existing inventory transaction ledger.
        if (accepted.signum() > 0 && saved.getWarehouseId() != null) {
            inventory.stock(saved.getMaterialId(), saved.getWarehouseId(), accepted,
                "STOCK_IN", "MATERIAL_LOT", saved.getId());
        }
        domainEvents.publish("supply-chain.material-lot.received", Map.of("lotId", saved.getId(), "lotCode", saved.getLotCode(), "materialId", saved.getMaterialId(), "qualityStatus", saved.getQualityStatus(), "tenantId", TENANT));
        return materialView(saved);
    }

    @Transactional
    public ProductionLotView recordProduction(ProductionRequest r) {
        if(r.lotCode()==null||r.lotCode().isBlank()||r.productId()==null||r.manufacturingOrderId()==null)
            throw bad("Lot code, finished product, and manufacturing order are required.");
        if(r.producedQuantity()==null||r.producedQuantity().signum()<=0)
            throw bad("Produced quantity must be greater than zero.");
        BigDecimal rejected=nz(r.rejectedQuantity());
        if(rejected.signum()<0||rejected.compareTo(r.producedQuantity())>0)
            throw bad("Rejected quantity must be between zero and produced quantity.");
        if(productionLots.existsByLotCodeAndTenantId(r.lotCode().trim(),TENANT))
            throw conflict("This production lot code already exists.");
        ProductionLot lot=new ProductionLot(); lot.setLotCode(r.lotCode().trim());
        lot.setProductId(r.productId()); lot.setManufacturingOrderId(r.manufacturingOrderId());
        lot.setProducedQuantity(r.producedQuantity()); lot.setRejectedQuantity(rejected);
        lot.setQualityStatus(rejected.compareTo(r.producedQuantity())==0 ? "REJECTED" :
            rejected.signum()>0 ? "PARTIALLY_REJECTED" : "PENDING_INSPECTION");
        lot.setWarehouseId(r.warehouseId()); lot.setProducedAt(r.producedAt()==null?Instant.now():r.producedAt());
        ProductionLot saved = productionLots.save(lot);
        domainEvents.publish("supply-chain.production-lot.created", Map.of("lotId", saved.getId(), "lotCode", saved.getLotCode(), "manufacturingOrderId", saved.getManufacturingOrderId(), "qualityStatus", saved.getQualityStatus(), "tenantId", TENANT));
        return productionView(saved);
    }

    @Transactional
    public ConsumptionView consume(ConsumptionRequest r) {
        MaterialLot material=materialLots.findByIdAndTenantId(r.materialLotId(),TENANT)
            .orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND,"Material lot was not found."));
        ProductionLot output=productionLots.findByIdAndTenantId(r.productionLotId(),TENANT)
            .orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND,"Production lot was not found."));
        if(!Objects.equals(output.getManufacturingOrderId(),r.manufacturingOrderId()))
            throw bad("Manufacturing order must match the selected finished-product lot.");
        if(!"ACCEPTED".equals(material.getQualityStatus()))
            throw conflict("Only fully accepted raw-material lots can be consumed.");
        if(r.quantityConsumed()==null||r.quantityConsumed().signum()<=0)
            throw bad("Consumed quantity must be greater than zero.");
        BigDecimal already=consumptions.findAllByMaterialLotOrderByRecordedAtAsc(material).stream()
            .map(LotConsumption::getQuantityConsumed).reduce(BigDecimal.ZERO,BigDecimal::add);
        if(already.add(r.quantityConsumed()).compareTo(material.getAcceptedQuantity())>0)
            throw conflict("Consumption exceeds the accepted quantity remaining in this lot.");
        if(r.unit()==null||r.unit().isBlank()) throw bad("Consumption unit is required.");
        LotConsumption edge=consumptions.save(new LotConsumption(material,output,r.manufacturingOrderId(),
            r.quantityConsumed(),r.unit().trim(),trim(r.remarks())));
        domainEvents.publish("supply-chain.material.consumed", Map.of("consumptionId", edge.getId(), "materialLotCode", material.getLotCode(), "productionLotCode", output.getLotCode(), "manufacturingOrderId", edge.getManufacturingOrderId(), "quantity", edge.getQuantityConsumed(), "tenantId", TENANT));
        return new ConsumptionView(edge.getId(),material.getLotCode(),output.getLotCode(),
            edge.getManufacturingOrderId(),edge.getQuantityConsumed(),edge.getUnit(),edge.getRecordedAt(),edge.getRemarks());
    }

    @Transactional(readOnly=true)
    public GenealogyView genealogy(String lotCode) {
        MaterialLot material=materialLots.findByLotCodeAndTenantId(lotCode,TENANT).orElse(null);
        ProductionLot output=productionLots.findByLotCodeAndTenantId(lotCode,TENANT).orElse(null);
        if(material==null&&output==null) throw new ResponseStatusException(HttpStatus.NOT_FOUND,"Lot was not found.");
        List<ConsumptionView> upstream=new ArrayList<>(), downstream=new ArrayList<>();
        if(output!=null) consumptions.findAllByProductionLotOrderByRecordedAtAsc(output).forEach(e->
            upstream.add(new ConsumptionView(e.getId(),e.getMaterialLot().getLotCode(),output.getLotCode(),
                e.getManufacturingOrderId(),e.getQuantityConsumed(),e.getUnit(),e.getRecordedAt(),e.getRemarks())));
        if(material!=null) consumptions.findAllByMaterialLotOrderByRecordedAtAsc(material).forEach(e->
            downstream.add(new ConsumptionView(e.getId(),material.getLotCode(),e.getProductionLot().getLotCode(),
                e.getManufacturingOrderId(),e.getQuantityConsumed(),e.getUnit(),e.getRecordedAt(),e.getRemarks())));
        return new GenealogyView(material==null?null:materialView(material),output==null?null:productionView(output),upstream,downstream);
    }

    @Transactional
    public ProofView recordProof(long shipmentId, ProofRequest r) {
        Shipment shipment=shipments.findByIdAndTenantId(shipmentId,TENANT)
            .orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND,"Shipment was not found."));
        if(!"OUTBOUND".equals(shipment.getDirection())) throw conflict("Proof of delivery applies only to outbound shipments.");
        if(!"DELIVERED".equals(shipment.getStatus())) throw conflict("Mark the shipment DELIVERED before recording proof of delivery.");
        if(r.recipientName()==null||r.recipientName().isBlank()) throw bad("Recipient name is required.");
        if(r.deliveredAt()==null) throw bad("Delivery timestamp is required.");
        if(proofs.findByShipmentId(shipmentId).isPresent()) throw conflict("Proof of delivery has already been recorded.");
        String actor=Optional.ofNullable(SecurityContextHolder.getContext().getAuthentication()).map(Authentication::getName).orElse("system");
        ProofOfDelivery proof=proofs.save(new ProofOfDelivery(shipment,r.recipientName().trim(),r.deliveredAt(),
            trim(r.notes()),trim(r.documentReference()),actor));
        domainEvents.publish("supply-chain.delivery.proof-recorded", Map.of("proofId", proof.getId(), "shipmentId", shipmentId, "recipientName", proof.getRecipientName(), "tenantId", TENANT));
        return new ProofView(proof.getId(),shipmentId,proof.getRecipientName(),proof.getDeliveredAt(),proof.getNotes(),proof.getDocumentReference(),proof.getRecordedBy(),proof.getRecordedAt());
    }

    private MaterialLotView materialView(MaterialLot x){return new MaterialLotView(x.getId(),x.getLotCode(),x.getMaterialId(),x.getSupplierId(),x.getSupplierName(),x.getSupplierLotCode(),x.getShipmentId(),x.getPurchaseOrderId(),x.getGoodsReceiptReference(),x.getWarehouseId(),x.getReceivedQuantity(),x.getAcceptedQuantity(),x.getRejectedQuantity(),x.getQualityStatus(),x.getManufacturedAt(),x.getExpiresAt(),x.getCreatedAt());}
    private ProductionLotView productionView(ProductionLot x){return new ProductionLotView(x.getId(),x.getLotCode(),x.getProductId(),x.getManufacturingOrderId(),x.getShipmentId(),x.getProducedQuantity(),x.getRejectedQuantity(),x.getQualityStatus(),x.getWarehouseId(),x.getProducedAt());}
    private BigDecimal nz(BigDecimal x){return x==null?BigDecimal.ZERO:x;}
    private String trim(String x){return x==null||x.isBlank()?null:x.trim();}
    private ResponseStatusException bad(String m){return new ResponseStatusException(HttpStatus.BAD_REQUEST,m);}
    private ResponseStatusException conflict(String m){return new ResponseStatusException(HttpStatus.CONFLICT,m);}
    public record MaterialLotRequest(String lotCode,Long materialId,Long supplierId,String supplierName,String supplierLotCode,Long shipmentId,Long purchaseOrderId,String goodsReceiptReference,Long warehouseId,BigDecimal receivedQuantity,BigDecimal acceptedQuantity,BigDecimal rejectedQuantity,Instant manufacturedAt,Instant expiresAt){}
    public record ProductionRequest(String lotCode,Long productId,Long manufacturingOrderId,BigDecimal producedQuantity,BigDecimal rejectedQuantity,Long warehouseId,Instant producedAt){}
    public record ConsumptionRequest(Long materialLotId,Long productionLotId,Long manufacturingOrderId,BigDecimal quantityConsumed,String unit,String remarks){}
    public record ProofRequest(String recipientName,Instant deliveredAt,String notes,String documentReference){}
    public record MaterialLotView(Long id,String lotCode,Long materialId,Long supplierId,String supplierName,String supplierLotCode,Long shipmentId,Long purchaseOrderId,String goodsReceiptReference,Long warehouseId,BigDecimal receivedQuantity,BigDecimal acceptedQuantity,BigDecimal rejectedQuantity,String qualityStatus,Instant manufacturedAt,Instant expiresAt,Instant createdAt){}
    public record ProductionLotView(Long id,String lotCode,Long productId,Long manufacturingOrderId,Long shipmentId,BigDecimal producedQuantity,BigDecimal rejectedQuantity,String qualityStatus,Long warehouseId,Instant producedAt){}
    public record ConsumptionView(Long id,String materialLotCode,String productionLotCode,Long manufacturingOrderId,BigDecimal quantityConsumed,String unit,Instant recordedAt,String remarks){}
    public record GenealogyView(MaterialLotView materialLot,ProductionLotView productionLot,List<ConsumptionView> upstream,List<ConsumptionView> downstream){}
    public record ProofView(Long id,Long shipmentId,String recipientName,Instant deliveredAt,String notes,String documentReference,String recordedBy,Instant recordedAt){}
}
