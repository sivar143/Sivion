import { HttpClient, HttpHeaders, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { firstValueFrom } from 'rxjs';
import { AuthService } from '../../services/auth.service';

export type HrWorkspaceRecordType = 'PERFORMANCE' | 'LEARNING';

export interface HrWorkspaceRecord {
  id?: number;
  type: HrWorkspaceRecordType;
  employeeId?: number;
  title: string;
  description?: string;
  score?: number;
  resourceUrl?: string;
  dueDate?: string;
  status: string;
  createdBy?: string;
  createdAt?: string;
}

@Injectable({ providedIn: 'root' })
export class HrWorkspaceRecordsApi {
  private readonly http = inject(HttpClient);
  private readonly auth = inject(AuthService);
  private readonly url = '/api/v1/hr/workspace-records';

  private async options(type: HrWorkspaceRecordType) {
    return {
      headers: new HttpHeaders({
        Authorization: `Bearer ${await this.auth.token()}`,
        'X-Tenant-ID': '1'
      }),
      params: new HttpParams().set('type', type)
    };
  }

  async list(type: HrWorkspaceRecordType): Promise<HrWorkspaceRecord[]> {
    return firstValueFrom(this.http.get<HrWorkspaceRecord[]>(this.url, await this.options(type)));
  }

  async create(record: HrWorkspaceRecord): Promise<HrWorkspaceRecord> {
    return firstValueFrom(this.http.post<HrWorkspaceRecord>(this.url, record, await this.options(record.type)));
  }

  async update(record: HrWorkspaceRecord): Promise<HrWorkspaceRecord> {
    if (!record.id) throw new Error('Record id is required');
    return firstValueFrom(this.http.put<HrWorkspaceRecord>(
      `${this.url}/${record.id}`,
      record,
      await this.options(record.type)
    ));
  }
}
