import { ComponentFixture, TestBed } from '@angular/core/testing';
import { beforeEach, describe, expect, it, vi } from 'vitest';
import { LeavesComponent } from './leaves.component';
import { HrApi } from '../../services/hr-api.service';

describe('LeavesComponent', () => {
  let fixture: ComponentFixture<LeavesComponent>;
  let component: LeavesComponent;
  const api = {
    leaves: vi.fn(async () => [{ id: 3, employeeId: 5, leaveType: 'ANNUAL', startDate: '2026-10-10', endDate: '2026-10-11', status: 'PENDING' }]),
    employees: vi.fn(async () => [{ id: 5, employeeNumber: 'E005', firstName: 'Ravi', lastName: 'Kumar', email: 'ravi@example.com' }]),
    updateLeave: vi.fn(async () => ({}))
  };

  beforeEach(async () => {
    vi.clearAllMocks();
    await TestBed.configureTestingModule({
      imports: [LeavesComponent],
      providers: [{ provide: HrApi, useValue: api }]
    }).compileComponents();
    fixture = TestBed.createComponent(LeavesComponent);
    component = fixture.componentInstance;
  });

  it('loads leave requests and employee names', async () => {
    await component.ngOnInit();
    expect(component.leaves).toHaveLength(1);
    expect(component.employeeName(5)).toBe('Ravi Kumar');
  });

  it('updates the leave status after a successful API response', async () => {
    await component.ngOnInit();
    await component.updateLeave(component.leaves[0], 'APPROVED');
    expect(api.updateLeave).toHaveBeenCalledWith(3, 'APPROVED');
    expect(component.leaves[0].status).toBe('APPROVED');
    expect(component.actionError).toBe('');
  });

  it('shows an error when leave status cannot be updated', async () => {
    await component.ngOnInit();
    api.updateLeave.mockRejectedValueOnce(new Error('network'));
    await component.updateLeave(component.leaves[0], 'REJECTED');
    expect(component.actionError).toContain('Unable to update');
    expect(component.leaves[0].status).toBe('PENDING');
  });
});
