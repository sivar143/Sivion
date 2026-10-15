package com.sivion.api.supplychain.repo;

import com.sivion.api.supplychain.domain.InboundShipmentReceipt;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InboundShipmentReceiptRepository extends JpaRepository<InboundShipmentReceipt, Long> {
    Optional<InboundShipmentReceipt> findByTenantIdAndShipmentIdAndReceiptReference(Long tenantId, Long shipmentId, String receiptReference);
    List<InboundShipmentReceipt> findAllByTenantIdAndShipmentIdOrderByRecordedAtAsc(Long tenantId, Long shipmentId);
}