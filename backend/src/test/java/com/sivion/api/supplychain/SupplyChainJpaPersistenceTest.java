package com.sivion.api.supplychain;

import static org.junit.jupiter.api.Assertions.*;

import com.sivion.api.procurement.domain.GoodsReceipt;
import com.sivion.api.procurement.repo.GoodsReceiptRepository;
import com.sivion.api.supplychain.domain.InboundShipmentReceipt;
import com.sivion.api.supplychain.domain.Shipment;
import com.sivion.api.supplychain.repo.InboundShipmentReceiptRepository;
import com.sivion.api.supplychain.repo.ShipmentRepository;
import java.math.BigDecimal;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@DataJpaTest(properties = {
    "spring.jpa.hibernate.ddl-auto=create-drop",
    "spring.jpa.database-platform=org.hibernate.dialect.H2Dialect"
})
@EntityScan(basePackageClasses = {Shipment.class, GoodsReceipt.class})
@EnableJpaRepositories(basePackageClasses = {ShipmentRepository.class, GoodsReceiptRepository.class})
class SupplyChainJpaPersistenceTest {
    @Autowired private ShipmentRepository shipments;
    @Autowired private GoodsReceiptRepository goodsReceipts;
    @Autowired private InboundShipmentReceiptRepository inboundReceipts;

    @Test
    void persistsShipmentGoodsReceiptAndPartialReceiptLinkInAnIsolatedDatabase() {
        Shipment shipment = new Shipment();
        shipment.setShipmentNumber("IN-DB-TEST-1");
        shipment.setDirection("INBOUND");
        shipment.setStatus("PARTIALLY_RECEIVED");
        shipment.setPublicTrackingToken("012345678901234567890123456789012345678901234567");
        Shipment savedShipment = shipments.saveAndFlush(shipment);

        GoodsReceipt receipt = new GoodsReceipt();
        receipt.setTenantId(1L);
        receipt.setReceiptNumber("GRN-DB-TEST-1");
        receipt.setPurchaseOrderId(22L);
        receipt.setProductId(31L);
        receipt.setWarehouseId(4L);
        receipt.setQuantity(new BigDecimal("2.5000"));
        receipt.setStatus("RECEIVED");
        GoodsReceipt savedReceipt = goodsReceipts.saveAndFlush(receipt);

        InboundShipmentReceipt link = inboundReceipts.saveAndFlush(new InboundShipmentReceipt(
            savedShipment.getId(), savedReceipt.getId(), savedReceipt.getReceiptNumber(), "SUP-REC-1",
            31L, 4L, new BigDecimal("2.5000"), 99L, "RAW-DB-TEST-1", false));

        Optional<InboundShipmentReceipt> reloaded = inboundReceipts
            .findByTenantIdAndShipmentIdAndReceiptReference(1L, savedShipment.getId(), "SUP-REC-1");

        assertTrue(reloaded.isPresent());
        assertEquals(savedReceipt.getId(), reloaded.get().getGoodsReceiptId());
        assertEquals(new BigDecimal("2.5000"), reloaded.get().getQuantity());
        assertEquals("RAW-DB-TEST-1", reloaded.get().getLotCode());
        assertEquals("GRN-DB-TEST-1", goodsReceipts
            .findByReceiptNumberAndTenantId("GRN-DB-TEST-1", 1L).orElseThrow().getReceiptNumber());
        assertTrue(shipments.findByIdAndTenantId(savedShipment.getId(), 1L).isPresent());
    }
}
