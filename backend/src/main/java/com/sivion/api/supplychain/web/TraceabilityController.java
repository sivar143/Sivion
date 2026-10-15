package com.sivion.api.supplychain.web;

import com.sivion.api.supplychain.service.TraceabilityService;
import com.sivion.api.supplychain.service.TraceabilityService.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@Validated
@RequestMapping("/api/v1/supply-chain")
public class TraceabilityController {
    private final TraceabilityService service;
    public TraceabilityController(TraceabilityService service){this.service=service;}

    @PostMapping("/material-lots")
    @PreAuthorize("hasAnyRole('ADMIN','PROCUREMENT_MANAGER','WAREHOUSE_MANAGER','INVENTORY_MANAGER','INVENTORY_USER')")
    public MaterialLotView receive(@Valid @RequestBody MaterialLotRequest r){return service.receiveMaterial(r);}

    @PostMapping("/production-lots")
    @PreAuthorize("hasAnyRole('ADMIN','MANUFACTURING_MANAGER','MANUFACTURING_USER','WAREHOUSE_MANAGER')")
    public ProductionLotView produce(@Valid @RequestBody ProductionRequest r){return service.recordProduction(r);}

    @PostMapping("/lot-consumptions")
    @PreAuthorize("hasAnyRole('ADMIN','MANUFACTURING_MANAGER','MANUFACTURING_USER')")
    public ConsumptionView consume(@Valid @RequestBody ConsumptionRequest r){return service.consume(r);}

    @GetMapping("/lots/{lotCode}/genealogy")
    @PreAuthorize("hasAnyRole('ADMIN','PROCUREMENT_MANAGER','WAREHOUSE_MANAGER','WAREHOUSE_USER','INVENTORY_MANAGER','INVENTORY_USER','MANUFACTURING_MANAGER','MANUFACTURING_USER','SALES_MANAGER','SALES_USER')")
    public GenealogyView genealogy(@PathVariable @NotBlank @Size(max=80) String lotCode){return service.genealogy(lotCode);}

    @PostMapping("/shipments/{id}/proof-of-delivery")
    @PreAuthorize("hasAnyRole('ADMIN','WAREHOUSE_MANAGER','INVENTORY_MANAGER','SALES_MANAGER','SALES_USER')")
    public ProofView proof(@PathVariable long id,@Valid @RequestBody ProofRequest r){return service.recordProof(id,r);}
}
