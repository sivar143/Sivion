import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { firstValueFrom } from 'rxjs';
import { AuthService } from '../services/auth.service';

export interface UserNotification {
  id: number;
  title: string;
  message: string;
  createdAt: string;
  readAt: string | null;
}

@Injectable({ providedIn: 'root' })
export class NotificationApi {
  private readonly http = inject(HttpClient);
  private readonly auth = inject(AuthService);

  private async headers(): Promise<HttpHeaders> {
    const token = await this.auth.token();
    return new HttpHeaders({
      Authorization: `Bearer ${token}`,
      'X-Tenant-ID': '1'
    });
  }

  async inbox(): Promise<UserNotification[]> {
    return firstValueFrom(this.http.get<UserNotification[]>('/api/v1/notifications', {
      headers: await this.headers()
    }));
  }

  async markRead(id: number): Promise<UserNotification> {
    return firstValueFrom(this.http.patch<UserNotification>(
      `/api/v1/notifications/${id}/read`,
      {},
      { headers: await this.headers() }
    ));
  }

  async markAllRead(): Promise<{ updatedCount: number }> {
    return firstValueFrom(this.http.patch<{ updatedCount: number }>(
      '/api/v1/notifications/read-all',
      {},
      { headers: await this.headers() }
    ));
  }
}
