package com.sivion.api.hr;

import com.sivion.api.hr.domain.Attendance;
import com.sivion.api.hr.domain.Department;
import com.sivion.api.hr.domain.Designation;
import com.sivion.api.hr.domain.Employee;
import com.sivion.api.hr.domain.Goal;
import com.sivion.api.hr.domain.LeaveRequest;
import com.sivion.api.hr.domain.Payslip;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/hr")
public class HrController {

    private final HrService service;

    public HrController(HrService service) {
        this.service = service;
    }

    @GetMapping("/dashboard")
    @PreAuthorize("hasAnyRole('ADMIN','HR_ADMIN','HR_USER','MANAGER','EMPLOYEE')")
    public Map<String, Object> dashboard() {
        return Map.of(
                "activeEmployees", service.activeEmployees(),
                "employees", service.employees().size(),
                "departments", service.departments().size());
    }

    @GetMapping("/employees")
    @PreAuthorize("hasAnyRole('ADMIN','HR_ADMIN','HR_USER','MANAGER')")
    public List<Employee> employees() {
        return service.employees();
    }

    @PostMapping("/employees")
    @PreAuthorize("hasAnyRole('ADMIN','HR_ADMIN','HR_USER')")
    public Employee createEmployee(@RequestBody Employee employee, Authentication authentication) {
        return service.createEmployee(employee, isAdmin(authentication));
    }

    @PutMapping("/employees/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','HR_ADMIN','HR_USER')")
    public Employee updateEmployee(
            @PathVariable Long id,
            @RequestBody Employee employee,
            Authentication authentication) {
        return service.updateEmployee(id, employee, isAdmin(authentication));
    }

    @GetMapping("/departments")
    @PreAuthorize("hasAnyRole('ADMIN','HR_ADMIN','HR_USER','MANAGER')")
    public List<Department> departments() {
        return service.departments();
    }

    @PostMapping("/departments")
    @PreAuthorize("hasAnyRole('ADMIN','HR_ADMIN','HR_USER')")
    public Department createDepartment(@RequestBody Department department) {
        return service.createDepartment(department);
    }

    @PutMapping("/departments/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','HR_ADMIN','HR_USER')")
    public Department updateDepartment(@PathVariable Long id, @RequestBody Department department) {
        return service.updateDepartment(id, department);
    }

    @GetMapping("/attendance")
    @PreAuthorize("hasAnyRole('ADMIN','HR_ADMIN','HR_USER','MANAGER','EMPLOYEE')")
    public List<Attendance> attendance(
            @RequestParam(required = false) LocalDate from,
            @RequestParam(required = false) LocalDate to) {
        LocalDate end = to == null ? LocalDate.now() : to;
        LocalDate start = from == null ? end.minusDays(30) : from;
        return service.attendance(start, end);
    }

    @PostMapping("/attendance")
    @PreAuthorize("hasAnyRole('ADMIN','HR_ADMIN','HR_USER','MANAGER','EMPLOYEE')")
    public Attendance markAttendance(@RequestBody Attendance attendance) {
        return service.markAttendance(attendance);
    }

    @GetMapping("/leaves")
    @PreAuthorize("hasAnyRole('ADMIN','HR_ADMIN','HR_USER','MANAGER','EMPLOYEE')")
    public List<LeaveRequest> leaves() {
        return service.leaves();
    }

    @PostMapping("/leaves")
    @PreAuthorize("hasAnyRole('ADMIN','HR_ADMIN','MANAGER','EMPLOYEE')")
    public LeaveRequest requestLeave(@RequestBody LeaveRequest leave) {
        return service.requestLeave(leave);
    }

    @PatchMapping("/leaves/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','HR_ADMIN','HR_USER','MANAGER')")
    public LeaveRequest updateLeave(@PathVariable Long id, @RequestParam String status) {
        return service.updateLeave(id, status, null);
    }

    @GetMapping("/goals")
    @PreAuthorize("hasAnyRole('ADMIN','HR_ADMIN','HR_USER','MANAGER','EMPLOYEE')")
    public List<Goal> goals() {
        return service.goals();
    }

    @PostMapping("/goals")
    @PreAuthorize("hasAnyRole('ADMIN','HR_ADMIN','MANAGER','EMPLOYEE')")
    public Goal createGoal(@RequestBody Goal goal) {
        return service.createGoal(goal);
    }

    @GetMapping("/designations")
    @PreAuthorize("hasAnyRole('ADMIN','HR_ADMIN','HR_USER')")
    public List<Designation> designations() {
        return service.designations();
    }

    @PostMapping("/designations")
    @PreAuthorize("hasAnyRole('ADMIN','HR_ADMIN','HR_USER')")
    public Designation createDesignation(@RequestBody Designation designation) {
        return service.saveDesignation(designation);
    }

    @PutMapping("/designations/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','HR_ADMIN','HR_USER')")
    public Designation updateDesignation(
            @PathVariable Long id,
            @RequestBody Designation designation) {
        return service.updateDesignation(id, designation);
    }

    @GetMapping("/payslips")
    @PreAuthorize("hasAnyRole('ADMIN','HR_ADMIN','HR_USER')")
    public List<Payslip> payslips() {
        return service.payslips();
    }

    @PostMapping("/payslips")
    @PreAuthorize("hasAnyRole('ADMIN','HR_ADMIN','HR_USER')")
    public Payslip createPayslip(@RequestBody Payslip payslip) {
        return service.savePayslip(payslip);
    }

    @PutMapping("/payslips/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','HR_ADMIN','HR_USER')")
    public Payslip updatePayslip(@PathVariable Long id, @RequestBody Payslip payslip) {
        return service.updatePayslip(id, payslip);
    }

    private boolean isAdmin(Authentication authentication) {
        return authentication.getAuthorities().stream()
                .anyMatch(authority -> "ROLE_ADMIN".equals(authority.getAuthority()));
    }
}
