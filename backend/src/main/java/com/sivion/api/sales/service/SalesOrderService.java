package com.sivion.api.sales.service;

import com.sivion.api.integration.RabbitTopology;
import com.sivion.api.sales.domain.SalesOrder;
import com.sivion.api.sales.domain.SalesOrderItem;
import com.sivion.api.sales.repo.SalesOrderRepository;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal; import java.util.*;

@Service public class SalesOrderService {
 private static final long TENANT=1L; private static final Set<String> STATUSES=Set.of("DRAFT","CONFIRMED","RESERVED","PARTIALLY_DISPATCHED","DISPATCHED","CANCELLED"); private final SalesOrderRepository repository; private final RabbitTemplate rabbit;
 public SalesOrderService(SalesOrderRepository repository,RabbitTemplate rabbit){this.repository=repository;this.rabbit=rabbit;}
 public List<SalesOrder> list(){return repository.findByTenantIdOrderByOrderDateDesc(TENANT);}
 @Transactional public SalesOrder create(OrderRequest request){if(request.customerId()==null)throw new IllegalArgumentException("customerId is required");if(request.items()==null||request.items().isEmpty())throw new IllegalArgumentException("At least one order item is required");SalesOrder order=new SalesOrder();order.setTenantId(TENANT);order.setCustomerId(request.customerId());order.setOpportunityId(request.opportunityId());order.setDeliveryAddress(request.deliveryAddress());order.setNotes(request.notes());order.setOrderNumber(nextNumber());BigDecimal total=BigDecimal.ZERO;for(ItemRequest r:request.items()){if(r.materialId()==null||r.quantity()==null||r.quantity().signum()<=0)throw new IllegalArgumentException("Each item requires a positive materialId and quantity");if(r.unitPrice()==null||r.unitPrice().signum()<0)throw new IllegalArgumentException("Each item requires a non-negative unitPrice");SalesOrderItem item=new SalesOrderItem();item.setMaterialId(r.materialId());item.setQuantity(r.quantity());item.setUnitPrice(r.unitPrice());item.setDescription(r.description());item.setLineTotal(r.quantity().multiply(r.unitPrice()));order.addItem(item);total=total.add(item.getLineTotal());}order.setTotalAmount(total);return repository.save(order);}
 @Transactional public SalesOrder updateStatus(Long id,String status){String next=status==null?"":status.toUpperCase(Locale.ROOT);if(!STATUSES.contains(next))throw new IllegalArgumentException("Unsupported order status: "+status);SalesOrder order=repository.findByIdAndTenantId(id,TENANT).orElseThrow(()->new NoSuchElementException("Sales order not found"));String current=order.getStatus();if("CANCELLED".equals(current)||"DISPATCHED".equals(current))throw new IllegalStateException("Order cannot be changed from "+current);order.setStatus(next);if("CONFIRMED".equals(next)&&!"CONFIRMED".equals(current))publishConfirmed(order);return order;}
 private void publishConfirmed(SalesOrder order){rabbit.convertAndSend(RabbitTopology.EXCHANGE,"sales.order.confirmed",Map.of("salesOrderId",order.getId(),"items",order.getItems().stream().map(i->Map.of("productId",i.getMaterialId(),"quantity",i.getQuantity())).toList()));}
 @Transactional public void markReserved(Long id){repository.findByIdAndTenantId(id,TENANT).ifPresent(o->{if(!"CANCELLED".equals(o.getStatus())&&!"DISPATCHED".equals(o.getStatus()))o.setStatus("RESERVED");});}
 @Transactional public void markDispatched(Long id){repository.findByIdAndTenantId(id,TENANT).ifPresent(o->{if(!"CANCELLED".equals(o.getStatus())){o.setStatus("DISPATCHED");rabbit.convertAndSend(RabbitTopology.EXCHANGE,"sales.order.dispatched",Map.of("salesOrderId",o.getId(),"customerId",o.getCustomerId(),"amount",o.getTotalAmount()));}});}
 private String nextNumber(){return "SO-"+UUID.randomUUID().toString().substring(0,8).toUpperCase();}
 public record ItemRequest(Long materialId,BigDecimal quantity,BigDecimal unitPrice,String description){} public record OrderRequest(Long customerId,Long opportunityId,String deliveryAddress,String notes,List<ItemRequest> items){}
}
