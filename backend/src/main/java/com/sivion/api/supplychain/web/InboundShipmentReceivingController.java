package com.sivion.api.supplychain.web;

import com.sivion.api.supplychain.service.InboundShipmentReceivingService;
import com.sivion.api.supplychain.service.InboundShipmentReceivingService.ReceiptRequest;
import com.sivion.api.supplychain.service.InboundShipmentReceivingService.ReceiptView;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@Validated
@RequestMapping("/api/v1/supply-chain/shipments/{shipmentId}/receipts")
public class InboundShipmentReceivingController {
    private final InboundShipmentReceivingService service;
    public InboundShipmentReceivingController(InboundShipmentReceivingService service){this.service=service;}

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','PROCUREMENT_MANAGER','WAREHOUSE_MANAGER','WAREHOUSE_USER','INVENTORY_MANAGER','INVENTORY_USER','QUALITY_MANAGER')")
    public ReceiptView receive(@PathVariable @Positive long shipmentId, @Valid @RequestBody Request request) {
        return service.receive(shipmentId,new ReceiptRequest(request.receiptReference(),request.lotCode(),
            request.productId(),request.warehouseId(),request.quantity(),request.supplierName(),
            request.supplierLotCode(),request.manufacturedAt(),request.expiresAt(),request.finalReceipt()));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','PROCUREMENT_MANAGER','WAREHOUSE_MANAGER','WAREHOUSE_USER','INVENTORY_MANAGER','INVENTORY_USER','QUALITY_MANAGER')")
    public List<ReceiptView> list(@PathVariable @Positive long shipmentId) {
        return service.list(shipmentId);
    }

    public record Request(@NotBlank @Size(max=80) String receiptReference,
        @NotBlank @Size(max=80) String lotCode, @NotNull @Positive Long productId,
        @NotNull @Positive Long warehouseId, @NotNull @Positive BigDecimal quantity,
        @Size(max=180) String supplierName, @Size(max=100) String supplierLotCode,
        Instant manufacturedAt, Instant expiresAt, boolean finalReceipt) { }
}
