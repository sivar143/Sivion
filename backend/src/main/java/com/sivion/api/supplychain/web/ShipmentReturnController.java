package com.sivion.api.supplychain.web;

import com.sivion.api.supplychain.service.ShipmentReturnService;
import com.sivion.api.supplychain.service.ShipmentReturnService.ReturnRequest;
import com.sivion.api.supplychain.service.ShipmentReturnService.ReturnView;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/supply-chain/shipments")
public class ShipmentReturnController {
    private final ShipmentReturnService service;
    public ShipmentReturnController(ShipmentReturnService service){this.service=service;}

    @PostMapping("/{shipmentId}/returns")
    @PreAuthorize("hasAnyRole('ADMIN','WAREHOUSE_MANAGER','INVENTORY_MANAGER','SALES_MANAGER')")
    public ReturnView receive(@PathVariable long shipmentId, @Valid @RequestBody ReturnRequest request) {
        return service.receive(shipmentId, request);
    }
}
