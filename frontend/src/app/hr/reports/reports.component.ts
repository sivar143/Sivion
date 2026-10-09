import { CommonModule } from '@angular/common';
import { Component, OnInit, inject } from '@angular/core';
import { HrApi, Department, Employee, LeaveRequest, Payslip } from '../../services/hr-api.service';

@Component({
  selector: 'app-hr-reports',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './reports.component.html',
  styleUrls: ['../hr.component.scss']
})
export class ReportsComponent implements OnInit {
  private readonly api = inject(HrApi);
  employees: Employee[] = [];
  departments: Department[] = [];
  leaves: LeaveRequest[] = [];
  payslips: Payslip[] = [];
  loadError = '';

  get activeEmployees(): number {
    return this.employees.filter(employee => employee.status === 'ACTIVE').length;
  }
  get pendingLeaves(): number {
    return this.leaves.filter(leave => leave.status === 'PENDING').length;
  }

  async ngOnInit(): Promise<void> {
    const results = await Promise.allSettled([
      this.api.employees(), this.api.departments(), this.api.leaves(), this.api.payslips()
    ]);
    if (results[0].status === 'fulfilled') this.employees = results[0].value;
    else this.loadError = 'Employee summary could not be loaded.';
    if (results[1].status === 'fulfilled') this.departments = results[1].value;
    else this.loadError = [this.loadError, 'Department summary could not be loaded.'].filter(Boolean).join(' ');
    if (results[2].status === 'fulfilled') this.leaves = results[2].value;
    else this.loadError = [this.loadError, 'Leave summary could not be loaded.'].filter(Boolean).join(' ');
    if (results[3].status === 'fulfilled') this.payslips = results[3].value;
    else this.loadError = [this.loadError, 'Payroll summary could not be loaded.'].filter(Boolean).join(' ');
  }
}