package com.sivion.api.inventory.service;

import com.sivion.api.inventory.domain.*;
import com.sivion.api.inventory.repo.*;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.*;

@Service
public class InventoryService {
 private static final long TENANT=1L;
 private final MaterialRepository materials; private final WarehouseRepository warehouses; private final InventoryTransactionRepository transactions; private final DispatchRepository dispatches;
 public InventoryService(MaterialRepository m,WarehouseRepository w,InventoryTransactionRepository t,DispatchRepository d){materials=m;warehouses=w;transactions=t;dispatches=d;}
 public List<MaterialView> materials(){return materials.findByTenantIdOrderByName(TENANT).stream().map(m->new MaterialView(m.getId(),m.getSku(),m.getName(),m.getDescription(),m.getUnit(),m.getReorderLevel(),m.getStatus(),transactions.balance(TENANT,1L,m.getId()))).toList();}
 @Transactional public MaterialView create(MaterialRequest r){if(materials.existsByTenantIdAndSku(TENANT,r.sku()))throw new IllegalArgumentException("Material SKU already exists"); Material m=new Material();m.setTenantId(TENANT);m.setSku(r.sku());m.setName(r.name());m.setDescription(r.description());m.setUnit(r.unit());m.setReorderLevel(r.reorderLevel()==null?BigDecimal.ZERO:r.reorderLevel());m.setStatus("ACTIVE");m=materials.save(m); if(r.initialQuantity()!=null&&r.initialQuantity().signum()!=0) addTransaction(m.getId(),1L,"STOCK_IN",r.initialQuantity(),"MATERIAL",m.getId()); return new MaterialView(m.getId(),m.getSku(),m.getName(),m.getDescription(),m.getUnit(),m.getReorderLevel(),m.getStatus(),transactions.balance(TENANT,1L,m.getId()));}
 @Transactional public void stock(Long productId,Long warehouseId,BigDecimal quantity,String type,String reference){if(quantity==null||quantity.signum()<=0)throw new IllegalArgumentException("Quantity must be greater than zero"); materials.findByIdAndTenantId(productId,TENANT).orElseThrow(()->new NoSuchElementException("Material not found")); warehouses.findByIdAndTenantId(warehouseId,TENANT).orElseThrow(()->new NoSuchElementException("Warehouse not found")); if("ADJUSTMENT".equals(type)){addTransaction(productId,warehouseId,type,quantity,reference,null);return;} addTransaction(productId,warehouseId,"STOCK_IN",quantity,reference,null);}
 @Transactional public DispatchView dispatch(DispatchRequest r){if(r.items()==null||r.items().isEmpty())throw new IllegalArgumentException("At least one dispatch item is required"); Dispatch d=new Dispatch();d.setTenantId(TENANT);d.setDispatchNumber("DSP-"+UUID.randomUUID().toString().substring(0,8).toUpperCase());d.setCustomerId(r.customerId());d.setReferenceNumber(r.referenceNumber());d.setDeliveryAddress(r.deliveryAddress());d.setNotes(r.notes());d.setDispatchDate(Instant.now());d.setStatus("DISPATCHED"); for(ItemRequest i:r.items()){BigDecimal balance=transactions.balance(TENANT,i.warehouseId(),i.productId());if(balance.compareTo(i.quantity())<0)throw new IllegalArgumentException("Insufficient stock for material " + i.productId());DispatchItem di=new DispatchItem();di.setProductId(i.productId());di.setQuantity(i.quantity());di.setWarehouseId(i.warehouseId());d.addItem(di);} d=dispatches.save(d);for(DispatchItem i:d.getItems())addTransaction(i.getProductId(),i.getWarehouseId(),"DISPATCH",i.getQuantity().negate(),"DISPATCH",d.getId());return view(d);}
 public List<DispatchView> history(){return dispatches.findByTenantIdOrderByDispatchDateDesc(TENANT).stream().map(this::view).toList();}
 private void addTransaction(Long p,Long w,String type,BigDecimal q,String ref,Long refId){InventoryTransaction t=new InventoryTransaction();t.setTenantId(TENANT);t.setProductId(p);t.setWarehouseId(w);t.setTransactionType(type);t.setQuantity(q);t.setReferenceType(ref);t.setReferenceId(refId);t.setCorrelationId(UUID.randomUUID().toString());transactions.save(t);}
 private DispatchView view(Dispatch d){return new DispatchView(d.getId(),d.getDispatchNumber(),d.getCustomerId(),d.getReferenceNumber(),d.getDispatchDate(),d.getDeliveryAddress(),d.getNotes(),d.getStatus(),d.getItems().stream().map(i->new ItemView(i.getProductId(),i.getWarehouseId(),i.getQuantity())).toList());}
 public record MaterialRequest(String sku,String name,String description,String unit,BigDecimal initialQuantity,BigDecimal reorderLevel){}
 public record MaterialView(Long id,String sku,String name,String description,String unit,BigDecimal reorderLevel,String status,BigDecimal quantity){}
 public record ItemRequest(Long productId,Long warehouseId,BigDecimal quantity){}
 public record DispatchRequest(Long customerId,String referenceNumber,String deliveryAddress,String notes,List<ItemRequest> items){}
 public record ItemView(Long productId,Long warehouseId,BigDecimal quantity){}
 public record DispatchView(Long id,String dispatchNumber,Long customerId,String referenceNumber,Instant dispatchDate,String deliveryAddress,String notes,String status,List<ItemView> items){}
}
