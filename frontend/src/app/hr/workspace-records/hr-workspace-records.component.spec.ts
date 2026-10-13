import { ComponentFixture, TestBed } from '@angular/core/testing';
import { beforeEach, describe, expect, it, vi } from 'vitest';
import { HrApi } from '../../services/hr-api.service';
import { AuthService } from '../../services/auth.service';
import { HrWorkspaceRecordsApi } from './hr-workspace-records-api.service';
import { HrWorkspaceRecordsComponent } from './hr-workspace-records.component';

describe('HrWorkspaceRecordsComponent', () => {
  let fixture: ComponentFixture<HrWorkspaceRecordsComponent>;
  let component: HrWorkspaceRecordsComponent;

  const api = {
    list: vi.fn(async () => []),
    create: vi.fn(async (record: any) => record),
    update: vi.fn(async (record: any) => record)
  };

  beforeEach(async () => {
    vi.clearAllMocks();
    api.list.mockResolvedValue([]);
    await TestBed.configureTestingModule({
      imports: [HrWorkspaceRecordsComponent],
      providers: [
        { provide: HrWorkspaceRecordsApi, useValue: api },
        { provide: HrApi, useValue: { employees: async () => [{ id: 4, employeeNumber: 'EMP-4', firstName: 'Sam', lastName: 'Lee' }] } },
        { provide: AuthService, useValue: { roles: ['HR_USER'] } }
      ]
    }).compileComponents();
    fixture = TestBed.createComponent(HrWorkspaceRecordsComponent);
    component = fixture.componentInstance;
  });

  it('validates a performance review before sending it', async () => {
    component.type = 'PERFORMANCE';
    component.draft.title = 'Quarterly review';
    component.draft.employeeId = 4;
    component.draft.score = 6;
    await component.save();
    expect(component.error).toContain('score from 1 to 5');
    expect(api.create).not.toHaveBeenCalled();
  });

  it('validates learning resource URLs', async () => {
    component.type = 'LEARNING';
    component.draft.title = 'Angular course';
    component.draft.resourceUrl = 'javascript:alert(1)';
    await component.save();
    expect(component.error).toContain('HTTPS');
    expect(api.create).not.toHaveBeenCalled();
  });

  it('sets editor permission based on assigned roles', () => {
    expect(component.canManage).toBe(true);
  });
});
