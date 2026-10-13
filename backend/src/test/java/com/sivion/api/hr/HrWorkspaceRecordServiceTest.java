package com.sivion.api.hr;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import com.sivion.api.hr.domain.HrWorkspaceRecord;
import com.sivion.api.hr.repository.HrWorkspaceRecordRepository;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.web.server.ResponseStatusException;

@ExtendWith(MockitoExtension.class)
class HrWorkspaceRecordServiceTest {
    @Mock
    private HrWorkspaceRecordRepository repository;
    @Mock
    private HrService hrService;
    @InjectMocks
    private HrWorkspaceRecordService service;

    @Test
    void rejectsPerformanceScoresOutsideOneToFive() {
        HrWorkspaceRecord record = new HrWorkspaceRecord();
        record.setTitle("Quarterly review");
        record.setEmployeeId(1L);
        record.setScore(6);
        var auth = new UsernamePasswordAuthenticationToken("hr", "n/a", List.of(new SimpleGrantedAuthority("ROLE_HR_USER")));

        assertThrows(ResponseStatusException.class, () -> service.create(1L, "hr", "PERFORMANCE", record, auth));
    }

    @Test
    void rejectsEmployeeReviewOutsideVisibleScope() {
        HrWorkspaceRecord record = new HrWorkspaceRecord();
        record.setTitle("Quarterly review");
        record.setEmployeeId(9L);
        record.setScore(4);
        var auth = new UsernamePasswordAuthenticationToken("manager", "n/a", List.of(new SimpleGrantedAuthority("ROLE_MANAGER")));
        when(hrService.employees(auth)).thenReturn(List.of());

        assertThrows(AccessDeniedException.class, () -> service.create(1L, "manager", "PERFORMANCE", record, auth));
    }

    @Test
    void learningResourcesAreVisibleWithoutEmployeeAssignment() {
        HrWorkspaceRecord record = new HrWorkspaceRecord();
        record.setType("LEARNING");
        when(repository.findByTenantIdAndTypeOrderByCreatedAtDesc(1L, "LEARNING")).thenReturn(List.of(record));
        var auth = new UsernamePasswordAuthenticationToken("employee", "n/a", List.of(new SimpleGrantedAuthority("ROLE_EMPLOYEE")));

        assertEquals(1, service.list(1L, "LEARNING", auth).size());
    }
}
