package com.sivion.api.integration;

import com.sivion.api.inventory.repo.InventoryTransactionRepository; import com.sivion.api.inventory.service.InventoryService; import org.springframework.amqp.rabbit.annotation.RabbitListener; import org.springframework.stereotype.Component; import java.math.BigDecimal; import java.util.Map;

@Component public class ProcurementGoodsReceivedListener {
 private static final long TENANT=1L; private final InventoryService inventory; private final InventoryTransactionRepository transactions;
 public ProcurementGoodsReceivedListener(InventoryService inventory,InventoryTransactionRepository transactions){this.inventory=inventory;this.transactions=transactions;}
 @RabbitListener(queues="sivion.inventory.goods-received")
 public void onGoodsReceived(Map<String,Object> event){Long receiptId=number(event.get("goodsReceiptId"));Long productId=number(event.get("productId"));Long warehouseId=number(event.get("warehouseId"));BigDecimal quantity=decimal(event.get("quantity"));if(receiptId==null||productId==null||warehouseId==null||quantity==null||quantity.signum()<=0)return;if(transactions.existsByTenantIdAndTransactionTypeAndReferenceTypeAndReferenceId(TENANT,"GOODS_RECEIVED","GOODS_RECEIPT",receiptId))return;inventory.stock(productId,warehouseId,quantity,"GOODS_RECEIVED","GOODS_RECEIPT:"+receiptId);}
 private Long number(Object value){return value==null?null:Long.valueOf(String.valueOf(value));} private BigDecimal decimal(Object value){return value==null?null:new BigDecimal(String.valueOf(value));}
}
