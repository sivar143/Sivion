import { CommonModule } from '@angular/common';
import { Component, OnInit, inject } from '@angular/core';
import { HrApi, Employee, LeaveRequest } from '../../services/hr-api.service';

@Component({
  selector: 'app-hr-leaves',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './leaves.component.html',
  styleUrls: ['../hr.component.scss']
})
export class LeavesComponent implements OnInit {
  private readonly api = inject(HrApi);
  leaves: LeaveRequest[] = [];
  employees: Employee[] = [];
  loadError = '';
  actionError = '';

  async ngOnInit(): Promise<void> {
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