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
    const results = await Promise.allSettled([this.api.leaves(), this.api.employees()]);
    if (results[0].status === 'fulfilled') this.leaves = results[0].value;
    else this.loadError = 'Leave requests could not be loaded.';
    if (results[1].status === 'fulfilled') this.employees = results[1].value;
    else this.loadError = [this.loadError, 'Employee names could not be loaded.'].filter(Boolean).join(' ');
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