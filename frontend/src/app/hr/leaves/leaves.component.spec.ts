import { ComponentFixture, TestBed } from '@angular/core/testing';
import { beforeEach, describe, expect, it, vi } from 'vitest';
import { LeavesComponent } from './leaves.component';
import { HrApi } from '../../services/hr-api.service';
import { AuthService } from '../../services/auth.service';

describe('LeavesComponent', () => {
  let fixture: ComponentFixture<LeavesComponent>;
  let component: LeavesComponent;
  const api = {
    leaves: vi.fn(async () => [{ id: 3, employeeId: 5, leaveType: 'ANNUAL', startDate: '2026-10-10', endDate: '2026-10-11', status: 'PENDING' }]),
    employees: vi.fn(async () => [{ id: 5, employeeNumber: 'E005', firstName: 'Ravi', lastName: 'Kumar', email: 'ravi@example.com' }]),
    updateLeave: vi.fn(async () => ({})),
    requestLeave: vi.fn(async () => ({}))
  };

  const auth = { username: 'hradmin', hasRole: (role: string) => role === 'HR_ADMIN' };

  beforeEach(async () => {
    vi.clearAllMocks();
    await TestBed.configureTestingModule({
      imports: [LeavesComponent],
      providers: [{ provide: HrApi, useValue: api }, { provide: AuthService, useValue: auth }]
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

  it('submits a valid leave request and reloads the list', async () => {
    await component.ngOnInit();
    component.draft = {
      employeeId: 5, leaveType: 'ANNUAL', startDate: '2026-10-20',
      endDate: '2026-10-22', reason: 'Family travel'
    };
    component.requestFormOpen = true;
    await component.submitLeaveRequest();
    expect(api.requestLeave).toHaveBeenCalledWith(expect.objectContaining({
      employeeId: 5, leaveType: 'ANNUAL', startDate: '2026-10-20', endDate: '2026-10-22'
    }));
    expect(component.requestFormOpen).toBe(false);
  });

  it('rejects a leave request when the end date precedes the start date', async () => {
    component.draft = {
      employeeId: 5, leaveType: 'ANNUAL', startDate: '2026-10-22',
      endDate: '2026-10-20', reason: ''
    };
    await component.submitLeaveRequest();
    expect(api.requestLeave).not.toHaveBeenCalled();
    expect(component.requestError).toContain('end date');
  });

  it('shows an error when leave status cannot be updated', async () => {
    await component.ngOnInit();
    api.updateLeave.mockRejectedValueOnce(new Error('network'));
    await component.updateLeave(component.leaves[0], 'REJECTED');
    expect(component.actionError).toContain('Unable to update');
    expect(component.leaves[0].status).toBe('PENDING');
  });

  it('renders leave requests after the initial asynchronous load', async () => {
    fixture.detectChanges();
    await fixture.whenStable();
    fixture.detectChanges();
    expect(fixture.nativeElement.textContent).toContain('ANNUAL');
    expect(fixture.nativeElement.textContent).toContain('Ravi Kumar');
  });
});
