package com.sivion.api.hr;

import com.sivion.api.hr.domain.*;
import com.sivion.api.hr.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.*;
import java.util.*;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;

@Service
@Transactional
public class HrService {
    // Every HR repository operation is tenant-scoped until request-level tenant context is introduced.
    private static final Long TENANT_ID = 1L;
    private static final Set<String> ALL_ROLES=Set.of( "ADMIN",
         "HR_ADMIN",
         "HR_USER",
         "MANAGER",
         "EMPLOYEE",
         "SALES_MANAGER",
         "SALES_USER",
         "INVENTORY_MANAGER",
         "INVENTORY_USER",
         "WAREHOUSE_MANAGER",
         "WAREHOUSE_USER",
         "PROCUREMENT_MANAGER",
         "FINANCE_MANAGER",
         "FINANCE_USER",
        "MARKETING_USER");
    private final EmployeeRepository employees;
    private final DepartmentRepository departments;
    private final AttendanceRepository attendance;
    private final LeaveRequestRepository leaves;
    private final GoalRepository goals;
    private final PayslipRepository payslips;
    private final DesignationRepository designations;
    private final KeycloakAdminService keycloak;
    public HrService(EmployeeRepository e,
         DepartmentRepository d,
         AttendanceRepository a,
         LeaveRequestRepository l,
         GoalRepository g,
         PayslipRepository p,
         DesignationRepository ds,
        KeycloakAdminService k) {
        employees=e;
        departments=d;
        attendance=a;
        leaves=l;
        goals=g;
        payslips=p;
        designations=ds;
        keycloak=k;
    }
    public List<Employee> employees(Authentication authentication) {
        List<Employee> all = employees.findByTenantIdOrderByLastNameAscFirstNameAsc(TENANT_ID);
        if (hasAnyRole(authentication, "ADMIN", "HR_ADMIN", "HR_USER")) return all;
        Employee current = currentEmployee(authentication, all);
        if (hasAnyRole(authentication, "MANAGER")) {
            return all.stream()
                    .filter(employee -> Objects.equals(employee.getId(), current.getId())
                            || Objects.equals(employee.getManagerId(), current.getId()))
                    .toList();
        }
        return List.of(current);
    }

    private String roleName(Authentication authentication) {
        if (authentication == null) return "";
        return authentication.getAuthorities().stream()
                .map(authority -> authority.getAuthority().replaceFirst("^ROLE_", ""))
                .filter(role -> Set.of("ADMIN", "HR_ADMIN", "HR_USER", "MANAGER", "EMPLOYEE").contains(role))
                .findFirst().orElse("");
    }

    private boolean hasAnyRole(Authentication authentication, String... roles) {
        if (authentication == null) return false;
        Set<String> expected = Set.of(roles);
        return authentication.getAuthorities().stream()
                .map(authority -> authority.getAuthority().replaceFirst("^ROLE_", ""))
                .anyMatch(expected::contains);
    }

    private Employee currentEmployee(Authentication authentication, List<Employee> all) {
        if (authentication == null || authentication.getName() == null) {
            throw new AccessDeniedException("Authenticated employee profile is required");
        }
        String principal = authentication.getName();
        return all.stream()
                .filter(employee -> principal.equals(employee.getKeycloakUserId())
                        || principal.equals(employee.getUsername()))
                .findFirst()
                .orElseThrow(() -> new AccessDeniedException("No HR employee profile is linked to this account"));
    }

    private Set<Long> visibleEmployeeIds(Authentication authentication) {
        List<Employee> all = employees.findByTenantIdOrderByLastNameAscFirstNameAsc(TENANT_ID);
        if (hasAnyRole(authentication, "ADMIN", "HR_ADMIN", "HR_USER")) {
            Set<Long> ids = new HashSet<>();
            all.forEach(employee -> ids.add(employee.getId()));
            return ids;
        }
        Employee current = currentEmployee(authentication, all);
        Set<Long> ids = new HashSet<>();
        ids.add(current.getId());
        if (hasAnyRole(authentication, "MANAGER")) {
            all.stream().filter(employee -> Objects.equals(employee.getManagerId(), current.getId()))
                    .map(Employee::getId).forEach(ids::add);
        }
        return ids;
    }

    private Employee currentEmployee(Authentication authentication) {
        return currentEmployee(authentication, employees.findByTenantIdOrderByLastNameAscFirstNameAsc(TENANT_ID));
    }

    private void requireEmployeeScope(Long employeeId, Authentication authentication) {
        if (!visibleEmployeeIds(authentication).contains(employeeId)) {
            throw new AccessDeniedException("You are not allowed to access this employee's HR records");
        }
    }
    public List<Department> departments() {
        return departments.findByTenantIdOrderByName(TENANT_ID);
    }
    public long activeEmployees() {
        return employees.countByTenantIdAndStatus(TENANT_ID, "ACTIVE");
    }
    public Employee createEmployee(Employee e, boolean admin) {
        prepareEmployee(e);
        if (e.getEmployeeNumber()==null||e.getEmployeeNumber().isBlank()) {
            throw new IllegalArgumentException( "Employee number is required");
        }
        if (employees.existsByTenantIdAndEmployeeNumber(TENANT_ID, e.getEmployeeNumber())) {
            throw new IllegalArgumentException( "Employee number already exists");
        }
        validateDesignationAssignment(e);
        validateAccountRole(e.getRole());
        if (!admin&&!Set.of( "HR_USER", "MANAGER", "EMPLOYEE").contains(e.getRole())) {
            throw new IllegalArgumentException( "HR can assign only HR_USER, MANAGER or EMPLOYEE roles");
        }
        validateOrganizationAssignments(e);
        if(e.getUsername()!=null&&!e.getUsername().isBlank()) {
            validatePassword(e.getPassword());
            String id=keycloak.createUser(e.getUsername(),
                 e.getEmail(),
                 e.getFirstName(),
                 e.getLastName(),
                 e.getPassword(),
                 Boolean.TRUE.equals(e.getTemporaryPassword()),
                 e.getRole(),
                Boolean.TRUE.equals(e.getAccountEnabled()));
            e.setKeycloakUserId(id);
            e.setPassword(null);
            e.setTemporaryPassword(false);
        }
        e.setTenantId(TENANT_ID);
        try {
            return employees.save(e);
        }
catch (RuntimeException ex) {
            if (e.getKeycloakUserId()!=null) {
                keycloak.deleteUser(e.getKeycloakUserId());
            }
            throw ex;
        }
    }
    public Employee updateEmployee(Long id, Employee input, boolean admin) {
        Employee current=employees.findById(id).orElseThrow(()->new IllegalArgumentException( "Employee not found"));
        if (!TENANT_ID.equals(current.getTenantId())) {
            throw new IllegalArgumentException( "Employee not found");
        }
        if (input.getDesignationId()!=null) {
            validateDesignationAssignment(input);
        }
        prepareEmployee(input);
        String role=input.getRole()==null?current.getRole(): input.getRole();
        validateAccountRole(role);
        if (!admin&&!Set.of( "HR_USER", "MANAGER", "EMPLOYEE").contains(role)) {
            throw new IllegalArgumentException( "HR can assign only HR_USER, MANAGER or EMPLOYEE roles");
        }
        validateOrganizationAssignments(input);
        String username=input.getUsername()==null?current.getUsername(): input.getUsername();
        Boolean enabled=input.getAccountEnabled()==null?current.getAccountEnabled(): input.getAccountEnabled();
        if(current.getKeycloakUserId()!=null) {
            keycloak.updateUser(current.getKeycloakUserId(),
                 username,
                 input.getEmail(),
                 input.getFirstName(),
                 input.getLastName(),
                 enabled,
                 input.getPassword(),
                 input.getTemporaryPassword(),
                role);
        }
        else if(username!=null&&!username.isBlank()&&input.getPassword()!=null&&!input.getPassword().isBlank()) {
            validatePassword(input.getPassword());
            String uid=keycloak.createUser(username,
                 input.getEmail(),
                 input.getFirstName(),
                 input.getLastName(),
                 input.getPassword(),
                 Boolean.TRUE.equals(input.getTemporaryPassword()),
                 role,
                Boolean.TRUE.equals(input.getAccountEnabled()));
            current.setKeycloakUserId(uid);
        }
        if (input.getEmployeeNumber()==null||input.getEmployeeNumber().isBlank()) {
            throw new IllegalArgumentException( "Employee number is required");
        }
        if (!Objects.equals(current.getEmployeeNumber(),
             input.getEmployeeNumber())&&employees.existsByTenantIdAndEmployeeNumber(TENANT_ID,
            input.getEmployeeNumber())) {
            throw new IllegalArgumentException( "Employee number already exists");
        }
        current.setEmployeeNumber(input.getEmployeeNumber());
        current.setFirstName(input.getFirstName());
        current.setLastName(input.getLastName());
        current.setEmail(input.getEmail());
        current.setPhone(input.getPhone());
        current.setDepartmentId(input.getDepartmentId());
        current.setDesignation(input.getDesignation());
        current.setDesignationId(input.getDesignationId());
        current.setManagerId(input.getManagerId());
        current.setJoiningDate(input.getJoiningDate());
        current.setStatus(input.getStatus()==null? "ACTIVE": input.getStatus());
        current.setUsername(username);
        current.setRole(role);
        current.setAccountEnabled(enabled);
        current.setPassword(null);
        current.setTemporaryPassword(false);
        return employees.save(current);
    }
    private void prepareEmployee(Employee e) {
        if (e.getStatus()==null) {
            e.setStatus( "ACTIVE");
        }
        if (e.getRole()==null||e.getRole().isBlank()) {
            e.setRole( "EMPLOYEE");
        }
        if (e.getAccountEnabled()==null) {
            e.setAccountEnabled(Boolean.TRUE);
        }
        if (e.getUsername()!=null) {
            e.setUsername(e.getUsername().trim());
        }
    }
    private void validateOrganizationAssignments(Employee e) {
        if(e.getDepartmentId()!=null) {
            Department d = departments.findById(e.getDepartmentId())
                    .orElseThrow(() -> new IllegalArgumentException("Department not found"));
            if (!TENANT_ID.equals(d.getTenantId())) {
                throw new IllegalArgumentException( "Invalid department");
            }
            if (! "ACTIVE".equals(d.getStatus())) {
                throw new IllegalArgumentException( "Inactive department cannot be assigned");
            }
        }
        if(e.getManagerId()!=null) {
            Employee manager = employees.findById(e.getManagerId())
                    .orElseThrow(() -> new IllegalArgumentException("Manager not found"));
            if (!TENANT_ID.equals(manager.getTenantId())) {
                throw new IllegalArgumentException( "Invalid manager");
            }
            if (e.getId()!=null&&e.getId().equals(manager.getId())) {
                throw new IllegalArgumentException( "Employee cannot manage themselves");
            }
            if (! "ACTIVE".equals(manager.getStatus())) {
                throw new IllegalArgumentException( "Inactive employee cannot be assigned as manager");
            }
        }
    }
    private void validateDesignationAssignment(Employee e) {
        if (e.getDesignationId()==null) {
            return;
        }
        Designation d = designations.findById(e.getDesignationId())
                .orElseThrow(() -> new IllegalArgumentException("Designation not found"));
        if (!TENANT_ID.equals(d.getTenantId())) {
            throw new IllegalArgumentException( "Invalid designation");
        }
        if (! "ACTIVE".equals(d.getStatus())) {
            throw new IllegalArgumentException( "Inactive designation cannot be assigned");
        }
        if (d.getDepartmentId()!=null&&(e.getDepartmentId()==null||!d.getDepartmentId().equals(e.getDepartmentId()))) {
            throw new IllegalArgumentException( "Designation does not belong to the selected department");
        }
        e.setDesignation(d.getName());
    }
    private void validatePassword(String p) {
        if (p==null||p.length()<8) {
            throw new IllegalArgumentException( "Password must contain at least 8 characters");
        }
    }
    private void validateAccountRole(String role) {
        if (role==null||!ALL_ROLES.contains(role)) {
            throw new IllegalArgumentException( "Unsupported staff role: "+role);
        }
    }
    public Department createDepartment(Department d) {
        normalizeDepartment(d);
        d.setTenantId(TENANT_ID);
        validateDepartment(d, null);
        return departments.save(d);
    }
    public Department updateDepartment(Long id, Department input) {
        Department current = departments.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Department not found"));
        if (!TENANT_ID.equals(current.getTenantId())) {
            throw new IllegalArgumentException( "Department not found");
        }
        normalizeDepartment(input);
        current.setCode(input.getCode());
        current.setName(input.getName());
        current.setParentId(input.getParentId());
        current.setStatus(input.getStatus());
        validateDepartment(current, id);
        return departments.save(current);
    }
    private void normalizeDepartment(Department d) {
        if (d.getCode()!=null) {
            d.setCode(d.getCode().trim().toUpperCase(Locale.ROOT));
        }
        if (d.getName()!=null) {
            d.setName(d.getName().trim());
        }
        if (d.getStatus()==null||d.getStatus().isBlank()) {
            d.setStatus( "ACTIVE");
        }
        else d.setStatus(d.getStatus().trim().toUpperCase(Locale.ROOT));
    }
    private void validateDepartment(Department d, Long currentId) {
        if (d.getCode()==null||d.getCode().isBlank()) {
            throw new IllegalArgumentException( "Department code is required");
        }
        if (d.getName()==null||d.getName().isBlank()) {
            throw new IllegalArgumentException( "Department name is required");
        }
        if (!d.getStatus().equals( "ACTIVE")&&!d.getStatus().equals( "INACTIVE")) {
            throw new IllegalArgumentException( "Department status must be ACTIVE or INACTIVE");
        }
        if (currentId==null?departments.existsByTenantIdAndCode(TENANT_ID,
             d.getCode()): departments.existsByTenantIdAndCodeAndIdNot(TENANT_ID,
             d.getCode(),
            currentId)) {
            throw new IllegalArgumentException( "Department code already exists");
        }
        if(d.getParentId()!=null) {
            Department parent = departments.findById(d.getParentId())
                    .orElseThrow(() -> new IllegalArgumentException("Parent department not found"));
            if (!TENANT_ID.equals(parent.getTenantId())) {
                throw new IllegalArgumentException( "Invalid parent department");
            }
            if (currentId!=null&&currentId.equals(parent.getId())) {
                throw new IllegalArgumentException( "A department cannot be its own parent");
            }
            if(currentId!=null) {
                Long ancestor=parent.getParentId();
                while(ancestor!=null) {
                    if (currentId.equals(ancestor)) {
                        throw new IllegalArgumentException( "Department hierarchy cannot contain cycles");
                    }
                    Department next = departments.findById(ancestor)
                            .orElseThrow(() -> new IllegalArgumentException("Invalid parent hierarchy"));
                    if (!TENANT_ID.equals(next.getTenantId())) {
                        throw new IllegalArgumentException( "Invalid parent hierarchy");
                    }
                    ancestor=next.getParentId();
                }
            }
        }
    }
    public Attendance markAttendance(Attendance a, Authentication authentication) {
        validateEmployeeTenant(a.getEmployeeId());
        requireEmployeeScope(a.getEmployeeId(), authentication);
        a.setTenantId(TENANT_ID);
        return attendance.findByTenantIdAndEmployeeIdAndAttendanceDate(
                    TENANT_ID,
                    a.getEmployeeId(),
                    a.getAttendanceDate()
            ).map(existing -> {
                existing.setStatus(a.getStatus());
                existing.setCheckIn(a.getCheckIn());
                existing.setCheckOut(a.getCheckOut());
                return attendance.save(existing);
            })
            .orElseGet(() -> attendance.save(a));
    }
    public List<Attendance> attendance(LocalDate from, LocalDate to, Authentication authentication) {
        Set<Long> visibleIds = visibleEmployeeIds(authentication);
        return attendance.findByTenantIdAndAttendanceDateBetweenOrderByAttendanceDateDesc(TENANT_ID, from, to)
                .stream().filter(record -> visibleIds.contains(record.getEmployeeId())).toList();
    }
    public List<LeaveRequest> leaves(Authentication authentication) {
        Set<Long> visibleIds = visibleEmployeeIds(authentication);
        return leaves.findByTenantIdOrderByCreatedAtDesc(TENANT_ID)
                .stream().filter(record -> visibleIds.contains(record.getEmployeeId())).toList();
    }
    public LeaveRequest requestLeave(LeaveRequest l, Authentication authentication) {
        validateEmployeeTenant(l.getEmployeeId());
        if (!hasAnyRole(authentication, "ADMIN", "HR_ADMIN", "HR_USER")) {
            Employee current = currentEmployee(authentication);
            if (!Objects.equals(current.getId(), l.getEmployeeId())) {
                throw new AccessDeniedException("You can request leave only for your own employee profile");
            }
        }
        l.setTenantId(TENANT_ID);
        l.setStatus( "PENDING");
        return leaves.save(l);
    }
    public LeaveRequest updateLeave(Long id, String status, Long approver, Authentication authentication) {
        LeaveRequest l=leaves.findById(id).orElseThrow(()->new IllegalArgumentException( "Leave request not found"));
        if (!l.getTenantId().equals(TENANT_ID)) {
            throw new IllegalArgumentException( "Invalid tenant");
        }
        if (!Set.of("APPROVED", "REJECTED").contains(status)) {
            throw new IllegalArgumentException("Leave status must be APPROVED or REJECTED");
        }
        if ("MANAGER".equals(roleName(authentication)) && !hasAnyRole(authentication, "ADMIN", "HR_ADMIN", "HR_USER")) {
            Employee manager = currentEmployee(authentication);
            if (Objects.equals(manager.getId(), l.getEmployeeId())) {
                throw new AccessDeniedException("Managers cannot approve their own leave requests");
            }
            requireEmployeeScope(l.getEmployeeId(), authentication);
        }
        if (!hasAnyRole(authentication, "ADMIN", "HR_ADMIN", "HR_USER", "MANAGER")) {
            throw new AccessDeniedException("You are not allowed to approve leave requests");
        }
        if (!"PENDING".equals(l.getStatus())) {
            throw new IllegalArgumentException("Only pending leave requests can be updated");
        }
        l.setStatus(status);
        l.setApprovedBy(approver);
        return leaves.save(l);
    }
    public List<Goal> goals(Authentication authentication) {
        Set<Long> visibleIds = visibleEmployeeIds(authentication);
        return goals.findByTenantIdOrderByDueDateAsc(TENANT_ID)
                .stream().filter(goal -> visibleIds.contains(goal.getEmployeeId())).toList();
    }
    public Goal createGoal(Goal g, Authentication authentication) {
        validateEmployeeTenant(g.getEmployeeId());
        requireEmployeeScope(g.getEmployeeId(), authentication);
        g.setTenantId(TENANT_ID);
        return goals.save(g);
    }
    public List<Designation> designations() {
        return designations.findByTenantIdOrderByName(TENANT_ID);
    }
    public Designation saveDesignation(Designation d) {
        normalizeDesignation(d);
        d.setTenantId(TENANT_ID);
        validateDesignation(d, null);
        return designations.save(d);
    }
    public Designation updateDesignation(Long id, Designation input) {
        Designation c=designations.findById(id).orElseThrow(()->new IllegalArgumentException( "Designation not found"));
        if (!TENANT_ID.equals(c.getTenantId())) {
            throw new IllegalArgumentException( "Designation not found");
        }
        normalizeDesignation(input);
        c.setCode(input.getCode());
        c.setName(input.getName());
        c.setDescription(input.getDescription());
        c.setDepartmentId(input.getDepartmentId());
        c.setStatus(input.getStatus());
        validateDesignation(c, id);
        return designations.save(c);
    }
    private void normalizeDesignation(Designation d) {
        if (d.getCode()!=null) {
            d.setCode(d.getCode().trim().toUpperCase(Locale.ROOT));
        }
        if (d.getName()!=null) {
            d.setName(d.getName().trim());
        }
        if (d.getDescription()!=null) {
            d.setDescription(d.getDescription().trim());
        }
        if (d.getStatus()==null||d.getStatus().isBlank()) {
            d.setStatus( "ACTIVE");
        }
        else d.setStatus(d.getStatus().trim().toUpperCase(Locale.ROOT));
    }
    private void validateDesignation(Designation d, Long id) {
        if (d.getCode()==null||d.getCode().isBlank()) {
            throw new IllegalArgumentException( "Designation code is required");
        }
        if (d.getName()==null||d.getName().isBlank()) {
            throw new IllegalArgumentException( "Designation name is required");
        }
        if (!Set.of( "ACTIVE", "INACTIVE").contains(d.getStatus())) {
            throw new IllegalArgumentException( "Invalid designation status");
        }
        if (id==null?designations.existsByTenantIdAndCode(TENANT_ID,
             d.getCode()): designations.existsByTenantIdAndCodeAndIdNot(TENANT_ID,
             d.getCode(),
            id)) {
            throw new IllegalArgumentException( "Designation code already exists");
        }
        if(d.getDepartmentId()!=null) {
            Department dep = departments.findById(d.getDepartmentId())
                    .orElseThrow(() -> new IllegalArgumentException("Department not found"));
            if (!TENANT_ID.equals(dep.getTenantId())) {
                throw new IllegalArgumentException( "Invalid department");
            }
        }
    }
    public List<Payslip> payslips() {
        return payslips.findByTenantIdOrderByPeriodEndDesc(TENANT_ID);
    }
    public Payslip savePayslip(Payslip p) {
        validatePayslip(p);
        p.setTenantId(TENANT_ID);
        if (p.getNetPay()==null) {
            p.setNetPay(p.getGrossPay().subtract(p.getDeductions()==null?java.math.BigDecimal.ZERO: p.getDeductions()));
        }
        p.touch();
        return payslips.save(p);
    }
    public Payslip updatePayslip(Long id, Payslip input) {
        Payslip current=payslips.findById(id).orElseThrow(()->new IllegalArgumentException( "Payslip not found"));
        if (!TENANT_ID.equals(current.getTenantId())) {
            throw new IllegalArgumentException( "Payslip not found");
        }
        validatePayslip(input);
        current.setEmployeeId(input.getEmployeeId());
        current.setPeriodStart(input.getPeriodStart());
        current.setPeriodEnd(input.getPeriodEnd());
        current.setGrossPay(input.getGrossPay());
        current.setDeductions(input.getDeductions());
        BigDecimal deductions = input.getDeductions() == null
                ? BigDecimal.ZERO
                : input.getDeductions();

        BigDecimal netPay = input.getNetPay() == null
                ? input.getGrossPay().subtract(deductions)
                : input.getNetPay();

        current.setNetPay(netPay);
        current.setStatus(input.getStatus()==null? "DRAFT": input.getStatus());
        current.setNotes(input.getNotes());
        current.touch();
        return payslips.save(current);
    }
    // Keep employee lookups tenant-scoped so payroll and HR operations cannot cross tenant boundaries.
    // Keep employee lookups tenant-scoped so payroll and HR operations cannot cross tenant boundaries.
    private void validateEmployeeTenant(Long employeeId) {
        if (employeeId==null) {
            throw new IllegalArgumentException( "Employee is required");
        }
        Employee employee = employees.findById(employeeId)
                .orElseThrow(() -> new IllegalArgumentException("Employee not found"));
        if (!TENANT_ID.equals(employee.getTenantId())) {
            throw new IllegalArgumentException( "Employee not found");
        }
    }
    // Validate the financial period and amounts before persisting payroll data.
    // Validate the financial period and amounts before persisting payroll data.
    private void validatePayslip(Payslip p) {
        validateEmployeeTenant(p.getEmployeeId());
        if (p.getPeriodStart()==null||p.getPeriodEnd()==null||p.getPeriodEnd().isBefore(p.getPeriodStart())) {
            throw new IllegalArgumentException( "Invalid payslip period");
        }
        if (p.getGrossPay()==null||p.getGrossPay().signum()<0) {
            throw new IllegalArgumentException( "Gross pay cannot be negative");
        }
        if (p.getDeductions()==null) {
            p.setDeductions(java.math.BigDecimal.ZERO);
        }
        if (p.getDeductions().signum()<0) {
            throw new IllegalArgumentException( "Deductions cannot be negative");
        }
        if (p.getGrossPay().compareTo(p.getDeductions())<0) {
            throw new IllegalArgumentException( "Deductions cannot exceed gross pay");
        }
    }
}
