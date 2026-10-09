import { ComponentFixture, TestBed } from '@angular/core/testing';
import { beforeEach, describe, expect, it, vi } from 'vitest';
import { GoalsComponent } from './goals.component';
import { HrApi } from '../../services/hr-api.service';
import { AuthService } from '../../services/auth.service';

describe('GoalsComponent', () => {
  let fixture: ComponentFixture<GoalsComponent>;
  let component: GoalsComponent;
  const api = {
    goals: vi.fn(async () => [{ id: 1, employeeId: 5, title: 'Complete onboarding', currentValue: 2, targetValue: 3 }]),
    employees: vi.fn(async () => [{ id: 5, employeeNumber: 'E005', firstName: 'Ravi', lastName: 'Kumar', email: 'ravi@example.com', username: 'ravi' }]),
    createGoal: vi.fn(async () => ({}))
  };

  const auth = { username: 'ravi', hasRole: (role: string) => role === 'EMPLOYEE' };

  beforeEach(async () => {
    vi.clearAllMocks();
    await TestBed.configureTestingModule({
      imports: [GoalsComponent],
      providers: [{ provide: HrApi, useValue: api }, { provide: AuthService, useValue: auth }]
    }).compileComponents();
    fixture = TestBed.createComponent(GoalsComponent);
    component = fixture.componentInstance;
  });

  it('loads goals and employee names', async () => {
    await component.ngOnInit();
    expect(component.goals).toHaveLength(1);
    expect(component.employeeName(5)).toBe('Ravi Kumar');
  });

  it('creates a goal for the signed-in employee and reloads', async () => {
    await component.ngOnInit();
    component.openForm();
    component.draft.title = 'Complete onboarding';
    component.draft.targetValue = 3;
    await component.saveGoal();
    expect(api.createGoal).toHaveBeenCalledWith(expect.objectContaining({
      employeeId: 5, title: 'Complete onboarding', targetValue: 3
    }));
    expect(component.formOpen).toBe(false);
  });

  it('does not submit a goal without a title or employee', async () => {
    component.draft = { employeeId: 0, title: ' ', targetValue: 1 };
    await component.saveGoal();
    expect(api.createGoal).not.toHaveBeenCalled();
    expect(component.actionError).toContain('goal title');
  });

  it('keeps goal loading errors visible while allowing initialization to complete', async () => {
    api.goals.mockRejectedValueOnce(new Error('network'));
    await component.ngOnInit();
    expect(component.loadError).toContain('Goals could not be loaded.');
  });

  it('renders goals after the initial asynchronous load', async () => {
    fixture.detectChanges();
    await fixture.whenStable();
    fixture.detectChanges();
    expect(fixture.nativeElement.textContent).toContain('Complete onboarding');
    expect(fixture.nativeElement.textContent).toContain('Ravi Kumar');
  });
});
