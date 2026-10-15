package com.sivion.api.supplychain.service;

import com.sivion.api.supplychain.domain.ManufacturingOrder;
import com.sivion.api.inventory.repo.MaterialRepository;
import com.sivion.api.supplychain.repo.ManufacturingOrderRepository;
import java.math.BigDecimal;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class ManufacturingOrderService {
    private static final long TENANT_ID = 1L;
    private static final Set<String> STATUSES = Set.of("PLANNED", "IN_PROGRESS", "COMPLETED", "CANCELLED");
    private final ManufacturingOrderRepository repository;
    private final MaterialRepository materials;
    public ManufacturingOrderService(ManufacturingOrderRepository repository, MaterialRepository materials){this.repository=repository;this.materials=materials;}

    @Transactional
    public OrderView create(OrderRequest request) {
        if(request==null || request.orderNumber()==null || request.orderNumber().isBlank())
            throw bad("Manufacturing order number is required.");
        if(request.productId()==null || request.productId()<=0 || request.plannedQuantity()==null
                || request.plannedQuantity().signum()<=0)
            throw bad("A valid finished product and positive planned quantity are required.");
        materials.findByIdAndTenantId(request.productId(), TENANT_ID)
            .filter(product -> "ACTIVE".equalsIgnoreCase(product.getStatus()))
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Active finished product was not found."));
        String number=request.orderNumber().trim();
        if(repository.existsByTenantIdAndOrderNumber(TENANT_ID,number))
            throw new ResponseStatusException(HttpStatus.CONFLICT,"Manufacturing order number already exists.");
        return view(repository.save(new ManufacturingOrder(number,request.productId(),request.plannedQuantity())));
    }

    @Transactional(readOnly=true)
    public List<OrderView> list(){return repository.findAllByTenantIdOrderByCreatedAtDesc(TENANT_ID).stream().map(this::view).toList();}

    @Transactional
    public OrderView updateStatus(long id,String status) {
        ManufacturingOrder order=repository.findByIdAndTenantId(id,TENANT_ID)
            .orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND,"Manufacturing order was not found."));
        String next=status==null?"":status.trim().toUpperCase(Locale.ROOT);
        if(!STATUSES.contains(next)) throw bad("Status must be PLANNED, IN_PROGRESS, COMPLETED, or CANCELLED.");
        if("CANCELLED".equals(order.getStatus()) || "COMPLETED".equals(order.getStatus()))
            throw new ResponseStatusException(HttpStatus.CONFLICT,"A completed or cancelled manufacturing order cannot be changed.");
        order.setStatus(next);
        return view(repository.save(order));
    }
    private OrderView view(ManufacturingOrder o){return new OrderView(o.getId(),o.getOrderNumber(),o.getProductId(),o.getPlannedQuantity(),o.getStatus(),o.getCreatedAt());}
    private ResponseStatusException bad(String message){return new ResponseStatusException(HttpStatus.BAD_REQUEST,message);}
    public record OrderRequest(String orderNumber,Long productId,BigDecimal plannedQuantity){}
    public record OrderView(Long id,String orderNumber,Long productId,BigDecimal plannedQuantity,String status,java.time.Instant createdAt){}
}
