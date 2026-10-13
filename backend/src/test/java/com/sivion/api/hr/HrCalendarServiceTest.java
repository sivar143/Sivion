package com.sivion.api.hr;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.sivion.api.hr.domain.HrCalendarEntry;
import com.sivion.api.hr.repository.HrCalendarEntryRepository;
import java.time.LocalDate;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

@ExtendWith(MockitoExtension.class)
class HrCalendarServiceTest {
    @Mock
    private HrCalendarEntryRepository repository;

    @InjectMocks
    private HrCalendarService service;

    @Test
    void createsTenantScopedHoliday() {
        HrCalendarEntry entry = new HrCalendarEntry();
        entry.setTitle("  Diwali ");
        entry.setStartsOn(LocalDate.of(2026, 11, 8));
        entry.setEndsOn(LocalDate.of(2026, 11, 8));
        when(repository.save(entry)).thenReturn(entry);

        HrCalendarEntry result = service.create(1L, "hr-user", "holiday", entry);

        assertEquals(1L, result.getTenantId());
        assertEquals("HOLIDAY", result.getType());
        assertEquals("Diwali", result.getTitle());
        assertEquals("hr-user", result.getCreatedBy());
    }

    @Test
    void rejectsReversedDateRange() {
        HrCalendarEntry entry = new HrCalendarEntry();
        entry.setTitle("Planning");
        entry.setStartsOn(LocalDate.of(2026, 11, 10));
        entry.setEndsOn(LocalDate.of(2026, 11, 9));

        assertThrows(ResponseStatusException.class, () -> service.create(1L, "hr-user", "MEETING", entry));
    }

    @Test
    void updateCannotCrossTenantOrEntryType() {
        when(repository.findByIdAndTenantId(2L, 1L)).thenReturn(Optional.empty());
        HrCalendarEntry entry = new HrCalendarEntry();
        entry.setTitle("Updated");
        entry.setStartsOn(LocalDate.of(2026, 11, 10));
        entry.setEndsOn(LocalDate.of(2026, 11, 10));

        assertThrows(ResponseStatusException.class, () -> service.update(1L, 2L, "HOLIDAY", entry));
        verify(repository).findByIdAndTenantId(2L, 1L);
    }
}
