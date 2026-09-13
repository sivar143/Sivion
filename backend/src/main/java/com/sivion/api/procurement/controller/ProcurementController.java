package com.sivion.api.procurement.controller;

import com.sivion.api.procurement.service.ProcurementService; import org.springframework.security.access.prepost.PreAuthorize; import org.springframework.web.bind.annotation.*; import java.util.*;

@RestController @RequestMapping("/api/v1/procurement") @PreAuthorize("hasAnyRole('ADMIN','PROCUREMENT_MANAGER')") public class ProcurementController {
 private final ProcurementService service; public ProcurementController(ProcurementService s){service=s;}
 @GetMapping("/suppliers") public List<ProcurementService.SupplierView> suppliers(){return service.suppliers();} @PostMapping("/suppliers") public ProcurementService.SupplierView createSupplier(@RequestBody ProcurementService.SupplierRequest r){return service.createSupplier(r);}
 @GetMapping("/requests") public List<ProcurementService.RequestView> requests(){return service.requests();} @PostMapping("/requests") public ProcurementService.RequestView createRequest(@RequestBody ProcurementService.RequestRequest r){return service.createRequest(r);}
 @GetMapping("/orders") public List<ProcurementService.OrderView> orders(){return service.orders();} @PostMapping("/orders") public ProcurementService.OrderView createOrder(@RequestBody ProcurementService.OrderRequest r){return service.createOrder(r);}
 @GetMapping("/goods-received") public List<ProcurementService.ReceiptView> receipts(){return service.receipts();} @PostMapping("/goods-received") public ProcurementService.ReceiptView receive(@RequestBody ProcurementService.ReceiptRequest r){return service.receive(r);}
}
