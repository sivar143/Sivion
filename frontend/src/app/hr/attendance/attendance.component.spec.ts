import { ComponentFixture, TestBed } from '@angular/core/testing';
import { beforeEach, describe, expect, it, vi } from 'vitest';
import { AttendanceComponent } from './attendance.component';
import { HrApi } from '../../services/hr-api.service';

describe('AttendanceComponent', () => {
  let fixture: ComponentFixture<AttendanceComponent>;
  let component: AttendanceComponent;
  const api = {
    attendance: vi.fn(async () => [{ id: 1, employeeId: 5, attendanceDate: '2026-10-01', status: 'PRESENT', checkIn: '09:00' }]),
    employees: vi.fn(async () => [{ id: 5, employeeNumber: 'E005', firstName: 'Ravi', lastName: 'Kumar', email: 'ravi@example.com' }])
  };

  beforeEach(async () => {
    vi.clearAllMocks();
    await TestBed.configureTestingModule({
      imports: [AttendanceComponent],
      providers: [{ provide: HrApi, useValue: api }]
    }).compileComponents();
    fixture = TestBed.createComponent(AttendanceComponent);
    component = fixture.componentInstance;
  });

  it('loads attendance and resolves employee names', async () => {
    await component.ngOnInit();
    expect(component.attendance).toHaveLength(1);
    expect(component.employeeName(5)).toBe('Ravi Kumar');
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
