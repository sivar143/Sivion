package com.sivion.api.hr;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.sivion.api.hr.domain.Employee;
import java.util.List;
import org.junit.jupiter.api.Test;

class HrControllerTest {

    @Test
    void payrollEmployeesAllowsNullableLegacyFields() {
        HrService service = mock(HrService.class);
        Employee employee = new Employee();
        employee.setEmployeeNumber("E-100");
        employee.setFirstName(null);
        employee.setLastName(null);
        when(service.payrollEmployees()).thenReturn(List.of(employee));

        HrController controller = new HrController(service);

        List<HrController.PayrollEmployeeResponse> result = controller.payrollEmployees();

        assertEquals(1, result.size());
        assertEquals("E-100", result.get(0).employeeNumber());
        assertNull(result.get(0).firstName());
        assertNull(result.get(0).lastName());
    }
}
