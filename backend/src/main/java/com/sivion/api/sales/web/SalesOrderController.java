package com.sivion.api.sales.web;

import com.sivion.api.sales.domain.SalesOrder;
import com.sivion.api.sales.service.SalesOrderService;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/sales/orders")
@PreAuthorize("hasAnyRole('ADMIN','SALES_MANAGER','SALES_USER')")
public class SalesOrderController {
    private final SalesOrderService service;
    public SalesOrderController(SalesOrderService service){this.service=service;}
    @GetMapping public List<SalesOrder> list(){return service.list();}
    @PostMapping @ResponseStatus(HttpStatus.CREATED) public SalesOrder create(@RequestBody SalesOrderService.OrderRequest request){return service.create(request);}
    @PatchMapping("/{id}/status") public SalesOrder status(@PathVariable Long id,@RequestBody Map<String,String> body){return service.updateStatus(id,body.get("status"));}
}
