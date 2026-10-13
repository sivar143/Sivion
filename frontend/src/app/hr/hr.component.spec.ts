import { ComponentFixture, TestBed } from '@angular/core/testing';
import { beforeEach, describe, expect, it } from 'vitest';
import { HrComponent } from './hr.component';
import { HrApi } from '../services/hr-api.service';
import { AuthService } from '../services/auth.service';
import { HrCalendarApi } from './calendar/hr-calendar-api.service';
import { HrWorkspaceRecordsApi } from './workspace-records/hr-workspace-records-api.service';
import { EmployeeDocumentsApi } from './documents/employee-documents-api.service';

describe('HrComponent', () => {
  let fixture: ComponentFixture<HrComponent>;
  let component: HrComponent;

  const api: any = {
    employees: async () => [],
    payrollEmployees: async () => [],
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
  const auth = { roles: ['ADMIN'], hasRole: (role: string) => role === 'ADMIN', token: async () => 'test-token' };

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [HrComponent],
      providers: [
        { provide: HrApi, useValue: api },
        { provide: AuthService, useValue: auth },
        { provide: HrCalendarApi, useValue: { list: async () => [], create: async (entry: any) => entry, update: async (entry: any) => entry } },
        { provide: HrWorkspaceRecordsApi, useValue: { list: async () => [], create: async (entry: any) => entry, update: async (entry: any) => entry } },
        { provide: EmployeeDocumentsApi, useValue: { list: async () => [], upload: async () => ({}), download: async () => new Blob() } }
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
    expect(fixture.nativeElement.querySelector('app-hr-employees')).toBeFalsy();
    expect(fixture.nativeElement.textContent).not.toContain('People & HR');

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
    expect(fixture.nativeElement.textContent).not.toContain('People & HR');
  });

  it.each([
    ['employees', 'Employees'],
    ['organization', 'Organization'],
    ['attendance', 'Attendance'],
    ['leaves', 'Leave Management'],
    ['payroll', 'Payroll & Payslips']
  ])('uses the correct menu label for the %s section', (section, expectedLabel) => {
    fixture.componentRef.setInput('section', section);
    fixture.detectChanges();

    expect(component.sectionTitle).toBe(expectedLabel);
  });

  it('renders the dedicated learning workflow for the knowledge section', () => {
    fixture.componentRef.setInput('section', 'knowledge');
    fixture.detectChanges();

    expect(fixture.nativeElement.textContent).toContain('Learning and development');
    expect(fixture.nativeElement.textContent).not.toContain('People & HR');
  });
});
