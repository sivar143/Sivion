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
    const results = await Promise.allSettled([this.api.goals(), this.api.employees()]);
    if (results[0].status === 'fulfilled') this.goals = results[0].value;
    else this.loadError = 'Goals could not be loaded.';
    if (results[1].status === 'fulfilled') this.employees = results[1].value;
    else this.loadError = [this.loadError, 'Employee names could not be loaded.'].filter(Boolean).join(' ');
  }

  employeeName(id: number): string {
    const employee = this.employees.find(item => item.id === id);
    return employee ? `${employee.firstName} ${employee.lastName}` : `Employee #${id}`;
  }
}