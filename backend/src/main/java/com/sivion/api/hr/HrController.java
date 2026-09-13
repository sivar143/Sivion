package com.sivion.api.hr;

import com.sivion.api.hr.domain.*; import org.springframework.security.access.prepost.PreAuthorize; import org.springframework.web.bind.annotation.*; import java.time.*; import java.util.*;
@RestController @RequestMapping("/api/v1/hr")
public class HrController {
 private final HrService service; public HrController(HrService s){service=s;}
 @GetMapping("/dashboard") @PreAuthorize("hasAnyRole('ADMIN','HR_ADMIN','HR_USER','MANAGER','EMPLOYEE')") public Map<String,Object> dashboard(){return Map.of("activeEmployees",service.activeEmployees(),"employees",service.employees().size(),"departments",service.departments().size());}
 @GetMapping("/employees") @PreAuthorize("hasAnyRole('ADMIN','HR_ADMIN','HR_USER','MANAGER')") public List<Employee> employees(){return service.employees();}
 @PostMapping("/employees") @PreAuthorize("hasAnyRole('ADMIN','HR_ADMIN')") public Employee createEmployee(@RequestBody Employee e){return service.createEmployee(e);}
 @GetMapping("/departments") @PreAuthorize("hasAnyRole('ADMIN','HR_ADMIN','HR_USER','MANAGER')") public List<Department> departments(){return service.departments();}
 @PostMapping("/departments") @PreAuthorize("hasAnyRole('ADMIN','HR_ADMIN')") public Department createDepartment(@RequestBody Department d){return service.createDepartment(d);}
 @GetMapping("/attendance") @PreAuthorize("hasAnyRole('ADMIN','HR_ADMIN','HR_USER','MANAGER','EMPLOYEE')") public List<Attendance> attendance(@RequestParam(required=false) LocalDate from,@RequestParam(required=false) LocalDate to){LocalDate end=to==null?LocalDate.now():to;return service.attendance(from==null?end.minusDays(30):from,end);}
 @PostMapping("/attendance") @PreAuthorize("hasAnyRole('ADMIN','HR_ADMIN','HR_USER','MANAGER','EMPLOYEE')") public Attendance mark(@RequestBody Attendance a){return service.markAttendance(a);}
 @GetMapping("/leaves") @PreAuthorize("hasAnyRole('ADMIN','HR_ADMIN','HR_USER','MANAGER','EMPLOYEE')") public List<LeaveRequest> leaves(){return service.leaves();}
 @PostMapping("/leaves") @PreAuthorize("hasAnyRole('ADMIN','HR_ADMIN','MANAGER','EMPLOYEE')") public LeaveRequest leave(@RequestBody LeaveRequest l){return service.requestLeave(l);}
 @PatchMapping("/leaves/{id}") @PreAuthorize("hasAnyRole('ADMIN','HR_ADMIN','MANAGER')") public LeaveRequest updateLeave(@PathVariable Long id,@RequestParam String status){return service.updateLeave(id,status,null);}
 @GetMapping("/goals") @PreAuthorize("hasAnyRole('ADMIN','HR_ADMIN','HR_USER','MANAGER','EMPLOYEE')") public List<Goal> goals(){return service.goals();}
 @PostMapping("/goals") @PreAuthorize("hasAnyRole('ADMIN','HR_ADMIN','MANAGER','EMPLOYEE')") public Goal goal(@RequestBody Goal g){return service.createGoal(g);}
}
