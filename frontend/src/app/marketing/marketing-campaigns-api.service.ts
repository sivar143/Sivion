import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { firstValueFrom } from 'rxjs';
import { AuthService } from '../services/auth.service';

export interface MarketingCampaign {
  id?: number;
  name: string;
  channel: string;
  budget: number;
  startDate?: string;
  endDate?: string;
  status: string;
  goal?: string;
  createdAt?: string;
}

@Injectable({ providedIn: 'root' })
export class MarketingCampaignsApi {
  private readonly http = inject(HttpClient);
  private readonly auth = inject(AuthService);
  private readonly baseUrl = '/api/v1/marketing/campaigns';

  private async options() {
    return {
      headers: new HttpHeaders({
        Authorization: `Bearer ${await this.auth.token()}`,
        'X-Tenant-ID': '1'
      })
    };
  }

  async list(): Promise<MarketingCampaign[]> {
    return firstValueFrom(this.http.get<MarketingCampaign[]>(this.baseUrl, await this.options()));
  }

  async create(campaign: MarketingCampaign): Promise<MarketingCampaign> {
    return firstValueFrom(this.http.post<MarketingCampaign>(this.baseUrl, campaign, await this.options()));
  }

  async update(campaign: MarketingCampaign): Promise<MarketingCampaign> {
    if (!campaign.id) throw new Error('Campaign id is required for update');
    return firstValueFrom(this.http.put<MarketingCampaign>(
      `${this.baseUrl}/${campaign.id}`,
      campaign,
      await this.options()
    ));
  }
}
