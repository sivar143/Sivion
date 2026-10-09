import { ComponentFixture, TestBed } from '@angular/core/testing';
import { beforeEach, describe, expect, it, vi } from 'vitest';
import { AttendanceComponent } from './attendance.component';
import { HrApi } from '../../services/hr-api.service';
import { AuthService } from '../../services/auth.service';

describe('AttendanceComponent', () => {
  let fixture: ComponentFixture<AttendanceComponent>;
  let component: AttendanceComponent;
  const api = {
    attendance: vi.fn(async () => [{ id: 1, employeeId: 5, attendanceDate: '2026-10-01', status: 'PRESENT', checkIn: '09:00' }]),
    employees: vi.fn(async () => [{ id: 5, employeeNumber: 'E005', firstName: 'Ravi', lastName: 'Kumar', email: 'ravi@example.com', username: 'ravi' }]),
    markAttendance: vi.fn(async () => ({}))
  };

  const auth = { username: 'ravi', hasRole: (role: string) => role === 'EMPLOYEE' };

  beforeEach(async () => {
    vi.clearAllMocks();
    await TestBed.configureTestingModule({
      imports: [AttendanceComponent],
      providers: [{ provide: HrApi, useValue: api }, { provide: AuthService, useValue: auth }]
    }).compileComponents();
    fixture = TestBed.createComponent(AttendanceComponent);
    component = fixture.componentInstance;
  });

  it('loads attendance and resolves employee names', async () => {
    await component.ngOnInit();
    expect(component.attendance).toHaveLength(1);
    expect(component.employeeName(5)).toBe('Ravi Kumar');
  });

  it('records attendance for the signed-in employee', async () => {
    await component.ngOnInit();
    component.openForm();
    component.draft.attendanceDate = '2026-10-09';
    component.draft.status = 'PRESENT';
    await component.saveAttendance();
    expect(api.markAttendance).toHaveBeenCalledWith(expect.objectContaining({
      employeeId: 5, attendanceDate: '2026-10-09', status: 'PRESENT',
      checkIn: undefined, checkOut: undefined
    }));
    expect(component.formOpen).toBe(false);
  });

  it('rejects check-out earlier than check-in', async () => {
    await component.ngOnInit();
    component.draft = {
      employeeId: 5, attendanceDate: '2026-10-09', status: 'PRESENT',
      checkIn: '2026-10-09T17:00', checkOut: '2026-10-09T09:00'
    };
    await component.saveAttendance();
    expect(api.markAttendance).not.toHaveBeenCalled();
    expect(component.actionError).toContain('Check-out');
  });

  it('uses a fallback label when an employee is missing', () => {
    expect(component.employeeName(99)).toBe('Employee #99');
  });

  it('reports an attendance API failure without rejecting initialization', async () => {
    api.attendance.mockRejectedValueOnce(new Error('network'));
    await component.ngOnInit();
    expect(component.loadError).toContain('Attendance records could not be loaded.');
  });
});
