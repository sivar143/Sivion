import { CommonModule } from '@angular/common';
import { Component, OnInit, inject } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { AuthService } from '../../services/auth.service';
import { HrApi, Attendance, Employee } from '../../services/hr-api.service';

@Component({
  selector: 'app-hr-attendance',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './attendance.component.html',
  styleUrls: ['../hr.component.scss']
})
export class AttendanceComponent implements OnInit {
  private readonly api = inject(HrApi);
  private readonly auth = inject(AuthService);
  attendance: Attendance[] = [];
  employees: Employee[] = [];
  loadError = '';
  actionError = '';
  fromDate = this.dateDaysAgo(30);
  toDate = this.localDate();
  formOpen = false;
  saving = false;
  draft: Attendance = this.emptyAttendance();

  get canSelectEmployee(): boolean {
    return ['ADMIN', 'HR_ADMIN', 'HR_USER', 'MANAGER'].some(role => this.auth.hasRole(role));
  }

  openForm(): void {
    this.draft = this.emptyAttendance();
    const current = this.employees.find(employee => employee.username === this.auth.username)
      ?? (this.employees.length === 1 ? this.employees[0] : undefined);
    if (current?.id) this.draft.employeeId = current.id;
    this.actionError = '';
    this.formOpen = true;
  }

  closeForm(): void {
    this.formOpen = false;
    this.actionError = '';
  }

  async saveAttendance(): Promise<void> {
    this.actionError = '';
    if (!this.draft.employeeId || !this.draft.attendanceDate) {
      this.actionError = 'Please select an employee and attendance date.';
      return;
    }
    if (this.draft.checkIn && this.draft.checkOut && this.draft.checkOut < this.draft.checkIn) {
      this.actionError = 'Check-out time cannot be earlier than check-in time.';
      return;
    }
    this.saving = true;
    try {
      await this.api.markAttendance({ ...this.draft, checkIn: this.draft.checkIn || undefined, checkOut: this.draft.checkOut || undefined });
      this.closeForm();
      await this.reload();
    } catch (error: any) {
      this.actionError = error?.error?.error ?? error?.message ?? 'Unable to save attendance.';
    } finally {
      this.saving = false;
    }
  }

  private emptyAttendance(): Attendance {
    return {
      employeeId: 0,
      attendanceDate: this.localDate(),
      status: 'PRESENT',
      checkIn: '',
      checkOut: ''
    };
  }

  private localDate(): string {
    return new Date(Date.now() - new Date().getTimezoneOffset() * 60000).toISOString().slice(0, 10);
  }

  private dateDaysAgo(days: number): string {
    const date = new Date();
    date.setDate(date.getDate() - days);
    return new Date(date.getTime() - date.getTimezoneOffset() * 60000).toISOString().slice(0, 10);
  }

  async ngOnInit(): Promise<void> {
    await this.reload();
  }

  async reload(): Promise<void> {
    this.loadError = '';
    if (this.fromDate && this.toDate && this.fromDate > this.toDate) {
      this.loadError = 'The start date must be on or before the end date.';
      return;
    }
    const requests = [
      this.api.attendance(this.fromDate || undefined, this.toDate || undefined).then(value => { this.attendance = value; })
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