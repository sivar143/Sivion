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

  get sectionTitle(): string {
    return this.section ? this.section[0].toUpperCase() + this.section.slice(1) : 'HR';
  }

  get isPlaceholderSection(): boolean {
    return ['holidays', 'meetings', 'performance', 'knowledge', 'documents'].includes(this.section);
  }
}
