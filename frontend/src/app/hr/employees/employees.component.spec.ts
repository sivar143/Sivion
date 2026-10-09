import { ComponentFixture, TestBed } from '@angular/core/testing';
import { beforeEach, describe, expect, it } from 'vitest';
import { EmployeesComponent } from './employees.component';
import { HrApi } from '../../services/hr-api.service';
import { AuthService } from '../../services/auth.service';

describe('EmployeesComponent', () => {
  let fixture: ComponentFixture<EmployeesComponent>;
  let component: EmployeesComponent;

  const api: any = {
    employees: async () => [],
    departments: async () => [],
    designations: async () => [],
    createEmployee: async () => {},
    updateEmployee: async () => {}
  };
  const auth = { hasRole: (role: string) => role === 'ADMIN' };

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [EmployeesComponent],
      providers: [
        { provide: HrApi, useValue: api },
        { provide: AuthService, useValue: auth }
      ]
    }).compileComponents();
    fixture = TestBed.createComponent(EmployeesComponent);
    component = fixture.componentInstance;
  });

  it('renders employees without waiting for department and designation requests', async () => {
    const originalEmployees = api.employees;
    const originalDepartments = api.departments;
    const originalDesignations = api.designations;
    let resolveDepartments!: (value: unknown[]) => void;
    let resolveDesignations!: (value: unknown[]) => void;

    api.employees = async () => [{
      id: 7,
      employeeNumber: 'E007',
      firstName: 'Taylor',
      lastName: 'Employee',
      email: 'taylor@example.com',
      accountEnabled: true
    }];
    api.departments = () => new Promise(resolve => { resolveDepartments = resolve; });
    api.designations = () => new Promise(resolve => { resolveDesignations = resolve; });

    try {
      const loading = component.reload();
      await new Promise<void>(resolve => setTimeout(resolve, 0));
      expect(component.employees).toHaveLength(1);
      expect(component.employees[0].employeeNumber).toBe('E007');

      resolveDepartments([]);
      resolveDesignations([]);
      await loading;
    } finally {
      api.employees = originalEmployees;
      api.departments = originalDepartments;
      api.designations = originalDesignations;
    }
  });
});
