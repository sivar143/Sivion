import { ComponentFixture, TestBed } from '@angular/core/testing';
import { beforeEach, describe, expect, it, vi } from 'vitest';
import { PayrollComponent } from './payroll.component';
import { HrApi } from '../../services/hr-api.service';
import { AuthService } from '../../services/auth.service';

describe('PayrollComponent', () => {
  let fixture: ComponentFixture<PayrollComponent>;
  let component: PayrollComponent;
  const api = {
    payslips: vi.fn(async () => [{ id: 4, employeeId: 5, periodStart: '2026-10-01', periodEnd: '2026-10-31', grossPay: 50000, deductions: 5000, status: 'DRAFT' }]),
    payrollEmployees: vi.fn(async () => [{ id: 5, employeeNumber: 'E005', firstName: 'Ravi', lastName: 'Kumar' }]),
    createPayslip: vi.fn(async () => ({})),
    updatePayslip: vi.fn(async () => ({}))
  };
  const auth = { hasRole: (role: string) => role === 'HR_ADMIN' };

  beforeEach(async () => {
    vi.clearAllMocks();
    await TestBed.configureTestingModule({
      imports: [PayrollComponent],
      providers: [{ provide: HrApi, useValue: api }, { provide: AuthService, useValue: auth }]
    }).compileComponents();
    fixture = TestBed.createComponent(PayrollComponent);
    component = fixture.componentInstance;
  });

  it('loads payslips and employee names', async () => {
    await component.ngOnInit();
    expect(component.payslips).toHaveLength(1);
    expect(component.employeeName(5)).toBe('Ravi Kumar');
    expect(component.canModifyHr).toBe(true);
  });

  it('updates an existing payslip and reloads records', async () => {
    await component.ngOnInit();
    component.openEdit(component.payslips[0]);
    component.draft.grossPay = 52000;
    await component.save();
    expect(api.updatePayslip).toHaveBeenCalledWith(4, expect.objectContaining({ grossPay: 52000 }));
    expect(component.modalOpen).toBe(false);
  });

  it('reports failures when payslips cannot be loaded', async () => {
    api.payslips.mockRejectedValueOnce(new Error('network'));
    await component.ngOnInit();
    expect(component.loadError).toContain('Payslips could not be loaded.');
  });
});
