package com.sivion.api.supplychain;

import com.sivion.api.supplychain.service.ShipmentService;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

/** Converts the committed inventory dispatch event into an idempotent outbound shipment. */
@Component
public class SupplyChainDispatchListener {
    private static final Logger LOGGER = LoggerFactory.getLogger(SupplyChainDispatchListener.class);
    private final ShipmentService shipments;
    public SupplyChainDispatchListener(ShipmentService shipments){this.shipments=shipments;}

    @RabbitListener(queues = "sivion.supply-chain.dispatches")
    public void onDispatch(Map<String,Object> payload) {
        Object value=payload==null?null:payload.get("dispatchId");
        if (!(value instanceof Number number) || number.longValue() <= 0) {
            throw new IllegalArgumentException("Inventory dispatch event must include a positive dispatchId.");
        }
        var shipment=shipments.createForDispatch(number.longValue());
        LOGGER.info("Linked inventory dispatch {} to shipment {}", number.longValue(), shipment.shipment().shipmentNumber());
    }
}
