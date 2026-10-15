package com.sivion.api.supplychain.service;

import com.sivion.api.inventory.domain.Dispatch;
import com.sivion.api.inventory.repo.DispatchRepository;
import com.sivion.api.procurement.domain.GoodsReceipt;
import com.sivion.api.procurement.domain.PurchaseOrder;
import com.sivion.api.procurement.repo.GoodsReceiptRepository;
import com.sivion.api.procurement.repo.PurchaseOrderItemRepository;
import com.sivion.api.procurement.repo.PurchaseOrderRepository;
import com.sivion.api.sales.domain.SalesOrder;
import com.sivion.api.sales.repo.SalesOrderRepository;
import java.util.Objects;
import java.math.BigDecimal;
import com.sivion.api.supplychain.repo.MaterialLotRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

/** Validates supply-chain links against the owning tenant's actual business records. */
@Service
public class SupplyChainReferenceValidator {
    private static final long TENANT_ID = 1L;
    private final PurchaseOrderRepository purchaseOrders;
    private final PurchaseOrderItemRepository purchaseOrderItems;
    private final GoodsReceiptRepository goodsReceipts;
    private final SalesOrderRepository salesOrders;
    private final DispatchRepository dispatches;
    private final MaterialLotRepository materialLots;

    @Autowired
    public SupplyChainReferenceValidator(PurchaseOrderRepository purchaseOrders,
            PurchaseOrderItemRepository purchaseOrderItems, GoodsReceiptRepository goodsReceipts,
            SalesOrderRepository salesOrders, DispatchRepository dispatches, MaterialLotRepository materialLots) {
        this.purchaseOrders = purchaseOrders;
        this.purchaseOrderItems = purchaseOrderItems;
        this.goodsReceipts = goodsReceipts;
        this.salesOrders = salesOrders;
        this.dispatches = dispatches;
        this.materialLots = materialLots;
    }

    /** Compatibility constructor for focused validation tests without lot reconciliation. */
    public SupplyChainReferenceValidator(PurchaseOrderRepository purchaseOrders,
            PurchaseOrderItemRepository purchaseOrderItems, GoodsReceiptRepository goodsReceipts,
            SalesOrderRepository salesOrders, DispatchRepository dispatches) {
        this(purchaseOrders, purchaseOrderItems, goodsReceipts, salesOrders, dispatches, null);
    }

    public ResolvedReferences validateShipment(String direction, Long purchaseOrderId, String goodsReceiptReference,
            Long salesOrderId, Long dispatchId) {
        PurchaseOrder order = null;
        if (purchaseOrderId != null) {
            order = purchaseOrders.findForUpdate(purchaseOrderId, TENANT_ID)
                    .orElseThrow(() -> notFound("Purchase order"));
            if ("CANCELLED".equalsIgnoreCase(order.getStatus())) {
                throw conflict("A cancelled purchase order cannot be linked to a shipment.");
            }
        }
        GoodsReceipt receipt = null;
        if (goodsReceiptReference != null && !goodsReceiptReference.isBlank()) {
            receipt = findGoodsReceipt(goodsReceiptReference.trim());
            if (receipt == null) throw notFound("Goods receipt");
            if (order != null && !Objects.equals(receipt.getPurchaseOrderId(), order.getId())) {
                throw bad("The goods receipt belongs to a different purchase order.");
            }
            if (order == null) {
                order = purchaseOrders.findForUpdate(receipt.getPurchaseOrderId(), TENANT_ID)
                        .orElseThrow(() -> notFound("Purchase order for goods receipt"));
            }
        }
        SalesOrder salesOrder = null;
        if (salesOrderId != null) {
            salesOrder = salesOrders.findByIdAndTenantId(salesOrderId, TENANT_ID)
                    .orElseThrow(() -> notFound("Sales order"));
            if ("CANCELLED".equalsIgnoreCase(salesOrder.getStatus())) {
                throw conflict("A cancelled sales order cannot be linked to a shipment.");
            }
        }
        if (dispatchId != null) {
            Dispatch dispatch = dispatches.findByIdAndTenantId(dispatchId, TENANT_ID)
                    .orElseThrow(() -> notFound("Dispatch"));
            if (salesOrder != null && dispatch.getSalesOrderId() != null
                    && !Objects.equals(dispatch.getSalesOrderId(), salesOrder.getId())) {
                throw bad("The dispatch belongs to a different sales order.");
            }
            if (salesOrder == null && dispatch.getSalesOrderId() != null) {
                salesOrder = salesOrders.findByIdAndTenantId(dispatch.getSalesOrderId(), TENANT_ID)
                        .orElseThrow(() -> notFound("Sales order for dispatch"));
            }
        }
        if ("INBOUND".equalsIgnoreCase(direction) && (salesOrderId != null || dispatchId != null)) {
            throw bad("Inbound shipments cannot be linked to outbound sales orders or dispatches.");
        }
        if ("OUTBOUND".equalsIgnoreCase(direction) && (purchaseOrderId != null || goodsReceiptReference != null)) {
            throw bad("Outbound shipments cannot be linked to inbound purchase orders or goods receipts.");
        }
        return new ResolvedReferences(order == null ? null : order.getId(),
                receipt == null ? goodsReceiptReference : receipt.getReceiptNumber(),
                salesOrder == null ? null : salesOrder.getId(), dispatchId);
    }

    public record ResolvedReferences(Long purchaseOrderId, String goodsReceiptReference,
            Long salesOrderId, Long dispatchId) {}

    public Dispatch findDispatchForTenant(Long dispatchId) {
        return dispatches.findByIdAndTenantId(dispatchId, TENANT_ID)
                .orElseThrow(() -> notFound("Dispatch"));
    }

    public GoodsReceipt validateMaterialReceipt(Long purchaseOrderId, String goodsReceiptReference,
            Long materialId, Long supplierId, Long warehouseId) {
        return validateMaterialReceipt(purchaseOrderId, goodsReceiptReference, materialId, supplierId, warehouseId, null);
    }

    public GoodsReceipt validateMaterialReceipt(Long purchaseOrderId, String goodsReceiptReference,
            Long materialId, Long supplierId, Long warehouseId, BigDecimal receivedQuantity) {
        GoodsReceipt receipt = goodsReceiptReference == null || goodsReceiptReference.isBlank()
                ? null : findGoodsReceipt(goodsReceiptReference.trim());
        PurchaseOrder order = null;
        Long effectiveOrderId = purchaseOrderId;
        if (receipt != null) {
            if (effectiveOrderId != null && !Objects.equals(effectiveOrderId, receipt.getPurchaseOrderId())) {
                throw bad("The goods receipt belongs to a different purchase order.");
            }
            effectiveOrderId = receipt.getPurchaseOrderId();
        } else if (goodsReceiptReference != null && !goodsReceiptReference.isBlank()) {
            throw notFound("Goods receipt");
        }
        if (effectiveOrderId != null && receipt == null) {
            throw bad("A goods receipt must exist before a purchase-order material lot can be accepted into traceable inventory.");
        }
        if (effectiveOrderId != null) {
            order = purchaseOrders.findForUpdate(effectiveOrderId, TENANT_ID)
                    .orElseThrow(() -> notFound("Purchase order"));
            if (supplierId != null && !Objects.equals(supplierId, order.getSupplierId())) {
                throw bad("Supplier does not match the linked purchase order.");
            }
            boolean itemMatches = purchaseOrderItems.findByPurchaseOrderId(order.getId()).stream()
                    .anyMatch(item -> Objects.equals(item.getProductId(), materialId)
                            && (warehouseId == null || Objects.equals(item.getWarehouseId(), warehouseId)));
            if (!itemMatches) throw bad("The material and warehouse do not match any line on the linked purchase order.");
        }
        if (receipt != null) {
            if (receivedQuantity != null) {
                BigDecimal alreadyAssigned = materialLots.findAllByGoodsReceiptReferenceAndTenantId(receipt.getReceiptNumber(), TENANT_ID)
                    .stream().map(com.sivion.api.supplychain.domain.MaterialLot::getReceivedQuantity)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
                if (alreadyAssigned.add(receivedQuantity).compareTo(receipt.getQuantity()) > 0)
                    throw conflict("Material lot quantities exceed the linked goods receipt quantity.");
            }
            if (!Objects.equals(receipt.getProductId(), materialId)) {
                throw bad("The material does not match the linked goods receipt.");
            }
            if (warehouseId != null && !Objects.equals(receipt.getWarehouseId(), warehouseId)) {
                throw bad("The warehouse does not match the linked goods receipt.");
            }
        }
        return receipt;
    }

    private GoodsReceipt findGoodsReceipt(String reference) {
        GoodsReceipt byNumber = goodsReceipts.findByReceiptNumberAndTenantId(reference, TENANT_ID).orElse(null);
        if (byNumber != null) return byNumber;
        try {
            return goodsReceipts.findById(Long.parseLong(reference))
                    .filter(receipt -> Objects.equals(receipt.getTenantId(), TENANT_ID)).orElse(null);
        } catch (NumberFormatException ignored) {
            return null;
        }
    }

    private ResponseStatusException notFound(String type) {
        return new ResponseStatusException(HttpStatus.NOT_FOUND, type + " was not found in this tenant.");
    }
    private ResponseStatusException bad(String message) {
        return new ResponseStatusException(HttpStatus.BAD_REQUEST, message);
    }
    private ResponseStatusException conflict(String message) {
        return new ResponseStatusException(HttpStatus.CONFLICT, message);
    }
}
