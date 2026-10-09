import { ComponentFixture, TestBed } from '@angular/core/testing';
import { beforeEach, describe, expect, it } from 'vitest';
import { AppComponent } from './app.component';
import { CrmApi } from './services/crm-api.service';
import { AuthService } from './services/auth.service';

describe('AppComponent', () => {
  let fixture: ComponentFixture<AppComponent>;
  let component: AppComponent;

  const crmApi = {
    customers: async () => [],
    contacts: async () => [],
    leads: async () => [],
    opportunities: async () => [],
    activities: async () => [],
    createCustomer: async () => ({}),
    createContact: async () => ({}),
    createLead: async () => ({}),
    createOpportunity: async () => ({}),
    createActivity: async () => ({}),
    updateLeadStatus: async () => ({}),
    updateOpportunityStage: async () => ({})
  };

  const auth = {
    username: 'test-user',
    roles: ['ADMIN'],
    logout: () => {},
    token: async () => 'test-token',
    hasRole: (role: string) => role === 'ADMIN'
  };

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [AppComponent],
      providers: [
        { provide: CrmApi, useValue: crmApi },
        { provide: AuthService, useValue: auth }
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(AppComponent);
    component = fixture.componentInstance;
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should toggle the sidebar', () => {
    expect(component.sidebarCollapsed).toBe(false);
    component.toggleSidebar();
    expect(component.sidebarCollapsed).toBe(true);
  });

  it('should map the sales workspace entry to customers', () => {
    component.select('sales');
    expect(component.activeItem).toBe('customers');
  });

  it('should calculate open leads and win rate', () => {
    component.leads = [
      { status: 'NEW' } as any,
      { status: 'CONVERTED' } as any
    ];
    component.opportunities = [
      { stage: 'CLOSED_WON' } as any,
      { stage: 'CLOSED_LOST' } as any
    ];

    expect(component.openLeads).toBe(1);
    expect(component.winRate).toBe(50);
  });
});
