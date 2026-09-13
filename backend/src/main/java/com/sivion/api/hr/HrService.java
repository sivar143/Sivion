package com.sivion.api.hr;

import com.sivion.api.hr.domain.*; import com.sivion.api.hr.repository.*; import org.springframework.stereotype.Service; import org.springframework.transaction.annotation.Transactional; import java.time.*; import java.util.*;
@Service @Transactional
public class HrService {
 private final EmployeeRepository employees; private final DepartmentRepository departments; private final AttendanceRepository attendance; private final LeaveRequestRepository leaves; private final GoalRepository goals;
 public HrService(EmployeeRepository e,DepartmentRepository d,AttendanceRepository a,LeaveRequestRepository l,GoalRepository g){employees=e;departments=d;attendance=a;leaves=l;goals=g;}
 public List<Employee> employees(){return employees.findByTenantIdOrderByLastNameAscFirstNameAsc(1L);} public List<Department> departments(){return departments.findByTenantIdOrderByName(1L);} public long activeEmployees(){return employees.countByTenantIdAndStatus(1L,"ACTIVE");}
 public Employee createEmployee(Employee e){e.setTenantId(1L); if(e.getStatus()==null)e.setStatus("ACTIVE"); return employees.save(e);} public Department createDepartment(Department d){d.setTenantId(1L);return departments.save(d);}
 public Attendance markAttendance(Attendance a){a.setTenantId(1L); return attendance.findByTenantIdAndEmployeeIdAndAttendanceDate(1L,a.getEmployeeId(),a.getAttendanceDate()).map(x->{x.setStatus(a.getStatus());x.setCheckIn(a.getCheckIn());x.setCheckOut(a.getCheckOut());return attendance.save(x);}).orElseGet(()->attendance.save(a));}
 public List<Attendance> attendance(LocalDate from,LocalDate to){return attendance.findByTenantIdAndAttendanceDateBetweenOrderByAttendanceDateDesc(1L,from,to);} public List<LeaveRequest> leaves(){return leaves.findByTenantIdOrderByCreatedAtDesc(1L);} public LeaveRequest requestLeave(LeaveRequest l){l.setTenantId(1L);l.setStatus("PENDING");return leaves.save(l);}
 public LeaveRequest updateLeave(Long id,String status,Long approver){LeaveRequest l=leaves.findById(id).orElseThrow(()->new IllegalArgumentException("Leave request not found"));if(!l.getTenantId().equals(1L))throw new IllegalArgumentException("Invalid tenant");l.setStatus(status);l.setApprovedBy(approver);return leaves.save(l);}
 public List<Goal> goals(){return goals.findByTenantIdOrderByDueDateAsc(1L);} public Goal createGoal(Goal g){g.setTenantId(1L);return goals.save(g);}
}
