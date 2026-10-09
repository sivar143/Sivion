import { CommonModule } from '@angular/common';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Component, OnInit, inject } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { firstValueFrom } from 'rxjs';

import { AuthService } from '../services/auth.service';
import { CrmApi, Customer } from '../services/crm-api.service';

@Component({
  selector: 'app-finance',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './finance.component.html',
  styles: [`
    .sub {
      margin-top: 28px;
    }
  `]
})
export class FinanceComponent implements OnInit {
  private readonly baseUrl = '/api/v1/finance';

  readonly http = inject(HttpClient);
  readonly auth = inject(AuthService);
  readonly crm = inject(CrmApi);

  invoices: any[] = [];
  payments: any[] = [];
  expenses: any[] = [];
  customers: Customer[] = [];
  loadError = '';
  private readonly loadFailures = new Set<string>();

  mode = '';
  draft: any = {};

  async ngOnInit(): Promise<void> {
    await Promise.allSettled([this.reload(), this.loadCustomers()]);
  }

  private async loadCustomers(): Promise<void> {
    try {
      this.customers = await this.crm.customers('');
      this.setLoadFailure('customers', false);
    } catch {
      this.setLoadFailure('customers', true);
    }
  }

  private setLoadFailure(source: string, failed: boolean): void {
    if (failed) this.loadFailures.add(source);
    else this.loadFailures.delete(source);
    this.loadError = this.loadFailures.size
      ? `Unable to load ${Array.from(this.loadFailures).join(', ')}. Other available data is still shown.`
      : '';
  }

  private async requestOptions(): Promise<{ headers: HttpHeaders }> {
    const token = await this.auth.token();

    return {
      headers: new HttpHeaders({
        Authorization: `Bearer ${token}`,
        'X-Tenant-ID': '1'
      })
    };
  }

  async reload(): Promise<void> {
    try {
      const options = await this.requestOptions();
      this.setLoadFailure('finance data (authentication or API request failed)', false);
      const results = await Promise.allSettled([
        firstValueFrom(this.http.get<any[]>(`${this.baseUrl}/invoices`, options)),
        firstValueFrom(this.http.get<any[]>(`${this.baseUrl}/payments`, options)),
        firstValueFrom(this.http.get<any[]>(`${this.baseUrl}/expenses`, options))
      ]);
      if (results[0].status === 'fulfilled') this.invoices = results[0].value;
      this.setLoadFailure('invoices', results[0].status === 'rejected');
      if (results[1].status === 'fulfilled') this.payments = results[1].value;
      this.setLoadFailure('payments', results[1].status === 'rejected');
      if (results[2].status === 'fulfilled') this.expenses = results[2].value;
      this.setLoadFailure('expenses', results[2].status === 'rejected');
    } catch {
      this.setLoadFailure('finance data (authentication or API request failed)', true);
    }
  }

  get invoiceTotal(): number {
    return this.invoices.reduce(
      (total, invoice) => total + Number(invoice.amount ?? 0),
      0
    );
  }

  get paymentTotal(): number {
    return this.payments.reduce(
      (total, payment) => total + Number(payment.amount ?? 0),
      0
    );
  }

  get expenseTotal(): number {
    return this.expenses.reduce(
      (total, expense) => total + Number(expense.amount ?? 0),
      0
    );
  }

  get outstanding(): number {
    return Math.max(0, this.invoiceTotal - this.paymentTotal);
  }

  get unpaidInvoices(): any[] {
    return this.invoices.filter(invoice => invoice.status !== 'PAID');
  }

  customerName(id: number | undefined): string {
    if (!id) {
      return 'Customer not assigned';
    }

    return (
      this.customers.find(customer => customer.id === id)?.name
      ?? `Customer #${id}`
    );
  }

  invoiceName(id: number | undefined): string {
    if (!id) {
      return 'Invoice not assigned';
    }

    return (
      this.invoices.find(invoice => invoice.id === id)?.invoiceNumber
      ?? `Invoice #${id}`
    );
  }

  async submit(): Promise<void> {
    if (!this.mode) {
      return;
    }

    try {
      const options = await this.requestOptions();
      const path = this.getSubmissionPath();

      await firstValueFrom(
        this.http.post(
          `${this.baseUrl}/${path}`,
          this.draft,
          options
        )
      );

      this.closeModal();
      await this.reload();
    } catch (error: any) {
      alert(error?.error?.message ?? 'Request failed');
    }
  }

  private getSubmissionPath(): 'invoices' | 'payments' | 'expenses' {
    switch (this.mode) {
      case 'invoice':
        return 'invoices';
      case 'payment':
        return 'payments';
      default:
        return 'expenses';
    }
  }

  closeModal(): void {
    this.mode = '';
    this.draft = {};
  }
}
