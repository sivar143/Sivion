package com.sivion.api.supplychain.web;

import com.sivion.api.supplychain.service.ManufacturingOrderService;
import com.sivion.api.supplychain.service.ManufacturingOrderService.OrderRequest;
import com.sivion.api.supplychain.service.ManufacturingOrderService.OrderView;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@Validated
@RequestMapping("/api/v1/supply-chain/manufacturing-orders")
public class ManufacturingOrderController {
    private final ManufacturingOrderService service;
    public ManufacturingOrderController(ManufacturingOrderService service){this.service=service;}

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','MANUFACTURING_MANAGER','MANUFACTURING_USER','WAREHOUSE_MANAGER')")
    public List<OrderView> list(){return service.list();}

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','MANUFACTURING_MANAGER')")
    public OrderView create(@Valid @RequestBody OrderRequest request){return service.create(request);}

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('ADMIN','MANUFACTURING_MANAGER')")
    public OrderView status(@PathVariable long id,@RequestBody StatusRequest request){return service.updateStatus(id,request.status());}

    public record StatusRequest(@NotBlank String status){}
}
