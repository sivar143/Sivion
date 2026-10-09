package com.sivion.api.hr;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.mockito.ArgumentMatchers.any;

import com.sivion.api.hr.domain.Attendance;
import com.sivion.api.hr.domain.Department;
import com.sivion.api.hr.domain.Designation;
import com.sivion.api.hr.domain.Employee;
import com.sivion.api.hr.domain.Goal;
import com.sivion.api.hr.domain.LeaveRequest;
import com.sivion.api.hr.domain.Payslip;
import com.sivion.api.hr.repository.AttendanceRepository;
import com.sivion.api.hr.repository.DepartmentRepository;
import com.sivion.api.hr.repository.DesignationRepository;
import com.sivion.api.hr.repository.EmployeeRepository;
import com.sivion.api.hr.repository.GoalRepository;
import com.sivion.api.hr.repository.LeaveRequestRepository;
import com.sivion.api.hr.repository.PayslipRepository;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.util.ReflectionTestUtils;

class HrServiceAccessScopeTest {
    @Test
    void inactiveEmployeeIsCreatedWithLoginDisabled() {
        EmployeeRepository employeeRepo = mock(EmployeeRepository.class);
        KeycloakAdminService keycloak = mock(KeycloakAdminService.class);
        when(employeeRepo.existsByTenantIdAndEmployeeNumber(1L, "E900")).thenReturn(false);
        when(keycloak.createUser("staff900", "staff900@example.com", "Staff", "Inactive",
                "initialPass123", false, "EMPLOYEE", false)).thenReturn("kc-900");
        when(employeeRepo.save(any(Employee.class))).thenAnswer(invocation -> invocation.getArgument(0));

        HrService service = new HrService(employeeRepo, mock(DepartmentRepository.class),
                mock(AttendanceRepository.class), mock(LeaveRequestRepository.class),
                mock(GoalRepository.class), mock(PayslipRepository.class),
                mock(DesignationRepository.class), keycloak);
        Employee employee = new Employee();
        employee.setEmployeeNumber("E900");
        employee.setFirstName("Staff");
        employee.setLastName("Inactive");
        employee.setEmail("staff900@example.com");
        employee.setUsername("staff900");
        employee.setPassword("initialPass123");
        employee.setRole("EMPLOYEE");
        employee.setStatus("INACTIVE");
        employee.setAccountEnabled(true);

        Employee saved = service.createEmployee(employee, false);

        assertEquals("INACTIVE", saved.getStatus());
        assertEquals(false, saved.getAccountEnabled());
        verify(keycloak).createUser("staff900", "staff900@example.com", "Staff", "Inactive",
                "initialPass123", false, "EMPLOYEE", false);
    }
    @Test
    void employeeOnlyReceivesOwnHrRecords() {
        EmployeeRepository employeeRepo = mock(EmployeeRepository.class);
        DepartmentRepository departmentRepo = mock(DepartmentRepository.class);
        AttendanceRepository attendanceRepo = mock(AttendanceRepository.class);
        LeaveRequestRepository leaveRepo = mock(LeaveRequestRepository.class);
        GoalRepository goalRepo = mock(GoalRepository.class);
        PayslipRepository payslipRepo = mock(PayslipRepository.class);
        DesignationRepository designationRepo = mock(DesignationRepository.class);

        Employee self = employee(10L, "kc-self", "self");
        Employee coworker = employee(20L, "kc-other", "other");
        when(employeeRepo.findByTenantIdOrderByLastNameAscFirstNameAsc(1L)).thenReturn(List.of(self, coworker));

        Attendance ownAttendance = attendance(10L);
        Attendance coworkerAttendance = attendance(20L);
        when(attendanceRepo.findByTenantIdAndAttendanceDateBetweenOrderByAttendanceDateDesc(
                org.mockito.ArgumentMatchers.eq(1L),
                org.mockito.ArgumentMatchers.any(LocalDate.class),
                org.mockito.ArgumentMatchers.any(LocalDate.class)))
                .thenReturn(List.of(ownAttendance, coworkerAttendance));

        LeaveRequest ownLeave = leave(10L);
        LeaveRequest coworkerLeave = leave(20L);
        when(leaveRepo.findByTenantIdOrderByCreatedAtDesc(1L)).thenReturn(List.of(ownLeave, coworkerLeave));

        Goal ownGoal = goal(10L);
        Goal coworkerGoal = goal(20L);
        when(goalRepo.findByTenantIdOrderByDueDateAsc(1L)).thenReturn(List.of(ownGoal, coworkerGoal));

        HrService service = new HrService(employeeRepo, departmentRepo, attendanceRepo, leaveRepo,
                goalRepo, payslipRepo, designationRepo, mock(KeycloakAdminService.class));
        var authentication = new UsernamePasswordAuthenticationToken(
                "kc-self", "ignored", List.of(new SimpleGrantedAuthority("ROLE_EMPLOYEE")));

        assertEquals(List.of(self), service.employees(authentication));
        assertEquals(List.of(ownAttendance), service.attendance(
                LocalDate.now().minusDays(1), LocalDate.now(), authentication));
        assertEquals(List.of(ownLeave), service.leaves(authentication));
        assertEquals(List.of(ownGoal), service.goals(authentication));
    }

    @Test
    void managerCanSeeOwnRecordsAndDirectReportsOnly() {
        EmployeeRepository employeeRepo = mock(EmployeeRepository.class);
        Employee manager = employee(1L, "kc-manager", "manager");
        Employee report = employee(2L, "kc-report", "report");
        report.setManagerId(1L);
        Employee unrelated = employee(3L, "kc-unrelated", "unrelated");
        when(employeeRepo.findByTenantIdOrderByLastNameAscFirstNameAsc(1L))
                .thenReturn(List.of(manager, report, unrelated));

        HrService service = new HrService(employeeRepo, mock(DepartmentRepository.class),
                mock(AttendanceRepository.class), mock(LeaveRequestRepository.class),
                mock(GoalRepository.class), mock(PayslipRepository.class),
                mock(DesignationRepository.class), mock(KeycloakAdminService.class));
        var authentication = new UsernamePasswordAuthenticationToken(
                "kc-manager", "ignored", List.of(new SimpleGrantedAuthority("ROLE_MANAGER")));

        assertEquals(List.of(manager, report), service.employees(authentication));
    }

    private static Employee employee(Long id, String keycloakId, String username) {
        Employee employee = new Employee();
        ReflectionTestUtils.setField(employee, "id", id);
        employee.setTenantId(1L);
        employee.setKeycloakUserId(keycloakId);
        employee.setUsername(username);
        employee.setFirstName(username);
        employee.setLastName("Test");
        employee.setEmployeeNumber("E" + id);
        employee.setEmail(username + "@example.com");
        employee.setStatus("ACTIVE");
        return employee;
    }

    private static Attendance attendance(Long employeeId) {
        Attendance value = new Attendance();
        value.setTenantId(1L);
        value.setEmployeeId(employeeId);
        value.setAttendanceDate(LocalDate.now());
        value.setStatus("PRESENT");
        return value;
    }

    private static LeaveRequest leave(Long employeeId) {
        LeaveRequest value = new LeaveRequest();
        value.setTenantId(1L);
        value.setEmployeeId(employeeId);
        value.setLeaveType("ANNUAL");
        value.setStartDate(LocalDate.now());
        value.setEndDate(LocalDate.now());
        value.setStatus("PENDING");
        return value;
    }

    private static Goal goal(Long employeeId) {
        Goal value = new Goal();
        value.setTenantId(1L);
        value.setEmployeeId(employeeId);
        value.setTitle("Test goal");
        value.setStatus("ACTIVE");
        return value;
    }
}
