package com.sivion.api.supplychain.web;

import com.sivion.api.supplychain.service.ProductionLotShipmentService;
import com.sivion.api.supplychain.service.ProductionLotShipmentService.AllocationRequest;
import com.sivion.api.supplychain.service.ProductionLotShipmentService.LinkView;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@Validated
@RequestMapping("/api/v1/supply-chain/production-lots")
public class ProductionLotShipmentController {
    private final ProductionLotShipmentService service;
    public ProductionLotShipmentController(ProductionLotShipmentService service){this.service=service;}

    @PostMapping("/{lotCode}/shipments")
    @PreAuthorize("hasAnyRole('ADMIN','WAREHOUSE_MANAGER','INVENTORY_MANAGER','MANUFACTURING_MANAGER','SALES_MANAGER')")
    public LinkView allocate(@PathVariable @NotBlank @Size(max=80) String lotCode,
            @Valid @RequestBody AllocationRequest request) {
        return service.allocate(lotCode,request);
    }
}
