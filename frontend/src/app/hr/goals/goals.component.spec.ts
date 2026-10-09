import { ComponentFixture, TestBed } from '@angular/core/testing';
import { beforeEach, describe, expect, it, vi } from 'vitest';
import { GoalsComponent } from './goals.component';
import { HrApi } from '../../services/hr-api.service';

describe('GoalsComponent', () => {
  let fixture: ComponentFixture<GoalsComponent>;
  let component: GoalsComponent;
  const api = {
    goals: vi.fn(async () => [{ id: 1, employeeId: 5, title: 'Complete onboarding', currentValue: 2, targetValue: 3 }]),
    employees: vi.fn(async () => [{ id: 5, employeeNumber: 'E005', firstName: 'Ravi', lastName: 'Kumar', email: 'ravi@example.com' }])
  };

  beforeEach(async () => {
    vi.clearAllMocks();
    await TestBed.configureTestingModule({
      imports: [GoalsComponent],
      providers: [{ provide: HrApi, useValue: api }]
    }).compileComponents();
    fixture = TestBed.createComponent(GoalsComponent);
    component = fixture.componentInstance;
  });

  it('loads goals and employee names', async () => {
    await component.ngOnInit();
    expect(component.goals).toHaveLength(1);
    expect(component.employeeName(5)).toBe('Ravi Kumar');
  });

  it('keeps goal loading errors visible while allowing initialization to complete', async () => {
    api.goals.mockRejectedValueOnce(new Error('network'));
    await component.ngOnInit();
    expect(component.loadError).toContain('Goals could not be loaded.');
  });
});
