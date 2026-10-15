package com.sivion.api.supplychain;

import static org.junit.jupiter.api.Assertions.*;

import com.sivion.api.procurement.domain.GoodsReceipt;
import com.sivion.api.procurement.domain.PurchaseOrder;
import com.sivion.api.procurement.domain.PurchaseOrderItem;
import com.sivion.api.supplychain.domain.*;
import java.math.BigDecimal;
import java.util.Map;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.junit.jupiter.api.Test;

/** Exercises Hibernate schema creation and basic persistence using a disposable H2 database. */
class SupplyChainJpaPersistenceTest {
    @Test
    void persistsShipmentGoodsReceiptAndPartialReceiptLinkInAnIsolatedDatabase() {
        StandardServiceRegistry registry = new StandardServiceRegistryBuilder()
            .applySettings(Map.of(
                "hibernate.connection.driver_class", "org.h2.Driver",
                "hibernate.connection.url", "jdbc:h2:mem:sivion_supply_chain;DB_CLOSE_DELAY=-1",
                "hibernate.connection.username", "sa",
                "hibernate.connection.password", "",
                "hibernate.dialect", "org.hibernate.dialect.H2Dialect",
                "hibernate.hbm2ddl.auto", "create-drop",
                "hibernate.show_sql", "false"
            )).build();

        try {
            MetadataSources metadata = new MetadataSources(registry)
                .addAnnotatedClass(Shipment.class)
                .addAnnotatedClass(ShipmentEvent.class)
                .addAnnotatedClass(MaterialLot.class)
                .addAnnotatedClass(ProductionLot.class)
                .addAnnotatedClass(LotConsumption.class)
                .addAnnotatedClass(ProofOfDelivery.class)
                .addAnnotatedClass(QualityInspection.class)
                .addAnnotatedClass(ShipmentReturn.class)
                .addAnnotatedClass(ProductionLotShipment.class)
                .addAnnotatedClass(InboundShipmentReceipt.class)
                .addAnnotatedClass(ManufacturingOrder.class)
                .addAnnotatedClass(GoodsReceipt.class)
                .addAnnotatedClass(PurchaseOrder.class)
                .addAnnotatedClass(PurchaseOrderItem.class);

            try (SessionFactory factory = metadata.buildMetadata().buildSessionFactory();
                 Session session = factory.openSession()) {
                Transaction transaction = session.beginTransaction();

                Shipment shipment = new Shipment();
                shipment.setShipmentNumber("IN-DB-TEST-1");
                shipment.setDirection("INBOUND");
                shipment.setStatus("PARTIALLY_RECEIVED");
                shipment.setPublicTrackingToken("012345678901234567890123456789012345678901234567");
                session.persist(shipment);

                GoodsReceipt receipt = new GoodsReceipt();
                receipt.setTenantId(1L);
                receipt.setReceiptNumber("GRN-DB-TEST-1");
                receipt.setPurchaseOrderId(22L);
                receipt.setProductId(31L);
                receipt.setWarehouseId(4L);
                receipt.setQuantity(new BigDecimal("2.5000"));
                receipt.setStatus("RECEIVED");
                session.persist(receipt);
                session.flush();

                InboundShipmentReceipt link = new InboundShipmentReceipt(
                    shipment.getId(), receipt.getId(), receipt.getReceiptNumber(), "SUP-REC-1",
                    31L, 4L, new BigDecimal("2.5000"), 99L, "RAW-DB-TEST-1", false);
                session.persist(link);
                session.flush();

                Long shipmentId = shipment.getId();
                Long receiptId = receipt.getId();
                Long linkId = link.getId();
                transaction.commit();
                session.clear();

                InboundShipmentReceipt reloaded = session.find(InboundShipmentReceipt.class, linkId);
                assertNotNull(reloaded);
                assertEquals(shipmentId, reloaded.getShipmentId());
                assertEquals(receiptId, reloaded.getGoodsReceiptId());
                assertEquals(new BigDecimal("2.5000"), reloaded.getQuantity());
                assertEquals("RAW-DB-TEST-1", reloaded.getLotCode());
                assertEquals("GRN-DB-TEST-1",
                    session.find(GoodsReceipt.class, receiptId).getReceiptNumber());
                assertEquals("IN-DB-TEST-1",
                    session.find(Shipment.class, shipmentId).getShipmentNumber());
            }
        } finally {
            StandardServiceRegistryBuilder.destroy(registry);
        }
    }
}
