import { CommonModule } from '@angular/common';
import { ChangeDetectorRef, Component, OnInit, inject } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { AuthService } from '../../services/auth.service';
import { HrApi, Employee, Goal } from '../../services/hr-api.service';

@Component({
  selector: 'app-hr-goals',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './goals.component.html',
  styleUrls: ['../hr.component.scss']
})
export class GoalsComponent implements OnInit {
  private readonly api = inject(HrApi);
  private readonly auth = inject(AuthService);
  private readonly cdr = inject(ChangeDetectorRef);
  goals: Goal[] = [];
  employees: Employee[] = [];
  loadError = '';
  actionError = '';
  formOpen = false;
  saving = false;
  draft: Goal = this.emptyGoal();

  get canAssignOthers(): boolean {
    return ['ADMIN', 'HR_ADMIN', 'HR_USER', 'MANAGER'].some(role => this.auth.hasRole(role));
  }

  openForm(): void {
    this.draft = this.emptyGoal();
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

  async saveGoal(): Promise<void> {
    this.actionError = '';
    if (!this.draft.employeeId || !this.draft.title.trim()) {
      this.actionError = 'Please select an employee and enter a goal title.';
      return;
    }
    if (this.draft.targetValue != null && this.draft.targetValue < 0) {
      this.actionError = 'The target value cannot be negative.';
      return;
    }
    this.saving = true;
    try {
      await this.api.createGoal({ ...this.draft });
      this.closeForm();
      this.cdr.markForCheck();
      await this.reload();
    } catch (error: any) {
      this.actionError = error?.error?.error ?? error?.message ?? 'Unable to create the goal.';
      this.cdr.markForCheck();
    } finally {
      this.saving = false;
      this.cdr.markForCheck();
    }
  }

  private emptyGoal(): Goal {
    return { employeeId: 0, title: '', description: '', targetValue: 1, currentValue: 0, status: 'ACTIVE' };
  }

  async ngOnInit(): Promise<void> {
    await this.reload();
  }

  async reload(): Promise<void> {
    this.loadError = '';
    const requests = [
      this.api.goals().then(value => { this.goals = value; this.cdr.markForCheck(); })
        .catch(() => this.addLoadError('Goals could not be loaded.')),
      this.api.employees().then(value => { this.employees = value; this.cdr.markForCheck(); })
        .catch(() => this.addLoadError('Employee names could not be loaded.'))
    ];
    await Promise.all(requests);
  }

  private addLoadError(message: string): void {
    this.loadError = this.loadError ? `${this.loadError} ${message}` : message;
    this.cdr.markForCheck();
  }

  employeeName(id: number): string {
    const employee = this.employees.find(item => item.id === id);
    return employee ? `${employee.firstName} ${employee.lastName}` : `Employee #${id}`;
  }
}