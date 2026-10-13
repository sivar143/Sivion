package com.sivion.api.hr.repository;

import com.sivion.api.hr.domain.HrCalendarEntry;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface HrCalendarEntryRepository extends JpaRepository<HrCalendarEntry, Long> {
    List<HrCalendarEntry> findByTenantIdAndTypeOrderByStartsOnAsc(Long tenantId, String type);
    Optional<HrCalendarEntry> findByIdAndTenantId(Long id, Long tenantId);
}
