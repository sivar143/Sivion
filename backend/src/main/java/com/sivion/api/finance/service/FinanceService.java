package com.sivion.api.finance.service;

import com.sivion.api.finance.domain.Expense;
import com.sivion.api.finance.domain.Invoice;
import com.sivion.api.finance.domain.Payment;
import com.sivion.api.finance.repo.ExpenseRepository;
import com.sivion.api.finance.repo.InvoiceRepository;
import com.sivion.api.finance.repo.PaymentRepository;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class FinanceService {

    // Tenant scoping is applied to every finance repository operation until request-level tenant context is introduced.

    private final InvoiceRepository invoices;
    private final PaymentRepository payments;
    private final ExpenseRepository expenses;

    public FinanceService(
            InvoiceRepository invoices,
            PaymentRepository payments,
            ExpenseRepository expenses) {
        this.invoices = invoices;
        this.payments = payments;
        this.expenses = expenses;
    }

    public List<InvoiceView> invoices() {
        return invoices.findByTenantIdOrderByInvoiceDateDesc(TENANT)
                .stream()
                .map(this::map)
                .toList();
    }

    @Transactional
    public InvoiceView invoice(InvoiceRequest request) {
        if (request.customerId() == null) {
            throw new IllegalArgumentException("Customer is required");
        }

        if (request.amount() == null || request.amount().signum() <= 0) {
            throw new IllegalArgumentException("Invoice amount must be greater than zero");
        }

        Invoice invoice = new Invoice();
        invoice.setTenantId(TENANT);
        invoice.setInvoiceNumber(
                "INV-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        invoice.setCustomerId(request.customerId());
        invoice.setAmount(request.amount());
        invoice.setStatus("ISSUED");

        return map(invoices.save(invoice));
    }

    public List<PaymentView> payments() {
        return payments.findByTenantIdOrderByPaidAtDesc(TENANT)
                .stream()
                .map(this::map)
                .toList();
    }

    @Transactional
    public PaymentView payment(PaymentRequest request) {
        if (request.invoiceId() == null) {
            throw new IllegalArgumentException("Invoice is required");
        }

        // Lock the invoice while calculating the outstanding amount to prevent concurrent overpayments.
        Invoice invoice = invoices.findForUpdate(request.invoiceId(), TENANT)
                .orElseThrow(() -> new NoSuchElementException("Invoice not found"));

        if (request.amount() == null || request.amount().signum() <= 0) {
            throw new IllegalArgumentException("Payment amount must be greater than zero");
        }

        if ("CANCELLED".equals(invoice.getStatus()) || "PAID".equals(invoice.getStatus())) {
            throw new IllegalArgumentException("Invoice is already closed");
        }

        BigDecimal received = payments
                .findByTenantIdAndInvoiceIdOrderByPaidAtDesc(TENANT, invoice.getId())
                .stream()
                .map(Payment::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        if (received.add(request.amount()).compareTo(invoice.getAmount()) > 0) {
            throw new IllegalArgumentException("Payment exceeds outstanding invoice amount");
        }

        Payment payment = new Payment();
        payment.setTenantId(TENANT);
        payment.setPaymentNumber(
                "PAY-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        payment.setInvoiceId(invoice.getId());
        payment.setAmount(request.amount());
        payment.setMethod(
                request.method() == null || request.method().isBlank()
                        ? "OTHER"
                        : request.method().trim());

        Payment savedPayment = payments.save(payment);

        BigDecimal total = received.add(request.amount());
        invoice.setStatus(
                total.compareTo(invoice.getAmount()) == 0
                        ? "PAID"
                        : "PARTIALLY_PAID");

        invoices.save(invoice);

        return map(savedPayment);
    }

    public List<ExpenseView> expenses() {
        return expenses.findByTenantIdOrderByExpenseDateDesc(TENANT)
                .stream()
                .map(this::map)
                .toList();
    }

    @Transactional
    public ExpenseView expense(ExpenseRequest request) {
        if (request.category() == null || request.category().isBlank()) {
            throw new IllegalArgumentException("Expense category is required");
        }

        if (request.description() == null || request.description().isBlank()) {
            throw new IllegalArgumentException("Expense description is required");
        }

        if (request.amount() == null || request.amount().signum() <= 0) {
            throw new IllegalArgumentException("Expense amount must be greater than zero");
        }

        Expense expense = new Expense();
        expense.setTenantId(TENANT);
        expense.setExpenseNumber(
                "EXP-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        expense.setCategory(request.category().trim());
        expense.setDescription(request.description().trim());
        expense.setAmount(request.amount());
        expense.setStatus("SUBMITTED");

        return map(expenses.save(expense));
    }

    private InvoiceView map(Invoice invoice) {
        return new InvoiceView(
                invoice.getId(),
                invoice.getInvoiceNumber(),
                invoice.getCustomerId(),
                invoice.getAmount(),
                invoice.getStatus(),
                invoice.getInvoiceDate());
    }

    private PaymentView map(Payment payment) {
        return new PaymentView(
                payment.getId(),
                payment.getPaymentNumber(),
                payment.getInvoiceId(),
                payment.getAmount(),
                payment.getMethod(),
                payment.getPaidAt());
    }

    private ExpenseView map(Expense expense) {
        return new ExpenseView(
                expense.getId(),
                expense.getExpenseNumber(),
                expense.getCategory(),
                expense.getDescription(),
                expense.getAmount(),
                expense.getStatus(),
                expense.getExpenseDate());
    }

    public record InvoiceRequest(Long customerId, BigDecimal amount) {
    }

    public record InvoiceView(
            Long id,
            String invoiceNumber,
            Long customerId,
            BigDecimal amount,
            String status,
            Instant invoiceDate) {
    }

    public record PaymentRequest(Long invoiceId, BigDecimal amount, String method) {
    }

    public record PaymentView(
            Long id,
            String paymentNumber,
            Long invoiceId,
            BigDecimal amount,
            String method,
            Instant paidAt) {
    }

    public record ExpenseRequest(String category, String description, BigDecimal amount) {
    }

    public record ExpenseView(
            Long id,
            String expenseNumber,
            String category,
            String description,
            BigDecimal amount,
            String status,
            Instant expenseDate) {
    }
}
