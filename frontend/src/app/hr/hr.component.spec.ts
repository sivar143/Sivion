import { ComponentFixture, TestBed } from '@angular/core/testing';
import { beforeEach, describe, expect, it } from 'vitest';
import { HrComponent } from './hr.component';
import { HrApi } from '../services/hr-api.service';
import { AuthService } from '../services/auth.service';

describe('HrComponent', () => {
  let fixture: ComponentFixture<HrComponent>;
  let component: HrComponent;

  const api = {
    employees: async () => [],
    departments: async () => [],
    designations: async () => [],
    attendance: async () => [],
    leaves: async () => [],
    goals: async () => [],
    payslips: async () => []
  };

  const auth = {
    hasRole: (role: string) => role === 'ADMIN'
  };

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [HrComponent],
      providers: [
        { provide: HrApi, useValue: api },
        { provide: AuthService, useValue: auth }
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(HrComponent);
    component = fixture.componentInstance;
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should expose both organization creation actions', () => {
    component.section = 'organization';
    fixture.detectChanges();

    const buttons = Array.from(
      fixture.nativeElement.querySelectorAll('button')
    ) as HTMLButtonElement[];

    expect(buttons.some(button => button.textContent?.includes('+ Department'))).toBe(true);
    expect(buttons.some(button => button.textContent?.includes('+ Designation'))).toBe(true);
  });

  it('should render a load error accessibly', async () => {
    fixture.detectChanges();
    await fixture.whenStable();

    component.loadError = 'Some HR data could not be loaded.';
    fixture.detectChanges();

    expect(fixture.nativeElement.querySelector('[role="alert"]')?.textContent)
      .toContain('Some HR data could not be loaded.');
  });
});
