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
    payslips: async () => [],
    updateLeave: async () => {},
    updateDepartment: async () => {},
    createDepartment: async () => {},
    updateDesignation: async () => {},
    createDesignation: async () => {},
    createEmployee: async () => {},
    updateEmployee: async () => {},
    createPayslip: async () => {},
    updatePayslip: async () => {}
  };
  const auth = { hasRole: (role: string) => role === 'ADMIN' };

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

  it('creates the HR section host', () => {
    expect(component).toBeTruthy();
  });

  it('renders the dedicated organization component and its actions', () => {
    fixture.componentRef.setInput('section', 'organization');
    fixture.detectChanges();

    expect(fixture.nativeElement.querySelector('app-hr-organization')).toBeTruthy();
    const buttons = Array.from(fixture.nativeElement.querySelectorAll('button')) as HTMLButtonElement[];
    expect(buttons.some(button => button.textContent?.includes('+ Department'))).toBe(true);
    expect(buttons.some(button => button.textContent?.includes('+ Designation'))).toBe(true);
  });

  it.each([
    ['employees', 'app-hr-employees'],
    ['attendance', 'app-hr-attendance'],
    ['leaves', 'app-hr-leaves'],
    ['payroll', 'app-hr-payroll'],
    ['goals', 'app-hr-goals'],
    ['reports', 'app-hr-reports']
  ])('renders the dedicated component for the %s section', (section, selector) => {
    fixture.componentRef.setInput('section', section);
    fixture.detectChanges();
    expect(fixture.nativeElement.querySelector(selector)).toBeTruthy();
  });
});
