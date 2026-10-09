import { CommonModule } from '@angular/common';
import { ChangeDetectorRef, Component, OnInit, inject } from '@angular/core';
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
  private readonly cdr = inject(ChangeDetectorRef);
  employees: Employee[] = [];
  departments: Department[] = [];
  leaves: LeaveRequest[] = [];
  payslips: Payslip[] = [];
  loadError = '';

  private addLoadError(message: string): void {
    this.loadError = this.loadError ? `${this.loadError} ${message}` : message;
    this.cdr.markForCheck();
  }

  get activeEmployees(): number {
    return this.employees.filter(employee => employee.status === 'ACTIVE').length;
  }
  get pendingLeaves(): number {
    return this.leaves.filter(leave => leave.status === 'PENDING').length;
  }

  async ngOnInit(): Promise<void> {
    const requests = [
      this.api.employees().then(value => { this.employees = value; this.cdr.markForCheck(); })
        .catch(() => this.addLoadError('Employee summary could not be loaded.')),
      this.api.departments().then(value => { this.departments = value; this.cdr.markForCheck(); })
        .catch(() => this.addLoadError('Department summary could not be loaded.')),
      this.api.leaves().then(value => { this.leaves = value; this.cdr.markForCheck(); })
        .catch(() => this.addLoadError('Leave summary could not be loaded.')),
      this.api.payslips().then(value => { this.payslips = value; this.cdr.markForCheck(); })
        .catch(() => this.addLoadError('Payroll summary could not be loaded.'))
    ];
    await Promise.all(requests);
  }
}