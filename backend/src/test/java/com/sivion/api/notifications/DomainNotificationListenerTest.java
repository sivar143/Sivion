package com.sivion.api.notifications;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.sivion.api.hr.domain.Employee;
import com.sivion.api.hr.repository.EmployeeRepository;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class DomainNotificationListenerTest {
    @Test
    void routesConfirmedSalesOrdersToInventoryUsers() {
        EmployeeRepository repository = org.mockito.Mockito.mock(EmployeeRepository.class);
        NotificationService notifications = org.mockito.Mockito.mock(NotificationService.class);
        Employee inventoryUser = new Employee();
        inventoryUser.setId(1L);
        inventoryUser.setUsername("stock-user");
        inventoryUser.setRole("INVENTORY_USER");
        Employee salesUser = new Employee();
        salesUser.setId(2L);
        salesUser.setUsername("sales-user");
        salesUser.setRole("SALES_USER");
        when(repository.findByTenantIdOrderByLastNameAscFirstNameAsc(1L))
            .thenReturn(List.of(inventoryUser, salesUser));

        new DomainNotificationListener(repository, notifications).onDomainEvent(
            "sales.order.confirmed", Map.of("salesOrderId", 42L)
        );

        verify(notifications).send(1L, "stock-user", "Sales order confirmed",
            "salesOrderId #42 is confirmed and ready for inventory processing.");
        org.mockito.Mockito.verifyNoMoreInteractions(notifications);
    }
}
