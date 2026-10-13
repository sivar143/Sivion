package com.sivion.api.hr;

import com.sivion.api.audit.AuditLogFilter;
import com.sivion.api.hr.domain.Employee;
import com.sivion.api.hr.domain.LeaveRequest;
import com.sivion.api.hr.repository.EmployeeRepository;
import com.sivion.api.notifications.NotificationService;
import java.util.List;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Bridges completed HR workflows to the persistent inbox. Notification failures
 * must never roll back or mask a successfully saved HR transaction.
 */
@Component
public class HrNotificationEvents {
    private static final Logger LOGGER = LoggerFactory.getLogger(HrNotificationEvents.class);
    private static final Long TENANT_ID = 1L;
    private static final Set<String> HR_ROLES = Set.of("ADMIN", "HR_ADMIN", "HR_USER");

    private final EmployeeRepository employees;
    private final NotificationService notifications;

    public HrNotificationEvents(EmployeeRepository employees, NotificationService notifications) {
        this.employees = employees;
        this.notifications = notifications;
    }

    public void leaveRequested(LeaveRequest leave) {
        List<Employee> recipients = employees.findByTenantIdOrderByLastNameAscFirstNameAsc(TENANT_ID).stream()
            .filter(employee -> employee.getUsername() != null && !employee.getUsername().isBlank())
            .filter(employee -> HR_ROLES.contains(employee.getRole()))
            .toList();
        for (Employee recipient : recipients) {
            sendSafely(recipient.getUsername(), "Leave request submitted",
                "A leave request for employee #" + leave.getEmployeeId() + " is awaiting review.");
        }
    }

    public void leaveDecision(LeaveRequest leave) {
        Employee recipient = employees.findByTenantIdOrderByLastNameAscFirstNameAsc(TENANT_ID).stream()
            .filter(employee -> leave.getEmployeeId().equals(employee.getId()))
            .filter(employee -> employee.getUsername() != null && !employee.getUsername().isBlank())
            .findFirst()
            .orElse(null);
        if (recipient == null) return;

        sendSafely(recipient.getUsername(), "Leave request " + leave.getStatus().toLowerCase(),
            "Your leave request #" + leave.getId() + " was " + leave.getStatus().toLowerCase() + ".");
    }

    private void sendSafely(String recipient, String title, String message) {
        try {
            notifications.send(TENANT_ID, recipient, title, message);
        } catch (RuntimeException exception) {
            LOGGER.warn("Unable to create notification for recipient {}", recipient, exception);
        }
    }
}
