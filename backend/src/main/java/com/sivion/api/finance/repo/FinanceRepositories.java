package com.sivion.api.finance.repo;
import com.sivion.api.finance.domain.*; import org.springframework.data.jpa.repository.JpaRepository; import java.util.*;
interface InvoiceRepository extends JpaRepository<Invoice,Long>{List<Invoice> findByTenantIdOrderByInvoiceDateDesc(Long t);Optional<Invoice> findByIdAndTenantId(Long id,Long t);}
interface PaymentRepository extends JpaRepository<Payment,Long>{List<Payment> findByTenantIdOrderByPaidAtDesc(Long t);}
interface ExpenseRepository extends JpaRepository<Expense,Long>{List<Expense> findByTenantIdOrderByExpenseDateDesc(Long t);}
