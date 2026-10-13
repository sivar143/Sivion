import { CommonModule } from '@angular/common';
import { Component, Input } from '@angular/core';
import { AttendanceComponent } from './attendance/attendance.component';
import { EmployeesComponent } from './employees/employees.component';
import { GoalsComponent } from './goals/goals.component';
import { LeavesComponent } from './leaves/leaves.component';
import { OrganizationComponent } from './organization/organization.component';
import { PayrollComponent } from './payroll/payroll.component';
import { ReportsComponent } from './reports/reports.component';

@Component({
  selector: 'app-hr',
  standalone: true,
  imports: [
    CommonModule,
    EmployeesComponent,
    OrganizationComponent,
    AttendanceComponent,
    LeavesComponent,
    PayrollComponent,
    GoalsComponent,
    ReportsComponent
  ],
  templateUrl: './hr.component.html',
  styleUrls: ['./hr.component.scss']
})
export class HrComponent {
  @Input() section = 'employees';

  // Use the menu's actual label rather than showing the same generic HR heading on every section.
  private readonly sectionLabels: Record<string, string> = {
    employees: 'Employees',
    organization: 'Organization',
    attendance: 'Attendance',
    leaves: 'Leave Management',
    payroll: 'Payroll & Payslips',
    holidays: 'Holidays',
    meetings: 'Meetings',
    goals: 'Goals',
    performance: 'Performance',
    knowledge: 'Knowledge Development',
    documents: 'Employee Documents',
    reports: 'Reports'
  };

  private readonly sectionDescriptions: Record<string, string> = {
    holidays: 'Maintain the organization holiday calendar.',
    meetings: 'Coordinate organization and employee meetings.',
    performance: 'Review employee performance and review cycles.',
    knowledge: 'Manage learning and development activities.',
    documents: 'Manage employee-related documents and records.'
  };

  get sectionTitle(): string {
    return this.sectionLabels[this.section] || 'HR';
  }

  get sectionDescription(): string {
    return this.sectionDescriptions[this.section] || 'Manage this HR workflow.';
  }

  get isPlaceholderSection(): boolean {
    return ['holidays', 'meetings', 'performance', 'knowledge', 'documents'].includes(this.section);
  }
}
