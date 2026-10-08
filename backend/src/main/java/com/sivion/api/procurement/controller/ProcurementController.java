package com.sivion.api.procurement.controller;

import com.sivion.api.procurement.service.ProcurementService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/procurement")
@PreAuthorize("hasAnyRole('ADMIN','PROCUREMENT_MANAGER')")
public class ProcurementController {

    private final ProcurementService service;

    public ProcurementController(ProcurementService service) {
        this.service = service;
    }

    @GetMapping("/suppliers")
    public List<ProcurementService.SupplierView> suppliers() {
        return service.suppliers();
    }

    @PostMapping("/suppliers")
    public ProcurementService.SupplierView createSupplier(
            @RequestBody ProcurementService.SupplierRequest request
    ) {
        return service.createSupplier(request);
    }

    @GetMapping("/requests")
    public List<ProcurementService.RequestView> requests() {
        return service.requests();
    }

    @PostMapping("/requests")
    public ProcurementService.RequestView createRequest(
            @RequestBody ProcurementService.RequestRequest request
    ) {
        return service.createRequest(request);
    }

    @PatchMapping("/requests/{id}")
    public ProcurementService.RequestView updateRequest(
            @PathVariable Long id,
            @RequestParam String status
    ) {
        return service.updateRequestStatus(id, status);
    }

    @GetMapping("/orders")
    public List<ProcurementService.OrderView> orders() {
        return service.orders();
    }

    @PostMapping("/orders")
    public ProcurementService.OrderView createOrder(
            @RequestBody ProcurementService.OrderRequest request
    ) {
        return service.createOrder(request);
    }

    @PatchMapping("/orders/{id}")
    public ProcurementService.OrderView updateOrder(
            @PathVariable Long id,
            @RequestParam String status
    ) {
        return service.updateOrderStatus(id, status);
    }

    @GetMapping("/goods-received")
    public List<ProcurementService.ReceiptView> receipts() {
        return service.receipts();
    }

    @PostMapping("/goods-received")
    public ProcurementService.ReceiptView receive(
            @RequestBody ProcurementService.ReceiptRequest request
    ) {
        return service.receive(request);
    }
}
