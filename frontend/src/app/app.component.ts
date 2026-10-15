import {
  Component, OnInit, inject
} from '@angular/core';
import {
  CommonModule
} from '@angular/common';
import {
  FormsModule
} from '@angular/forms';
import {
  CrmApi, Customer, Contact, Lead, Opportunity, Activity
} from './services/crm-api.service';
import {
  AuthService
} from './services/auth.service';
import {
  ROLE_TO_WORKSPACE, WORKSPACES, WorkspaceDefinition
} from './workspace-config';
import {
  InventoryComponent
} from './inventory/inventory.component';
import {
  HrComponent
} from './hr/hr.component';
import {
  ProcurementComponent
} from './procurement/procurement.component';
import {
  FinanceComponent
} from './finance/finance.component';
import {
  SalesOrderComponent
} from './sales-order/sales-order.component';
import {
  AuditComponent
} from './audit/audit.component';
import { NotificationCenterComponent } from './notifications/notification-center.component';
import { ReportsComponent } from './reports/reports.component';
import { MarketingCampaignsComponent } from './marketing/marketing-campaigns.component';
import { MarketingCampaign, MarketingCampaignsApi } from './marketing/marketing-campaigns-api.service';
import { AdministrationComponent } from './administration/administration.component';
import { SupplyChainComponent } from './supply-chain/supply-chain.component';
import { getDialogErrorMessage } from './shared/dialog-error.util';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    InventoryComponent,
    HrComponent,
    ProcurementComponent,
    FinanceComponent,
    SalesOrderComponent,
    AuditComponent,
    NotificationCenterComponent,
    ReportsComponent,
    MarketingCampaignsComponent,
    AdministrationComponent,
    SupplyChainComponent
  ],
  templateUrl: './app.component.html'
})
export class AppComponent implements OnInit {
  api = inject(CrmApi);
  sidebarCollapsed = false;
  userMenuOpen = false;
  auth = inject(AuthService);
  private readonly campaignApi = inject(MarketingCampaignsApi);
  activeItem = 'overview';
  workspace: WorkspaceDefinition = WORKSPACES.employee;
  customers: Customer[] = [];
  contacts: Contact[] = [];
  leads: Lead[] = [];
  opportunities: Opportunity[] = [];
  activities: Activity[] = [];
  campaigns: MarketingCampaign[] = [];
  campaignLoadError = '';
  customerQuery = '';
  newCustomer = false;
  newContact = false;
  newLead = false;
  newOpp = false;
  newActivity = false;
  dialogError = '';
  submitting = false;
  customerDraft: Customer = {
    code: '',
    name: '',
    status: 'ACTIVE'
  };
  contactDraft: Contact = {
    customerId: 0,
    firstName: '',
    primaryContact: false,
    status: 'ACTIVE'
  };
  leadDraft: Lead = {
    name: '',
    source: 'WEBSITE',
    status: 'NEW',
    rating: 'WARM'
  };
  oppDraft: Opportunity = {
    name: '',
    stage: 'QUALIFICATION',
    probability: 20
  };
  activityDraft: Activity = {
    type: 'CALL',
    subject: ''
  };
  leadStatuses = ['NEW', 'CONTACTED', 'QUALIFIED', 'CONVERTED'];
  oppStages = [
    'QUALIFICATION',
    'NEEDS_ANALYSIS',
    'PROPOSAL',
    'NEGOTIATION',
    'CLOSED_WON',
    'CLOSED_LOST'
  ];

  async ngOnInit() {
    const id = this.resolveWorkspace();
    this.workspace = WORKSPACES[id];
    this.activeItem = this.workspace.items[0]?.id || 'overview';
    const initialLoads: Promise<void>[] = [
      this.loadCustomers(),
      this.loadLeads(),
      this.loadOpps()
    ];
    if (id === 'marketing') {
      initialLoads.push(this.loadCampaigns());
    }
    await Promise.all(initialLoads);
  }

  // Resolve the first workspace granted by Keycloak. Backend authorization remains authoritative.
  private resolveWorkspace() {
    for (const role of this.auth.roles) {
      if (ROLE_TO_WORKSPACE[role]) {
        return ROLE_TO_WORKSPACE[role];
      }
    }
    return 'employee' as const;
  }

  get currentItem() {
    // In the Administration workspace, nested screens retain the parent module heading.
    // Workspaces with direct navigation (for example, Sales Management) keep their own item heading.
    const headerItemId = this.activeNavigationItem;
    return this.workspace.items.find(item => item.id === headerItemId)
      || this.workspace.items.find(item => item.id === this.activeItem);
  }

  // Resolve nested module sections back to their parent menu item when the workspace uses module-level navigation.
  get activeNavigationItem(): string {
    const menuIds = new Set(this.workspace.items.map(item => item.id));
    const sectionGroups: Record<string, string[]> = {
      sales: ['customers', 'contacts', 'leads', 'opportunities', 'activities', 'meetings', 'sales-orders'],
      procurement: ['suppliers', 'requests', 'orders', 'goods-received'],
      finance: ['invoices', 'payments', 'expenses']
    };

    for (const [parentId, sectionIds] of Object.entries(sectionGroups)) {
      if (menuIds.has(parentId) && (this.activeItem === parentId || sectionIds.includes(this.activeItem))) {
        return parentId;
      }
    }

    return this.activeItem;
  }

  get isCrmItem() {
    const crmWorkspaces = ['sales-manager', 'sales-user', 'marketing'];
    return crmWorkspaces.includes(this.workspace.id) && [
      'sales',
      'customers',
      'contacts',
      'leads',
      'opportunities',
      'activities',
      'meetings',
      'goals',
      'performance',
      'team'
    ].includes(this.activeItem) && !this.isHr;
  }

  get isSupplyChain() {
    return this.activeItem === 'supply-chain';
  }

  get isInventory() {
    return [
      'inventory',
      'materials',
      'stock-in',
      'adjustments',
      'dispatch',
      'dispatch-history'
    ].includes(this.activeItem);
  }

  get isHr() {
    // Several section IDs (goals, meetings, reports) are shared with CRM.
    // Only route them to HR when the active workspace actually owns HR workflows.
    if (this.workspace.id === 'finance') {
      // Finance has read-only payroll, but its reports belong to the finance workspace.
      return this.activeItem === 'payroll';
    }
    const hrWorkspaces = ['admin', 'hr-admin', 'hr-user', 'manager', 'employee'];
    return hrWorkspaces.includes(this.workspace.id) && [
      'employees',
      'organization',
      'attendance',
      'leaves',
      'payroll',
      'holidays',
      'meetings',
      'goals',
      'performance',
      'knowledge',
      'documents',
      'reports'
    ].includes(this.activeItem);
  }

  get isProcurement() {
    return ['suppliers', 'requests', 'orders', 'goods-received'].includes(this.activeItem);
  }

  get isFinance() {
    return ['invoices', 'payments', 'expenses'].includes(this.activeItem);
  }

  get isSalesOrders() {
    return this.activeItem === 'sales-orders';
  }

  // Audit history is an administrative capability and is exposed only in the admin workspace.
  get isReports(): boolean {
    return (this.activeItem === 'reports' || (this.workspace.id === 'marketing' && this.activeItem === 'analytics')) && !this.isHr;
  }

  get isAdministration(): boolean {
    return this.workspace.id === 'admin' && this.activeItem === 'administration';
  }

  get isMarketingCampaigns(): boolean {
    return ['admin', 'marketing'].includes(this.workspace.id)
      && ['marketing', 'campaigns'].includes(this.activeItem);
  }

  get isAudit(): boolean {
    return this.workspace.id === 'admin' && this.activeItem === 'audit';
  }

  get openLeads() {
    return this.leads
      .filter(lead => !['CONVERTED', 'CLOSED'].includes(lead.status))
      .length;
  }

  get pipeline() {
    return this.opportunities
      .filter(opportunity => !['CLOSED_LOST', 'CLOSED_WON'].includes(opportunity.stage))
      .reduce(
        (total, opportunity) =>
          total + (opportunity.amount || 0) * (opportunity.probability || 0) / 100,
        0
      );
  }

  get winRate() {
    const closedOpportunities = this.opportunities.filter(
      opportunity => ['CLOSED_WON', 'CLOSED_LOST'].includes(opportunity.stage)
    );
    return closedOpportunities.length
      ? Math.round(
          closedOpportunities.filter(opportunity => opportunity.stage === 'CLOSED_WON').length
          * 100 / closedOpportunities.length
        )
      : 0;
  }

  toggleSidebar() {
    this.sidebarCollapsed = !this.sidebarCollapsed;
  }

  // Keep account actions in the top-right account menu instead of the workspace navigation.
  toggleUserMenu() {
    this.userMenuOpen = !this.userMenuOpen;
  }

  // Clear transient forms when switching modules so stale drafts are not carried into another workflow.
  select(id: string) {
    if (id === 'sales') {
      id = 'customers';
    }
    if (id === 'procurement') {
      id = 'suppliers';
    }
    if (id === 'finance') {
      id = 'invoices';
    }

    this.activeItem = id;
    this.userMenuOpen = false;
    this.newCustomer = false;
    this.newContact = false;
    this.newLead = false;
    this.newOpp = false;
    this.newActivity = false;

    if (id === 'contacts') {
      this.loadContacts();
    }
    if (id === 'activities' || id === 'meetings') {
      this.loadActivities();
    }
  }

  leadsFor(status: string) {
    return this.leads.filter(lead => lead.status === status);
  }

  oppsFor(stage: string) {
    return this.opportunities.filter(opportunity => opportunity.stage === stage);
  }

  customerName(id: number) {
    return this.customers.find(customer => customer.id === id)?.name || `Customer #${id}`;
  }

  async loadCustomers() {
    this.customers = await this.api.customers(this.customerQuery);
  }

  async loadContacts() {
    this.contacts = await this.api.contacts();
  }

  async loadLeads() {
    this.leads = await this.api.leads();
  }

  async loadOpps() {
    this.opportunities = await this.api.opportunities();
  }

  async loadCampaigns(): Promise<void> {
    try {
      this.campaigns = await this.campaignApi.list();
      this.campaignLoadError = '';
    } catch {
      this.campaigns = [];
      this.campaignLoadError = 'Campaign options could not be loaded.';
    }
  }

  onLeadSourceChange(source: string): void {
    this.leadDraft.source = source;
    if (source !== 'CAMPAIGN') {
      this.leadDraft.campaignId = undefined;
    }
  }

  async loadActivities() {
    this.activities = await this.api.activities();
  }

  async saveCustomer() {
    if (this.submitting) return;
    this.dialogError = '';
    this.submitting = true;
    try {
      await this.api.createCustomer(this.customerDraft);
      this.newCustomer = false;
      this.customerDraft = { code: '', name: '', status: 'ACTIVE' };
      await this.loadCustomers();
    } catch (error: unknown) {
      this.dialogError = getDialogErrorMessage(error, 'The customer could not be created. Please review the details and try again.');
    } finally {
      this.submitting = false;
    }
  }

  async saveContact() {
    if (this.submitting) return;
    this.dialogError = '';
    this.submitting = true;
    try {
      await this.api.createContact(this.contactDraft);
      this.newContact = false;
      this.contactDraft = { customerId: 0, firstName: '', primaryContact: false, status: 'ACTIVE' };
      await this.loadContacts();
    } catch (error: unknown) {
      this.dialogError = getDialogErrorMessage(error, 'The contact could not be created. Please review the details and try again.');
    } finally {
      this.submitting = false;
    }
  }

  async saveLead() {
    if (this.submitting) return;
    this.dialogError = '';
    this.submitting = true;
    try {
      await this.api.createLead(this.leadDraft);
      this.newLead = false;
      this.leadDraft = { name: '', source: 'WEBSITE', campaignId: undefined, status: 'NEW', rating: 'WARM' };
      await this.loadLeads();
    } catch (error: unknown) {
      this.dialogError = getDialogErrorMessage(error, 'The lead could not be created. Please review the details and try again.');
    } finally {
      this.submitting = false;
    }
  }

  async saveOpp() {
    if (this.submitting) return;
    this.dialogError = '';
    this.submitting = true;
    try {
      await this.api.createOpportunity(this.oppDraft);
      this.newOpp = false;
      this.oppDraft = { name: '', stage: 'QUALIFICATION', probability: 20 };
      await this.loadOpps();
    } catch (error: unknown) {
      this.dialogError = getDialogErrorMessage(error, 'The opportunity could not be created. Please review the details and try again.');
    } finally {
      this.submitting = false;
    }
  }

  async saveActivity() {
    if (this.submitting) return;
    this.dialogError = '';
    this.submitting = true;
    try {
      await this.api.createActivity(this.activityDraft);
      this.newActivity = false;
      this.activityDraft = { type: 'CALL', subject: '' };
      await this.loadActivities();
    } catch (error: unknown) {
      this.dialogError = getDialogErrorMessage(error, 'The activity could not be created. Please review the details and try again.');
    } finally {
      this.submitting = false;
    }
  }

  async moveLead(lead: Lead, status: string) {
    if (!lead.id || lead.status === status) {
      return;
    }
    await this.api.updateLeadStatus(lead.id, status);
    await this.loadLeads();
  }

  async moveOpportunity(opportunity: Opportunity, stage: string) {
    if (!opportunity.id || opportunity.stage === stage) {
      return;
    }
    await this.api.updateOpportunityStage(opportunity.id, stage);
    await this.loadOpps();
  }
}
