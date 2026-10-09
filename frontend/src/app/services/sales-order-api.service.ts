import { inject, Injectable } from '@angular/core';
import { AuthService } from './auth.service';

export interface SalesOrderItem {
  materialId: number;
  quantity: number;
  unitPrice: number;
  description?: string;
  lineTotal?: number;
}

export interface SalesOrder {
  id?: number;
  orderNumber: string;
  customerId: number;
  opportunityId?: number;
  status: string;
  totalAmount: number;
  orderDate: string;
  deliveryAddress?: string;
  notes?: string;
  items: SalesOrderItem[];
}

@Injectable({ providedIn: 'root' })
export class SalesOrderApi {
  private readonly auth = inject(AuthService);
  private readonly base = '/api/v1/sales/orders';

  private async headers(): Promise<HeadersInit> {
    return {
      'Content-Type': 'application/json',
      Authorization: `Bearer ${await this.auth.token()}`,
      'X-Tenant-ID': '1'
    };
  }

  async list(): Promise<SalesOrder[]> {
    const response = await fetch(this.base, { headers: await this.headers() });
    if (!response.ok) throw new Error(await response.text());
    return response.json();
  }

  async create(request: {
    customerId: number;
    opportunityId?: number;
    deliveryAddress?: string;
    notes?: string;
    items: SalesOrderItem[];
  }): Promise<SalesOrder> {
    const response = await fetch(this.base, {
      method: 'POST',
      headers: await this.headers(),
      body: JSON.stringify(request)
    });
    if (!response.ok) throw new Error(await response.text());
    return response.json();
  }

  async updateStatus(id: number, status: string): Promise<SalesOrder> {
    const response = await fetch(`${this.base}/${id}/status`, {
      method: 'PATCH',
      headers: await this.headers(),
      body: JSON.stringify({ status })
    });
    if (!response.ok) throw new Error(await response.text());
    return response.json();
  }
}
