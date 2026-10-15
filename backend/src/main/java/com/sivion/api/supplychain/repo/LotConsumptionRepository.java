package com.sivion.api.supplychain.repo;
import com.sivion.api.supplychain.domain.LotConsumption;
import com.sivion.api.supplychain.domain.MaterialLot;
import com.sivion.api.supplychain.domain.ProductionLot;
import java.util.*;
import org.springframework.data.jpa.repository.JpaRepository;
public interface LotConsumptionRepository extends JpaRepository<LotConsumption, Long> {
 List<LotConsumption> findAllByMaterialLotOrderByRecordedAtAsc(MaterialLot materialLot);
 List<LotConsumption> findAllByProductionLotOrderByRecordedAtAsc(ProductionLot productionLot);
}
