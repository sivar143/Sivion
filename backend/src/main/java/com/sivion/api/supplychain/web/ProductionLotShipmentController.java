package com.sivion.api.supplychain.web;

import com.sivion.api.supplychain.service.ProductionLotShipmentService;
import com.sivion.api.supplychain.service.ProductionLotShipmentService.AllocationRequest;
import com.sivion.api.supplychain.service.ProductionLotShipmentService.LinkView;
import jakarta.validation.Valid;
import java.util.List;
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

    @GetMapping("/{lotCode}/shipments")
    @PreAuthorize("hasAnyRole('ADMIN','WAREHOUSE_MANAGER','WAREHOUSE_USER','INVENTORY_MANAGER','INVENTORY_USER','MANUFACTURING_MANAGER','MANUFACTURING_USER','SALES_MANAGER','SALES_USER')")
    public List<LinkView> list(@PathVariable @NotBlank @Size(max=80) String lotCode) {
        return service.list(lotCode);
    }

    @PostMapping("/{lotCode}/shipments")
    @PreAuthorize("hasAnyRole('ADMIN','WAREHOUSE_MANAGER','INVENTORY_MANAGER','MANUFACTURING_MANAGER','SALES_MANAGER')")
    public LinkView allocate(@PathVariable @NotBlank @Size(max=80) String lotCode,
            @Valid @RequestBody AllocationRequest request) {
        return service.allocate(lotCode,request);
    }
}
