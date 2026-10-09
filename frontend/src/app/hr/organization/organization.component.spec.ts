import { ComponentFixture, TestBed } from '@angular/core/testing';
import { beforeEach, describe, expect, it, vi } from 'vitest';
import { OrganizationComponent } from './organization.component';
import { HrApi } from '../../services/hr-api.service';
import { AuthService } from '../../services/auth.service';

describe('OrganizationComponent', () => {
  let fixture: ComponentFixture<OrganizationComponent>;
  let component: OrganizationComponent;

  const api = {
    departments: vi.fn(async () => [{ id: 1, code: 'ENG', name: 'Engineering', status: 'ACTIVE' }]),
    designations: vi.fn(async () => [{ id: 2, code: 'DEV', name: 'Developer', status: 'ACTIVE' }]),
    employees: vi.fn(async () => [{
      id: 10, employeeNumber: 'E010', firstName: 'Ari', lastName: 'Dev',
      email: 'ari@example.com', departmentId: 1
    }]),
    createDepartment: vi.fn(async () => ({})),
    updateDepartment: vi.fn(async () => ({})),
    createDesignation: vi.fn(async () => ({})),
    updateDesignation: vi.fn(async () => ({}))
  };
  const auth = { hasRole: (role: string) => role === 'ADMIN' };

  beforeEach(async () => {
    vi.clearAllMocks();
    await TestBed.configureTestingModule({
      imports: [OrganizationComponent],
      providers: [
        { provide: HrApi, useValue: api },
        { provide: AuthService, useValue: auth }
      ]
    }).compileComponents();
    fixture = TestBed.createComponent(OrganizationComponent);
    component = fixture.componentInstance;
  });

  it('loads organization data and employee counts on initial screen render', async () => {
    fixture.detectChanges();
    await fixture.whenStable();
    fixture.detectChanges();

    expect(api.departments).toHaveBeenCalledOnce();
    expect(api.designations).toHaveBeenCalledOnce();
    expect(api.employees).toHaveBeenCalledOnce();
    expect(component.employeesInDepartment(1)).toBe(1);
    expect(fixture.nativeElement.textContent).toContain('Engineering');
    expect(fixture.nativeElement.textContent).toContain('Developer');
    expect(fixture.nativeElement.textContent).toContain('Employees');
  });

  it('creates a department and reloads the organization data', async () => {
    component.departmentDraft = { code: 'OPS', name: 'Operations', status: 'ACTIVE' };
    await component.saveDepartment();
    expect(api.createDepartment).toHaveBeenCalledWith(component.departmentDraft);
    expect(api.departments).toHaveBeenCalled();
    expect(component.departmentModal).toBe(false);
  });
});
