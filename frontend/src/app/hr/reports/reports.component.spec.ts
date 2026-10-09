import { ComponentFixture, TestBed } from '@angular/core/testing';
import { beforeEach, describe, expect, it, vi } from 'vitest';
import { ReportsComponent } from './reports.component';
import { HrApi } from '../../services/hr-api.service';

describe('ReportsComponent', () => {
  let fixture: ComponentFixture<ReportsComponent>;
  let component: ReportsComponent;
  const api = {
    employees: vi.fn(async () => [
      { id: 1, employeeNumber: 'E001', firstName: 'A', lastName: 'One', email: 'a@example.com', status: 'ACTIVE' },
      { id: 2, employeeNumber: 'E002', firstName: 'B', lastName: 'Two', email: 'b@example.com', status: 'INACTIVE' }
    ]),
    departments: vi.fn(async () => [{ id: 1, code: 'ENG', name: 'Engineering' }]),
    leaves: vi.fn(async () => [
      { id: 1, employeeId: 1, leaveType: 'ANNUAL', startDate: '2026-10-10', endDate: '2026-10-11', status: 'PENDING' },
      { id: 2, employeeId: 2, leaveType: 'SICK', startDate: '2026-10-12', endDate: '2026-10-12', status: 'APPROVED' }
    ]),
    payslips: vi.fn(async () => [{ id: 1, employeeId: 1, periodStart: '2026-10-01', periodEnd: '2026-10-31', grossPay: 50000, deductions: 5000 }])
  };

  beforeEach(async () => {
    vi.clearAllMocks();
    await TestBed.configureTestingModule({
      imports: [ReportsComponent],
      providers: [{ provide: HrApi, useValue: api }]
    }).compileComponents();
    fixture = TestBed.createComponent(ReportsComponent);
    component = fixture.componentInstance;
  });

  it('loads data and computes HR summary metrics', async () => {
    await component.ngOnInit();
    expect(component.employees).toHaveLength(2);
    expect(component.activeEmployees).toBe(1);
    expect(component.pendingLeaves).toBe(1);
    expect(component.departments).toHaveLength(1);
    expect(component.payslips).toHaveLength(1);
  });

  it('aggregates load failures into a visible message', async () => {
    api.employees.mockRejectedValueOnce(new Error('network'));
    api.leaves.mockRejectedValueOnce(new Error('network'));
    await component.ngOnInit();
    expect(component.loadError).toContain('Employee summary could not be loaded.');
    expect(component.loadError).toContain('Leave summary could not be loaded.');
  });
});
