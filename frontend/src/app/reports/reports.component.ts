import { CommonModule } from '@angular/common';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { ChangeDetectorRef, Component, Input, OnInit, inject } from '@angular/core';
import { firstValueFrom } from 'rxjs';
import { AuthService } from '../services/auth.service';
import { CrmApi } from '../services/crm-api.service';
import { InventoryApi } from '../services/inventory-api.service';

interface ReportMetric {
  label: string;
  value: string | number;
  detail: string;
}

@Component({
  selector: 'app-reports',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './reports.component.html',
  styleUrls: ['./reports.component.scss']
})
export class ReportsComponent implements OnInit {
  @Input() workspaceId = '';

  private readonly http = inject(HttpClient);
  private readonly auth = inject(AuthService);
  private readonly crm = inject(CrmApi);
  private readonly inventory = inject(InventoryApi);
  private readonly cdr = inject(ChangeDetectorRef);

  metrics: ReportMetric[] = [];
  loading = false;

  get workspaceTitle(): string {
    const labels: Record<string, string> = {
      'sales-manager': 'Sales management',
      'sales-user': 'Sales',
      marketing: 'Marketing',
      'inventory-manager': 'Inventory management',
      'inventory-user': 'Inventory',
      procurement: 'Procurement',
      finance: 'Finance'
    };
    return labels[this.workspaceId] || 'Workspace';
  }
  loadError = '';
  readonly money = new Intl.NumberFormat('en-IN', {
    style: 'currency',
    currency: 'INR',
    maximumFractionDigits: 0
  });

  async ngOnInit(): Promise<void> {
    await this.reload();
  }

  async reload(): Promise<void> {
    this.loading = true;
    this.loadError = '';
    this.metrics = [];
    this.cdr.markForCheck();

    try {
      switch (this.workspaceId) {
        case 'sales-manager':
        case 'sales-user':
        case 'marketing':
          await this.loadSalesReport();
          break;
        case 'inventory-manager':
        case 'inventory-user':
          await this.loadInventoryReport();
          break;
        case 'procurement':
          await this.loadProcurementReport();
          break;
        case 'finance':
          await this.loadFinanceReport();
          break;
        default:
          this.loadError = 'Reports are not configured for this workspace yet.';
      }
    } catch {
      this.loadError = 'Some report data could not be loaded. Refresh to try again.';
    } finally {
      this.loading = false;
      this.cdr.markForCheck();
    }
  }

  private async loadSalesReport(): Promise<void> {
    const results = await Promise.allSettled([this.crm.leads(), this.crm.opportunities()]);
    const failures: string[] = [];
    const leads = results[0].status === 'fulfilled' ? results[0].value : [];
    const opportunities = results[1].status === 'fulfilled' ? results[1].value : [];
    if (results[0].status === 'rejected') failures.push('leads');
    if (results[1].status === 'rejected') failures.push('opportunities');

    const openLeads = leads.filter(lead => !['CONVERTED', 'CLOSED'].includes(lead.status));
    const openOpportunities = opportunities.filter(
      opportunity => !['CLOSED_WON', 'CLOSED_LOST'].includes(opportunity.stage)
    );
    const weightedPipeline = openOpportunities.reduce(
      (total, opportunity) => total + Number(opportunity.amount || 0) * Number(opportunity.probability || 0) / 100,
      0
    );
    const campaignLeads = leads.filter(lead => lead.source === 'CAMPAIGN');

    this.metrics = [
      { label: 'Total leads', value: leads.length, detail: 'Leads returned by the current user’s authorized CRM scope' },
      { label: 'Open leads', value: openLeads.length, detail: 'Leads not converted or closed' },
      { label: 'Open opportunities', value: openOpportunities.length, detail: 'Opportunities still in progress' },
      { label: 'Weighted pipeline', value: this.money.format(weightedPipeline), detail: 'Open opportunity amount weighted by probability' }
    ];
    if (this.workspaceId === 'marketing') {
      this.metrics.push({
        label: 'Campaign-source leads',
        value: campaignLeads.length,
        detail: 'Leads whose recorded source is CAMPAIGN; not a campaign attribution model'
      });
    }
    if (failures.length) this.loadError = `Unable to load ${failures.join(' and ')}; displayed metrics use available data.`;
  }

  private async loadInventoryReport(): Promise<void> {
    const results = await Promise.allSettled([
      this.inventory.warehouses(),
      this.inventory.materials(),
      this.inventory.transactions(),
      this.inventory.history()
    ]);
    const failures: string[] = [];
    const warehouses = this.valueOrEmpty(results[0], 'warehouses', failures);
    const materials = this.valueOrEmpty(results[1], 'materials', failures);
    const transactions = this.valueOrEmpty(results[2], 'stock transactions', failures);
    const dispatches = this.valueOrEmpty(results[3], 'dispatch history', failures);
    const totalStock = materials.reduce((sum: number, material: any) => sum + Number(material.quantity || 0), 0);
    const lowStock = materials.filter((material: any) => Number(material.quantity || 0) <= Number(material.reorderLevel || 0));
    this.metrics = [
      { label: 'Warehouses', value: warehouses.length, detail: 'Configured warehouses' },
      { label: 'Materials', value: materials.length, detail: 'Material records returned by inventory API' },
      { label: 'Units in stock', value: totalStock.toLocaleString('en-IN'), detail: 'Sum of returned material quantities' },
      { label: 'At/below reorder level', value: lowStock.length, detail: 'Materials at or below their configured reorder level' },
      { label: 'Stock transactions', value: transactions.length, detail: 'Recorded stock movements' },
      { label: 'Dispatches', value: dispatches.length, detail: 'Recorded dispatches' }
    ];
    if (failures.length) this.loadError = `Unable to load ${failures.join(', ')}; displayed metrics use available data.`;
  }

  private async loadProcurementReport(): Promise<void> {
    const options = await this.requestOptions();
    const endpoints = ['suppliers', 'requests', 'orders', 'goods-received'];
    const results = await Promise.allSettled(
      endpoints.map(endpoint => firstValueFrom(this.http.get<any[]>(`/api/v1/procurement/${endpoint}`, options)))
    );
    const failures: string[] = [];
    const [suppliers, requests, orders, receipts] = results.map((result, index) =>
      this.valueOrEmpty(result, endpoints[index], failures)
    );
    const pendingRequests = requests.filter((request: any) => ['PENDING', 'SUBMITTED', 'REQUESTED'].includes(request.status));
    const openOrders = orders.filter((order: any) => ['ISSUED', 'PARTIALLY_RECEIVED'].includes(order.status));
    const orderedValue = orders.reduce((sum: number, order: any) =>
      sum + Number(order.totalAmount ?? order.amount ?? 0), 0);
    this.metrics = [
      { label: 'Suppliers', value: suppliers.length, detail: 'Supplier records' },
      { label: 'Purchase requests', value: requests.length, detail: 'Requests recorded' },
      { label: 'Pending requests', value: pendingRequests.length, detail: 'Requests awaiting a later workflow state' },
      { label: 'Open purchase orders', value: openOrders.length, detail: 'Orders issued or partially received' },
      { label: 'Purchase order value', value: this.money.format(orderedValue), detail: 'Sum of stored order totals where available' },
      { label: 'Goods receipts', value: receipts.length, detail: 'Recorded receipt transactions' }
    ];
    if (failures.length) this.loadError = `Unable to load ${failures.join(', ')}; displayed metrics use available data.`;
  }

  private async loadFinanceReport(): Promise<void> {
    const options = await this.requestOptions();
    const endpoints = ['invoices', 'payments', 'expenses'];
    const results = await Promise.allSettled(
      endpoints.map(endpoint => firstValueFrom(this.http.get<any[]>(`/api/v1/finance/${endpoint}`, options)))
    );
    const failures: string[] = [];
    const [invoices, payments, expenses] = results.map((result, index) =>
      this.valueOrEmpty(result, endpoints[index], failures)
    );
    const invoiceTotal = invoices.reduce((sum: number, invoice: any) => sum + Number(invoice.amount || 0), 0);
    const paymentTotal = payments.reduce((sum: number, payment: any) => sum + Number(payment.amount || 0), 0);
    const expenseTotal = expenses.reduce((sum: number, expense: any) => sum + Number(expense.amount || 0), 0);
    const unpaidInvoices = invoices.filter((invoice: any) => invoice.status !== 'PAID');
    const unpaidValue = unpaidInvoices.reduce((sum: number, invoice: any) => sum + Number(invoice.amount || 0), 0);
    this.metrics = [
      { label: 'Invoice value', value: this.money.format(invoiceTotal), detail: 'Sum of returned invoice amounts' },
      { label: 'Payments received', value: this.money.format(paymentTotal), detail: 'Sum of recorded payment amounts' },
      { label: 'Recorded expenses', value: this.money.format(expenseTotal), detail: 'Sum of recorded expense amounts' },
      { label: 'Unpaid invoices', value: unpaidInvoices.length, detail: 'Invoices whose status is not PAID' },
      { label: 'Unpaid invoice value', value: this.money.format(unpaidValue), detail: 'Gross value of unpaid invoices; not an accounting reconciliation' }
    ];
    if (failures.length) this.loadError = `Unable to load ${failures.join(', ')}; displayed metrics use available data.`;
  }

  private async requestOptions(): Promise<{ headers: HttpHeaders }> {
    return {
      headers: new HttpHeaders({
        Authorization: `Bearer ${await this.auth.token()}`,
        'X-Tenant-ID': '1'
      })
    };
  }

  private valueOrEmpty<T>(
    result: PromiseSettledResult<T[]>,
    label: string,
    failures: string[]
  ): T[] {
    if (result.status === 'fulfilled') return result.value;
    failures.push(label);
    return [];
  }
}
