import { CommonModule } from '@angular/common';
import { Component, OnInit, inject } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { AuthService } from '../../services/auth.service';
import { HrApi, Employee, LeaveRequest } from '../../services/hr-api.service';

@Component({
  selector: 'app-hr-leaves',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './leaves.component.html',
  styleUrls: ['../hr.component.scss']
})
export class LeavesComponent implements OnInit {
  private readonly api = inject(HrApi);
  private readonly auth = inject(AuthService);
  leaves: LeaveRequest[] = [];
  employees: Employee[] = [];
  loadError = '';
  actionError = '';
  requestError = '';
  requestFormOpen = false;
  submittingRequest = false;
  draft: LeaveRequest = this.emptyLeaveRequest();

  get canRequestForOthers(): boolean {
    return ['ADMIN', 'HR_ADMIN', 'HR_USER'].some(role => this.auth.hasRole(role));
  }

  get canReview(): boolean {
    return ['ADMIN', 'HR_ADMIN', 'HR_USER', 'MANAGER'].some(role => this.auth.hasRole(role));
  }

  canReviewLeave(leave: LeaveRequest): boolean {
    if (!this.canReview || leave.status !== 'PENDING') return false;
    if (this.auth.hasRole('MANAGER') && !this.canRequestForOthers) {
      const current = this.employees.find(employee => employee.username === this.auth.username);
      return !!current && current.id !== leave.employeeId;
    }
    return true;
  }

  openRequestForm(): void {
    this.draft = this.emptyLeaveRequest();
    const current = this.employees.find(employee => employee.username === this.auth.username)
      ?? (this.employees.length === 1 ? this.employees[0] : undefined);
    if (!this.canRequestForOthers && current?.id) this.draft.employeeId = current.id;
    this.requestError = '';
    this.requestFormOpen = true;
  }

  closeRequestForm(): void {
    this.requestFormOpen = false;
    this.requestError = '';
  }

  async submitLeaveRequest(): Promise<void> {
    this.requestError = '';
    if (!this.draft.employeeId || !this.draft.leaveType || !this.draft.startDate || !this.draft.endDate) {
      this.requestError = 'Please complete all required leave fields.';
      return;
    }
    if (this.draft.endDate < this.draft.startDate) {
      this.requestError = 'The end date must be on or after the start date.';
      return;
    }
    this.submittingRequest = true;
    try {
      await this.api.requestLeave({ ...this.draft });
      this.closeRequestForm();
      await this.reload();
    } catch (error: any) {
      this.requestError = error?.error?.error ?? error?.message ?? 'Unable to submit the leave request.';
    } finally {
      this.submittingRequest = false;
    }
  }

  private emptyLeaveRequest(): LeaveRequest {
    return { employeeId: 0, leaveType: 'ANNUAL', startDate: '', endDate: '', reason: '' };
  }

  async ngOnInit(): Promise<void> {
    await this.reload();
  }

  async reload(): Promise<void> {
    this.loadError = '';
    const requests = [
      this.api.leaves().then(value => { this.leaves = value; })
        .catch(() => this.addLoadError('Leave requests could not be loaded.')),
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

  async updateLeave(leave: LeaveRequest, status: 'APPROVED' | 'REJECTED'): Promise<void> {
    if (!leave.id) return;
    this.actionError = '';
    try {
      await this.api.updateLeave(leave.id, status);
      this.leaves = this.leaves.map(item => item.id === leave.id ? { ...item, status } : item);
    } catch {
      this.actionError = 'Unable to update the leave request. Please try again.';
    }
  }
}