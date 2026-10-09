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
    employees: vi.fn(async () => [{ id: 10, employeeNumber: 'E010', firstName: 'Ari', lastName: 'Dev', email: 'ari@example.com', departmentId: 1 }]),
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
      providers: [{ provide: HrApi, useValue: api }, { provide: AuthService, useValue: auth }]
    }).compileComponents();
    fixture = TestBed.createComponent(OrganizationComponent);
    component = fixture.componentInstance;
  });

  it('loads departments, designations, and employee counts', async () => {
    await component.ngOnInit();
    expect(component.departments[0].name).toBe('Engineering');
    expect(component.designations[0].name).toBe('Developer');
    expect(component.employeesInDepartment(1)).toBe(1);
    expect(component.canModifyHr).toBe(true);
  });

  it('creates a department and reloads the organization data', async () => {
    component.departmentDraft = { code: 'OPS', name: 'Operations', status: 'ACTIVE' };
    await component.saveDepartment();
    expect(api.createDepartment).toHaveBeenCalledWith(component.departmentDraft);
    expect(api.departments).toHaveBeenCalled();
    expect(component.departmentModal).toBe(false);
  });
});
