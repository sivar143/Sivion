package com.sivion.api.finance.controller;

import com.sivion.api.finance.service.FinanceService;
import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/finance")
@PreAuthorize("hasAnyRole('ADMIN', 'FINANCE_USER', 'FINANCE_MANAGER')")
public class FinanceController {

    private final FinanceService financeService;

    public FinanceController(FinanceService financeService) {
        this.financeService = financeService;
    }

    @GetMapping("/invoices")
    public List<FinanceService.InvoiceView> invoices() {
        return financeService.invoices();
    }

    @PostMapping("/invoices")
    public FinanceService.InvoiceView invoice(
            @RequestBody FinanceService.InvoiceRequest request) {
        return financeService.invoice(request);
    }

    @GetMapping("/payments")
    public List<FinanceService.PaymentView> payments() {
        return financeService.payments();
    }

    @PostMapping("/payments")
    public FinanceService.PaymentView payment(
            @RequestBody FinanceService.PaymentRequest request) {
        return financeService.payment(request);
    }

    @GetMapping("/expenses")
    public List<FinanceService.ExpenseView> expenses() {
        return financeService.expenses();
    }

    @PostMapping("/expenses")
    public FinanceService.ExpenseView expense(
            @RequestBody FinanceService.ExpenseRequest request) {
        return financeService.expense(request);
    }
}
