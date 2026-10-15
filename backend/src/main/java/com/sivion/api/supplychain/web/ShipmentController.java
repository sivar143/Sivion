package com.sivion.api.supplychain.web;

import com.sivion.api.supplychain.service.ShipmentService;
import com.sivion.api.supplychain.service.ShiprocketTrackingService;
import com.sivion.api.supplychain.service.ShipmentService.CreateShipment;
import com.sivion.api.supplychain.service.ShipmentService.PublicTracking;
import com.sivion.api.supplychain.service.ShipmentService.ShipmentDetail;
import com.sivion.api.supplychain.service.ShipmentService.ShipmentView;
import com.sivion.api.supplychain.service.ShipmentService.StatusUpdate;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@Validated
@RequestMapping("/api/v1/supply-chain")
public class ShipmentController {
    private final ShipmentService service;
    private final ShiprocketTrackingService shiprocketTracking;

    public ShipmentController(ShipmentService service, ShiprocketTrackingService shiprocketTracking) {
        this.service = service;
        this.shiprocketTracking = shiprocketTracking;
    }

    @GetMapping("/shipments")
    @PreAuthorize("hasAnyRole('ADMIN','PROCUREMENT_MANAGER','WAREHOUSE_MANAGER','WAREHOUSE_USER','INVENTORY_MANAGER','INVENTORY_USER','MANUFACTURING_MANAGER','MANUFACTURING_USER','SALES_MANAGER','SALES_USER')")
    public List<ShipmentView> list() {
        return service.list();
    }

    @GetMapping("/shipments/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','PROCUREMENT_MANAGER','WAREHOUSE_MANAGER','WAREHOUSE_USER','INVENTORY_MANAGER','INVENTORY_USER','MANUFACTURING_MANAGER','MANUFACTURING_USER','SALES_MANAGER','SALES_USER')")
    public ShipmentDetail detail(@PathVariable long id) {
        return service.detail(id);
    }

    @PostMapping("/shipments")
    @PreAuthorize("hasAnyRole('ADMIN','PROCUREMENT_MANAGER','WAREHOUSE_MANAGER','INVENTORY_MANAGER','MANUFACTURING_MANAGER','SALES_MANAGER')")
    public ShipmentDetail create(@Valid @RequestBody CreateShipmentRequest request) {
        return service.create(new CreateShipment(request.shipmentNumber(), request.direction(),
            request.referenceNumber(), request.purchaseOrderId(), request.goodsReceiptReference(), request.salesOrderId(), request.dispatchId(), request.supplierName(), request.customerName(), request.carrierName(),
            request.carrierTrackingNumber(), request.originLocation(), request.destinationLocation(),
            request.dispatchedAt(), request.estimatedArrivalAt(), request.notes()));
    }

    @PostMapping("/shipments/{id}/events")
    @PreAuthorize("hasAnyRole('ADMIN','PROCUREMENT_MANAGER','WAREHOUSE_MANAGER','INVENTORY_MANAGER','MANUFACTURING_MANAGER','SALES_MANAGER')")
    public ShipmentDetail updateStatus(@PathVariable long id, @Valid @RequestBody StatusUpdateRequest request) {
        return service.updateStatus(id, new StatusUpdate(request.status(), request.eventTime(),
            request.location(), request.remarks(), request.idempotencyKey()));
    }

    @PostMapping("/shipments/{id}/carrier-refresh")
    @PreAuthorize("hasAnyRole('ADMIN','WAREHOUSE_MANAGER','INVENTORY_MANAGER','SALES_MANAGER','SALES_USER')")
    public ShipmentDetail refreshCarrierTracking(@PathVariable long id) {
        return shiprocketTracking.refresh(id);
    }

    @GetMapping("/track/{token}")
    public PublicTracking publicTracking(@PathVariable @NotBlank @Size(min = 48, max = 48) String token) {
        return service.publicTracking(token);
    }

    public record CreateShipmentRequest(
        @NotBlank @Size(max = 60) String shipmentNumber,
        @NotBlank String direction,
        @Size(max = 100) String referenceNumber,
        Long purchaseOrderId,
        @Size(max = 100) String goodsReceiptReference,
        Long salesOrderId,
        Long dispatchId,
        @Size(max = 180) String supplierName,
        @Size(max = 180) String customerName,
        @Size(max = 120) String carrierName,
        @Size(max = 160) String carrierTrackingNumber,
        @Size(max = 250) String originLocation,
        @Size(max = 250) String destinationLocation,
        java.time.Instant dispatchedAt,
        java.time.Instant estimatedArrivalAt,
        @Size(max = 1000) String notes) { }

    public record StatusUpdateRequest(
        @NotBlank String status,
        java.time.Instant eventTime,
        @Size(max = 250) String location,
        @Size(max = 1000) String remarks,
        @Size(max = 120) String idempotencyKey) { }
}
