import { ComponentFixture, TestBed } from '@angular/core/testing';
import { beforeEach, describe, expect, it } from 'vitest';
import { HrComponent } from './hr.component';
import { HrApi } from '../services/hr-api.service';
import { AuthService } from '../services/auth.service';

describe('HrComponent', () => {
  let fixture: ComponentFixture<HrComponent>;
  let component: HrComponent;

  const api: any = {
    employees: async () => [],
    departments: async () => [],
    designations: async () => [],
    attendance: async () => [],
    leaves: async () => [],
    goals: async () => [],
    payslips: async () => []
  };

  const auth = {
    hasRole: (role: string) => role === 'ADMIN'
  };

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [HrComponent],
      providers: [
        { provide: HrApi, useValue: api },
        { provide: AuthService, useValue: auth }
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(HrComponent);
    component = fixture.componentInstance;
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should expose both organization creation actions', () => {
    component.section = 'organization';
    fixture.detectChanges();

    const buttons = Array.from(
      fixture.nativeElement.querySelectorAll('button')
    ) as HTMLButtonElement[];

    expect(buttons.some(button => button.textContent?.includes('+ Department'))).toBe(true);
    expect(buttons.some(button => button.textContent?.includes('+ Designation'))).toBe(true);
  });

  it('should show employees as soon as their request completes', async () => {
    const originalEmployees = api.employees;
    const originalDepartments = api.departments;
    let resolveDepartments!: (departments: unknown[]) => void;

    api.employees = async () => [{
      id: 7,
      employeeNumber: 'E007',
      firstName: 'Taylor',
      lastName: 'Employee',
      email: 'taylor@example.com'
    }];
    // Simulate an unrelated HR request that has not completed yet.
    api.departments = () => new Promise((resolve) => {
      resolveDepartments = resolve;
    });

    try {
      const reload = component.reload();
      await new Promise<void>((resolve) => setTimeout(resolve, 0));

      expect(component.employees).toHaveLength(1);
      expect(component.employees[0].employeeNumber).toBe('E007');

      resolveDepartments([]);
      await reload;
    } finally {
      api.employees = originalEmployees;
      api.departments = originalDepartments;
    }
  });

  it('should render a load error accessibly', () => {
    // Isolate template rendering from ngOnInit's asynchronous data reload.
    component.ngOnInit = async () => {};
    component.loadError = 'Some HR data could not be loaded.';
    fixture.detectChanges();

    expect(fixture.nativeElement.querySelector('[role="alert"]')?.textContent)
      .toContain('Some HR data could not be loaded.');
  });
});
