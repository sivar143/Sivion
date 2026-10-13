import { HttpClient, HttpHeaders, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { firstValueFrom } from 'rxjs';
import { AuthService } from '../services/auth.service';

export interface AuditRecord {
  id: number;
  actor: string;
  method: string;
  endpoint: string;
  statusCode: number;
  occurredAt: string;
  clientAddress?: string;
}

@Injectable({
  providedIn: 'root'
})
export class AuditApi {
  private readonly http = inject(HttpClient);
  private readonly auth = inject(AuthService);

  async records(limit = 100): Promise<AuditRecord[]> {
    const token = await this.auth.token();
    const headers = new HttpHeaders({
      Authorization: `Bearer ${token}`,
      'X-Tenant-ID': '1'
    });
    const params = new HttpParams().set('limit', Math.min(Math.max(limit, 1), 200));

    return firstValueFrom(this.http.get<AuditRecord[]>('/api/v1/audit', {
      headers,
      params
    }));
  }
}
