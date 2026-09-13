package com.sivion.api.integration;

import com.sivion.api.finance.domain.Invoice; import com.sivion.api.finance.repo.InvoiceRepository; import org.springframework.amqp.rabbit.annotation.RabbitListener; import org.springframework.stereotype.Component; import org.springframework.transaction.annotation.Transactional; import java.math.BigDecimal; import java.util.*;

@Component public class FinanceDispatchListener {
 private static final long TENANT=1L; private final InvoiceRepository invoices;
 public FinanceDispatchListener(InvoiceRepository invoices){this.invoices=invoices;}
 @Transactional @RabbitListener(queues="sivion.finance.dispatches") public void onDispatched(Map<String,Object> event){Long orderId=number(event.get("salesOrderId"));if(orderId==null||invoices.findByTenantIdAndSalesOrderId(TENANT,orderId).isPresent())return;Long customer=number(event.get("customerId"));BigDecimal amount=decimal(event.get("amount"));if(customer==null||amount==null||amount.signum()<0)return;Invoice i=new Invoice();i.setTenantId(TENANT);i.setSalesOrderId(orderId);i.setCustomerId(customer);i.setAmount(amount);i.setInvoiceNumber("INV-"+UUID.randomUUID().toString().substring(0,8).toUpperCase());i.setStatus("ISSUED");invoices.save(i);}
 private Long number(Object v){return v==null?null:Long.valueOf(String.valueOf(v));} private BigDecimal decimal(Object v){return v==null?null:new BigDecimal(String.valueOf(v));}
}
