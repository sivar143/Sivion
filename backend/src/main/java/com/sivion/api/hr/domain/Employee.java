package com.sivion.api.hr.domain;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name="hr_employees", uniqueConstraints=@UniqueConstraint(name="uk_hr_employee_tenant_number", columnNames={"tenant_id","employee_number"}))
public class Employee {
  @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
  @Column(name="tenant_id", nullable=false) private Long tenantId;
  @Column(name="employee_number", nullable=false, length=40) private String employeeNumber;
  @Column(name="first_name", nullable=false, length=100) private String firstName;
  @Column(name="last_name", nullable=false, length=100) private String lastName;
  @Column(nullable=false, length=180) private String email;
  @Column(length=80) private String phone;
  @Column(name="department_id") private Long departmentId;
  @Column(length=120) private String designation;
  @Column(name="manager_id") private Long managerId;
  @Column(name="joining_date") private LocalDate joiningDate;
  @Column(nullable=false, length=20) private String status="ACTIVE";
  public Long getId(){return id;} public Long getTenantId(){return tenantId;} public void setTenantId(Long v){tenantId=v;} public String getEmployeeNumber(){return employeeNumber;} public void setEmployeeNumber(String v){employeeNumber=v;} public String getFirstName(){return firstName;} public void setFirstName(String v){firstName=v;} public String getLastName(){return lastName;} public void setLastName(String v){lastName=v;} public String getEmail(){return email;} public void setEmail(String v){email=v;} public String getPhone(){return phone;} public void setPhone(String v){phone=v;} public Long getDepartmentId(){return departmentId;} public void setDepartmentId(Long v){departmentId=v;} public String getDesignation(){return designation;} public void setDesignation(String v){designation=v;} public Long getManagerId(){return managerId;} public void setManagerId(Long v){managerId=v;} public LocalDate getJoiningDate(){return joiningDate;} public void setJoiningDate(LocalDate v){joiningDate=v;} public String getStatus(){return status;} public void setStatus(String v){status=v;}
}
