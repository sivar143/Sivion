import { ComponentFixture, TestBed } from '@angular/core/testing';
import { beforeEach, describe, expect, it } from 'vitest';
import { AppComponent } from './app.component';
import { CrmApi } from './services/crm-api.service';
import { AuthService } from './services/auth.service';
import { WORKSPACES } from './workspace-config';
import { MarketingCampaignsApi } from './marketing/marketing-campaigns-api.service';

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
        { provide: AuthService, useValue: auth },
        { provide: MarketingCampaignsApi, useValue: { list: async () => [] } }
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

  it('should map the sales workspace entry to customers and keep it highlighted', () => {
    component.workspace = WORKSPACES['sales-manager'];
    component.select('sales');
    expect(component.activeItem).toBe('customers');
    expect(component.activeNavigationItem).toBe('customers');
  });

  it('keeps Sales, Procurement and Finance highlighted for nested module sections in the admin menu', () => {
    component.workspace = WORKSPACES.admin;

    component.select('sales');
    expect(component.activeNavigationItem).toBe('sales');

    component.select('leads');
    expect(component.activeNavigationItem).toBe('sales');
    expect(component.currentItem?.label).toBe('Sales');
    expect(component.currentItem?.description).toBe('Customers, leads, opportunities and orders');

    component.select('procurement');
    expect(component.activeNavigationItem).toBe('procurement');
    expect(component.currentItem?.label).toBe('Procurement');
    expect(component.currentItem?.description).toBe('Suppliers, purchasing and goods received');

    component.select('goods-received');
    expect(component.activeNavigationItem).toBe('procurement');
    expect(component.currentItem?.label).toBe('Procurement');

    component.select('finance');
    expect(component.activeNavigationItem).toBe('finance');
    expect(component.currentItem?.label).toBe('Finance');
    expect(component.currentItem?.description).toBe('Invoices, payments, expenses and reports');

    component.select('payments');
    expect(component.activeNavigationItem).toBe('finance');
    expect(component.currentItem?.label).toBe('Finance');
  });

  it('keeps the selected navigation item highlighted until another section is selected', () => {
    component.workspace = WORKSPACES.admin;
    component.select('attendance');
    expect(component.activeNavigationItem).toBe('attendance');

    component.select('employees');
    expect(component.activeNavigationItem).toBe('employees');
  });

  it('routes sales reports to the dedicated reports component, not HR or the generic CRM view', () => {
    component.workspace = WORKSPACES['sales-manager'];
    component.activeItem = 'goals';
    expect(component.isHr).toBe(false);
    expect(component.isCrmItem).toBe(true);

    component.activeItem = 'reports';
    expect(component.isHr).toBe(false);
    expect(component.isReports).toBe(true);
    expect(component.isCrmItem).toBe(false);
  });

  it('keeps employee goals in the HR module', () => {
    component.workspace = WORKSPACES.employee;
    component.activeItem = 'goals';
    expect(component.isHr).toBe(true);
  });

  it('routes only payroll, not finance reports, through the HR shell', () => {
    component.workspace = WORKSPACES.finance;
    component.activeItem = 'payroll';
    expect(component.isHr).toBe(true);
    component.activeItem = 'reports';
    expect(component.isHr).toBe(false);
    expect(component.isCrmItem).toBe(false);
  });

  it('shows the audit trail only for the admin workspace', () => {
    component.workspace = WORKSPACES.admin;
    component.select('audit');
    expect(component.isAudit).toBe(true);
    expect(component.activeNavigationItem).toBe('audit');

    component.workspace = WORKSPACES.employee;
    expect(component.isAudit).toBe(false);
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
