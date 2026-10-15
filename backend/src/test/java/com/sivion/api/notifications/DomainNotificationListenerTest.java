package com.sivion.api.notifications;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doThrow;

import com.sivion.api.hr.domain.Employee;
import com.sivion.api.hr.repository.EmployeeRepository;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.web.server.ResponseStatusException;

class DomainNotificationListenerTest {
    @Test
    void notifiesSupplyChainStakeholdersAboutShipmentStatus() {
        EmployeeRepository repository = org.mockito.Mockito.mock(EmployeeRepository.class);
        NotificationService notifications = org.mockito.Mockito.mock(NotificationService.class);
        Employee warehouseUser = new Employee();
        warehouseUser.setUsername("warehouse-user");
        warehouseUser.setRole("WAREHOUSE_USER");
        Employee salesUser = new Employee();
        salesUser.setUsername("sales-user");
        salesUser.setRole("SALES_USER");
        when(repository.findByTenantIdOrderByLastNameAscFirstNameAsc(1L))
            .thenReturn(List.of(warehouseUser, salesUser));

        new DomainNotificationListener(repository, notifications).onDomainEvent(
            "supply-chain.shipment.status", Map.of("eventId", 88L, "shipmentId", 5L, "shipmentNumber", "DSP-100", "status", "IN_TRANSIT")
        );

        verify(notifications).send(1L, "warehouse-user", "Shipment status updated",
            "shipmentNumber #DSP-100 has a new shipment milestone.");
        verify(notifications).send(1L, "sales-user", "Shipment status updated",
            "DSP-100 has a new shipment milestone.");
    }

    @Test
    void propagatesNotificationFailureSoRabbitCanRetryAndDeadLetter() {
        EmployeeRepository repository = org.mockito.Mockito.mock(EmployeeRepository.class);
        NotificationService notifications = org.mockito.Mockito.mock(NotificationService.class);
        Employee employee = new Employee();
        employee.setUsername("warehouse-user");
        employee.setRole("WAREHOUSE_USER");
        when(repository.findByTenantIdOrderByLastNameAscFirstNameAsc(1L)).thenReturn(List.of(employee));
        doThrow(new IllegalStateException("database unavailable"))
            .when(notifications).send(1L, "warehouse-user", "Shipment status updated",
                "DSP-100 has a new shipment milestone.");

        assertThrows(IllegalStateException.class, () ->
            new DomainNotificationListener(repository, notifications).onDomainEvent(
                "supply-chain.shipment.status", Map.of("shipmentNumber", "DSP-100", "status", "IN_TRANSIT")
            ));
    }

    @Test
    void routesConfirmedSalesOrdersToInventoryUsers() {
        EmployeeRepository repository = org.mockito.Mockito.mock(EmployeeRepository.class);
        NotificationService notifications = org.mockito.Mockito.mock(NotificationService.class);
        Employee inventoryUser = new Employee();
        inventoryUser.setUsername("stock-user");
        inventoryUser.setRole("INVENTORY_USER");
        Employee salesUser = new Employee();
        salesUser.setUsername("sales-user");
        salesUser.setRole("SALES_USER");
        when(repository.findByTenantIdOrderByLastNameAscFirstNameAsc(1L))
            .thenReturn(List.of(inventoryUser, salesUser));

        new DomainNotificationListener(repository, notifications).onDomainEvent(
            "sales.order.confirmed", Map.of("salesOrderId", 42L)
        );

        verify(notifications).sendOnce(1L, "sales.order.confirmed:salesOrderId:42", "stock-user", "Sales order confirmed",
            "salesOrderId #42 is confirmed and ready for inventory processing.");
        org.mockito.Mockito.verifyNoMoreInteractions(notifications);
    }
}
