package com.sivion.api.integration;

import com.sivion.api.finance.domain.Invoice;
import com.sivion.api.finance.repo.InvoiceRepository;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.util.*;

@Component public class FinanceDispatchListener {
 private static final long TENANT=1L; private final InvoiceRepository invoices;
 public FinanceDispatchListener(InvoiceRepository invoices){this.invoices=invoices;}
 @Transactional @RabbitListener(queues="sivion.finance.dispatches") public void onDispatched(Map<String,Object> event){Long orderId=number(event.get("salesOrderId"));if(orderId==null||invoices.findByTenantIdAndSalesOrderId(TENANT,orderId).isPresent())return;Object customer=event.get("customerId");Object amount=event.get("amount");if(customer==null||amount==null)return;Invoice i=new Invoice();i.setTenantId(TENANT);i.setSalesOrderId(orderId);i.setCustomerId(number(customer));i.setAmount(new BigDecimal(String.valueOf(amount)));i.setInvoiceNumber("INV-"+UUID.randomUUID().toString().substring(0,8).toUpperCase());i.setStatus("ISSUED");invoices.save(i);}
 private Long number(Object v){return v==null?null:Long.valueOf(String.valueOf(v));}
}
