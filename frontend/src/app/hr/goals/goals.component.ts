import { CommonModule } from '@angular/common';
import { Component, OnInit, inject } from '@angular/core';
import { HrApi, Employee, Goal } from '../../services/hr-api.service';

@Component({
  selector: 'app-hr-goals',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './goals.component.html',
  styleUrls: ['../hr.component.scss']
})
export class GoalsComponent implements OnInit {
  private readonly api = inject(HrApi);
  goals: Goal[] = [];
  employees: Employee[] = [];
  loadError = '';

  async ngOnInit(): Promise<void> {
    const requests = [
      this.api.goals().then(value => { this.goals = value; })
        .catch(() => this.addLoadError('Goals could not be loaded.')),
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