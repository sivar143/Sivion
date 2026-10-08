import  {
  Injectable,inject
}
from '@angular/core';
import  {
  HttpClient,HttpHeaders
}
from '@angular/common/http';
import  {
  firstValueFrom
}
from 'rxjs';
import  {
  AuthService
}
from './auth.service';
export interface Employee  {
  id?:number;
  employeeNumber:string;
  firstName:string;
  lastName:string;
  email:string;
  phone?:string;
  departmentId?:number;
  designationId?:number;
  designation?:string;
  managerId?:number;
  joiningDate?:string;
  status?:string;
  keycloakUserId?:string;
  username?:string;
  role?:string;
  accountEnabled?:boolean;
  password?:string;
  temporaryPassword?:boolean;
}
export interface Department  {
  id?:number;
  code:string;
  name:string;
  parentId?:number;
  status?:string;
}
export interface Designation  {
  id?:number;
  code:string;
  name:string;
  description?:string;
  departmentId?:number;
  status?:string;
}
export interface Attendance  {
  id?:number;
  employeeId:number;
  attendanceDate:string;
  status:string;
  checkIn?:string;
  checkOut?:string;
}
export interface LeaveRequest  {
  id?:number;
  employeeId:number;
  leaveType:string;
  startDate:string;
  endDate:string;
  status?:string;
  reason?:string;
}
export interface Goal  {
  id?:number;
  employeeId:number;
  title:string;
  description?:string;
  targetValue?:number;
  currentValue?:number;
  dueDate?:string;
  status?:string;
}
export interface Payslip  {
  id?:number;
  employeeId:number;
  periodStart:string;
  periodEnd:string;
  grossPay:number;
  deductions:number;
  netPay?:number;
  status?:string;
  notes?:string;
}
@Injectable( {
  providedIn:'root'
}) export class HrApi  {
  private http=inject(HttpClient);
  private auth=inject(AuthService);
  private async options() {
    return {
      headers:new HttpHeaders( {
        'Authorization':`Bearer ${await this.auth.token()}`,'X-Tenant-ID':'1'
      })
    };
  }
  async employees() {
    return firstValueFrom(this.http.get<Employee[]>('/api/v1/hr/employees',await this.options()));
  }
  async departments() {
    return firstValueFrom(this.http.get<Department[]>('/api/v1/hr/departments',await this.options()));
  }
  async designations() {
    return firstValueFrom(this.http.get<Designation[]>('/api/v1/hr/designations',await this.options()));
  }
  async createEmployee(e:Employee) {
    return firstValueFrom(this.http.post<Employee>('/api/v1/hr/employees',e,await this.options()));
  }
  async updateEmployee(id:number,e:Employee) {
    return firstValueFrom(this.http.put<Employee>(`/api/v1/hr/employees/${id}`,e,await this.options()));
  }
  async createDepartment(d:Department) {
    return firstValueFrom(this.http.post<Department>('/api/v1/hr/departments',d,await this.options()));
  }
  async updateDepartment(d:Department) {
    if(!d.id)throw new Error('Department id is required for update');
    return firstValueFrom(this.http.put<Department>(`/api/v1/hr/departments/${d.id}`,d,await this.options()));
  }
  async createDesignation(d:Designation) {
    return firstValueFrom(this.http.post<Designation>('/api/v1/hr/designations',d,await this.options()));
  }
  async updateDesignation(d:Designation) {
    if(!d.id)throw new Error('Designation id is required for update');
    return firstValueFrom(this.http.put<Designation>(`/api/v1/hr/designations/${d.id}`,d,await this.options()));
  }
  async attendance() {
    return firstValueFrom(this.http.get<Attendance[]>('/api/v1/hr/attendance',await this.options()));
  }
  async markAttendance(a:Attendance) {
    return firstValueFrom(this.http.post<Attendance>('/api/v1/hr/attendance',a,await this.options()));
  }
  async leaves() {
    return firstValueFrom(this.http.get<LeaveRequest[]>('/api/v1/hr/leaves',await this.options()));
  }
  async requestLeave(l:LeaveRequest) {
    return firstValueFrom(this.http.post<LeaveRequest>('/api/v1/hr/leaves',l,await this.options()));
  }
  async updateLeave(id:number,status:string) {
    return firstValueFrom(this.http.patch<LeaveRequest>(`/api/v1/hr/leaves/${id}?status=${status}`, {
    },await this.options()));
  }
  async goals() {
    return firstValueFrom(this.http.get<Goal[]>('/api/v1/hr/goals',await this.options()));
  }
  async createGoal(g:Goal) {
    return firstValueFrom(this.http.post<Goal>('/api/v1/hr/goals',g,await this.options()));
  }
  async payslips() {
    return firstValueFrom(this.http.get<Payslip[]>('/api/v1/hr/payslips',await this.options()));
  }
  async createPayslip(p:Payslip) {
    return firstValueFrom(this.http.post<Payslip>('/api/v1/hr/payslips',p,await this.options()));
  }
  async updatePayslip(id:number,p:Payslip) {
    return firstValueFrom(this.http.put<Payslip>(`/api/v1/hr/payslips/${id}`,p,await this.options()));
  }
}
