import { CommonModule } from '@angular/common';
import { Component, OnInit, inject } from '@angular/core';
import { HrApi, Attendance, Employee } from '../../services/hr-api.service';

@Component({
  selector: 'app-hr-attendance',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './attendance.component.html',
  styleUrls: ['../hr.component.scss']
})
export class AttendanceComponent implements OnInit {
  private readonly api = inject(HrApi);
  attendance: Attendance[] = [];
  employees: Employee[] = [];
  loadError = '';

  async ngOnInit(): Promise<void> {
    const requests = [
      this.api.attendance().then(value => { this.attendance = value; })
        .catch(() => this.addLoadError('Attendance records could not be loaded.')),
      this.api.employees().then(value => { this.employees = value; })
        .catch(() => this.addLoadError('Employee names could not be loaded.'))
    ];
    await Promise.all(requests);
  }

  private addLoadError(message: string): void {
    this.loadError = this.loadError ? `${this.loadError} ${message}` : message;
  }

  employeeName(id: number): string {
    const employee = this.employees.find(item => item.id === id);
    return employee ? `${employee.firstName} ${employee.lastName}` : `Employee #${id}`;
  }
}