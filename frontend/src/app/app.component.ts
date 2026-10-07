import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { CrmApi, Customer, Contact, Lead, Opportunity, Activity } from './services/crm-api.service';
import { AuthService } from './services/auth.service';
import { ROLE_TO_WORKSPACE, WORKSPACES, WorkspaceDefinition } from './workspace-config';
import { InventoryComponent } from './inventory/inventory.component';
import { HrComponent } from './hr/hr.component';
import { ProcurementComponent } from './procurement/procurement.component';
import { FinanceComponent } from './finance/finance.component';
import { SalesOrderComponent } from './sales-order/sales-order.component';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [CommonModule, FormsModule, InventoryComponent, HrComponent, ProcurementComponent, FinanceComponent, SalesOrderComponent],
  templateUrl: './app.component.html'
})
export class AppComponent implements OnInit {
  api = inject(CrmApi);
  sidebarCollapsed = false;
  auth = inject(AuthService);
  activeItem = 'overview';
  workspace: WorkspaceDefinition = WORKSPACES.employee;
  customers: Customer[] = [];
  contacts: Contact[] = [];
  leads: Lead[] = [];
  opportunities: Opportunity[] = [];
  activities: Activity[] = [];
  customerQuery = '';
  newCustomer = false;
  newContact = false;
  newLead = false;
  newOpp = false;
  newActivity = false;
  customerDraft: Customer = { code: '', name: '', status: 'ACTIVE' };
  contactDraft: Contact = { customerId: 0, firstName: '', primaryContact: false, status: 'ACTIVE' };
  leadDraft: Lead = { name: '', source: 'WEBSITE', status: 'NEW', rating: 'WARM' };
  oppDraft: Opportunity = { name: '', stage: 'QUALIFICATION', probability: 20 };
  activityDraft: Activity = { type: 'CALL', subject: '' };
  leadStatuses = ['NEW', 'CONTACTED', 'QUALIFIED', 'CONVERTED'];
  oppStages = ['QUALIFICATION', 'NEEDS_ANALYSIS', 'PROPOSAL', 'NEGOTIATION', 'CLOSED_WON', 'CLOSED_LOST'];

  async ngOnInit() {
    const id = this.resolveWorkspace();
    this.workspace = WORKSPACES[id];
    this.activeItem = this.workspace.items[0]?.id || 'overview';
    await Promise.all([this.loadCustomers(), this.loadLeads(), this.loadOpps()]);
  }

  private resolveWorkspace() {
    for (const role of this.auth.roles) {
      if (ROLE_TO_WORKSPACE[role]) return ROLE_TO_WORKSPACE[role];
    }
    return 'employee' as const;
  }

  get currentItem() { return this.workspace.items.find(x => x.id === this.activeItem); }
  get isCrmItem() { return ['sales', 'customers', 'contacts', 'leads', 'opportunities', 'activities', 'meetings', 'goals', 'performance', 'team', 'reports'].includes(this.activeItem); }
  get isInventory() { return ['inventory', 'materials', 'stock-in', 'adjustments', 'dispatch', 'dispatch-history'].includes(this.activeItem); }
  get isHr() { return ['employees', 'organization', 'attendance', 'leaves', 'holidays', 'knowledge', 'documents'].includes(this.activeItem); }
  get isProcurement() { return ['suppliers', 'requests', 'orders', 'goods-received'].includes(this.activeItem); }
  get isFinance() { return ['invoices', 'payments', 'expenses'].includes(this.activeItem); }
  get isSalesOrders() { return this.activeItem === 'sales-orders'; }
  get openLeads() { return this.leads.filter(x => !['CONVERTED', 'CLOSED'].includes(x.status)).length; }
  get pipeline() { return this.opportunities.filter(x => !['CLOSED_LOST', 'CLOSED_WON'].includes(x.stage)).reduce((a, x) => a + (x.amount || 0) * (x.probability || 0) / 100, 0); }
  get winRate() {
    const c = this.opportunities.filter(x => ['CLOSED_WON', 'CLOSED_LOST'].includes(x.stage));
    return c.length ? Math.round(c.filter(x => x.stage === 'CLOSED_WON').length * 100 / c.length) : 0;
  }

  toggleSidebar() { this.sidebarCollapsed = !this.sidebarCollapsed; }

  select(id: string) {
    if (id === 'sales') id = 'customers';
    if (id === 'procurement') id = 'suppliers';
    if (id === 'finance') id = 'invoices';
    this.activeItem = id;
    this.newCustomer = this.newContact = this.newLead = this.newOpp = this.newActivity = false;
    if (id === 'contacts') this.loadContacts();
    if (id === 'activities' || id === 'meetings') this.loadActivities();
  }

  leadsFor(s: string) { return this.leads.filter(x => x.status === s); }
  oppsFor(s: string) { return this.opportunities.filter(x => x.stage === s); }
  customerName(id: number) { return this.customers.find(c => c.id === id)?.name || `Customer #${id}`; }
  async loadCustomers() { this.customers = await this.api.customers(this.customerQuery); }
  async loadContacts() { this.contacts = await this.api.contacts(); }
  async loadLeads() { this.leads = await this.api.leads(); }
  async loadOpps() { this.opportunities = await this.api.opportunities(); }
  async loadActivities() { this.activities = await this.api.activities(); }
  async saveCustomer() { await this.api.createCustomer(this.customerDraft); this.newCustomer = false; this.customerDraft = { code: '', name: '', status: 'ACTIVE' }; await this.loadCustomers(); }
  async saveContact() { await this.api.createContact(this.contactDraft); this.newContact = false; this.contactDraft = { customerId: 0, firstName: '', primaryContact: false, status: 'ACTIVE' }; await this.loadContacts(); }
  async saveLead() { await this.api.createLead(this.leadDraft); this.newLead = false; this.leadDraft = { name: '', source: 'WEBSITE', status: 'NEW', rating: 'WARM' }; await this.loadLeads(); }
  async saveOpp() { await this.api.createOpportunity(this.oppDraft); this.newOpp = false; this.oppDraft = { name: '', stage: 'QUALIFICATION', probability: 20 }; await this.loadOpps(); }
  async saveActivity() { await this.api.createActivity(this.activityDraft); this.newActivity = false; this.activityDraft = { type: 'CALL', subject: '' }; await this.loadActivities(); }
  async moveLead(l: Lead, status: string) { if (!l.id || l.status === status) return; await this.api.updateLeadStatus(l.id, status); await this.loadLeads(); }
  async moveOpportunity(o: Opportunity, stage: string) { if (!o.id || o.stage === stage) return; await this.api.updateOpportunityStage(o.id, stage); await this.loadOpps(); }
}