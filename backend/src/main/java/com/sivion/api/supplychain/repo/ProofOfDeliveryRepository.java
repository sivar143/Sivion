package com.sivion.api.supplychain.repo;
import com.sivion.api.supplychain.domain.ProofOfDelivery;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
public interface ProofOfDeliveryRepository extends JpaRepository<ProofOfDelivery, Long> {
 Optional<ProofOfDelivery> findByShipmentId(Long shipmentId);
}
