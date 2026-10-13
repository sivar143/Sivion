import { ComponentFixture, TestBed } from '@angular/core/testing';
import { beforeEach, describe, expect, it, vi } from 'vitest';
import { HrApi } from '../services/hr-api.service';
import { AdministrationComponent } from './administration.component';

describe('AdministrationComponent', () => {
  let fixture: ComponentFixture<AdministrationComponent>;
  let component: AdministrationComponent;

  const api = {
    employees: vi.fn(async (): Promise<any[]> => [
      { id: 1, employeeNumber: 'EMP-1', firstName: 'Ada', lastName: 'Lovelace', role: 'HR_USER', status: 'ACTIVE', accountEnabled: true },
      { id: 2, employeeNumber: 'EMP-2', firstName: 'Grace', lastName: 'Hopper', role: 'EMPLOYEE', status: 'INACTIVE', accountEnabled: false }
    ])
  };

  beforeEach(async () => {
    vi.clearAllMocks();
    api.employees.mockResolvedValue([
      { id: 1, employeeNumber: 'EMP-1', firstName: 'Ada', lastName: 'Lovelace', role: 'HR_USER', status: 'ACTIVE', accountEnabled: true },
      { id: 2, employeeNumber: 'EMP-2', firstName: 'Grace', lastName: 'Hopper', role: 'EMPLOYEE', status: 'INACTIVE', accountEnabled: false }
    ]);
    await TestBed.configureTestingModule({
      imports: [AdministrationComponent],
      providers: [{ provide: HrApi, useValue: api }]
    }).compileComponents();
    fixture = TestBed.createComponent(AdministrationComponent);
    component = fixture.componentInstance;
  });

  it('counts enabled accounts and roles', async () => {
    await component.reload();
    expect(component.enabledAccounts).toBe(1);
    expect(component.roleCounts).toEqual([
      { role: 'EMPLOYEE', count: 1 },
      { role: 'HR_USER', count: 1 }
    ]);
  });

  it('emits a request to manage staff logins', () => {
    const listener = vi.fn();
    component.manageStaff.subscribe(listener);
    component.manageStaff.emit();
    expect(listener).toHaveBeenCalledOnce();
  });
});
