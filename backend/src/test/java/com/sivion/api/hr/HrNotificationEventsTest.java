package com.sivion.api.hr;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.sivion.api.hr.domain.Employee;
import com.sivion.api.hr.domain.LeaveRequest;
import com.sivion.api.hr.repository.EmployeeRepository;
import com.sivion.api.notifications.NotificationService;
import java.util.List;
import org.junit.jupiter.api.Test;

class HrNotificationEventsTest {
    @Test
    void leaveRequestNotifiesHrAccountsWithoutExposingTheLeaveReason() {
        EmployeeRepository repository = org.mockito.Mockito.mock(EmployeeRepository.class);
        NotificationService notifications = org.mockito.Mockito.mock(NotificationService.class);
        Employee hr = new Employee();
        hr.setId(4L);
        hr.setUsername("hr-user");
        hr.setRole("HR_USER");
        Employee employee = new Employee();
        employee.setId(5L);
        employee.setUsername("employee");
        employee.setRole("EMPLOYEE");
        when(repository.findByTenantIdOrderByLastNameAscFirstNameAsc(1L)).thenReturn(List.of(hr, employee));
        LeaveRequest leave = new LeaveRequest();
        leave.setEmployeeId(5L);
        leave.setReason("Private medical details");

        new HrNotificationEvents(repository, notifications).leaveRequested(leave);

        verify(notifications).send(1L, "hr-user", "Leave request submitted",
            "A leave request for employee #5 is awaiting review.");
        org.mockito.Mockito.verifyNoMoreInteractions(notifications);
    }
}
