import { HttpClient, HttpHeaders, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { firstValueFrom } from 'rxjs';
import { AuthService } from '../../services/auth.service';

export type HrCalendarType = 'HOLIDAY' | 'MEETING';

export interface HrCalendarEntry {
  id?: number;
  type: HrCalendarType;
  title: string;
  description?: string;
  startsOn: string;
  endsOn: string;
  status: 'ACTIVE' | 'CANCELLED';
  createdBy?: string;
  createdAt?: string;
}

@Injectable({ providedIn: 'root' })
export class HrCalendarApi {
  private readonly http = inject(HttpClient);
  private readonly auth = inject(AuthService);
  private readonly url = '/api/v1/hr/calendar';

  private async options(type: HrCalendarType) {
    return {
      headers: new HttpHeaders({
        Authorization: `Bearer ${await this.auth.token()}`,
        'X-Tenant-ID': '1'
      }),
      params: new HttpParams().set('type', type)
    };
  }

  async list(type: HrCalendarType): Promise<HrCalendarEntry[]> {
    return firstValueFrom(this.http.get<HrCalendarEntry[]>(this.url, await this.options(type)));
  }

  async create(entry: HrCalendarEntry): Promise<HrCalendarEntry> {
    return firstValueFrom(this.http.post<HrCalendarEntry>(
      this.url,
      entry,
      await this.options(entry.type)
    ));
  }

  async update(entry: HrCalendarEntry): Promise<HrCalendarEntry> {
    if (!entry.id) throw new Error('Calendar entry id is required');
    return firstValueFrom(this.http.put<HrCalendarEntry>(
      `${this.url}/${entry.id}`,
      entry,
      await this.options(entry.type)
    ));
  }
}
