import { CommonModule } from '@angular/common';
import { ChangeDetectorRef, Component, OnInit, inject } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { getDialogErrorMessage } from '../shared/dialog-error.util';
import { MarketingCampaign, MarketingCampaignsApi } from './marketing-campaigns-api.service';
import { CrmApi, Lead } from '../services/crm-api.service';

@Component({
  selector: 'app-marketing-campaigns',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './marketing-campaigns.component.html',
  styleUrls: ['./marketing-campaigns.component.scss']
})
export class MarketingCampaignsComponent implements OnInit {
  private readonly api = inject(MarketingCampaignsApi);
  private readonly crm = inject(CrmApi);
  private readonly cdr = inject(ChangeDetectorRef);

  campaigns: MarketingCampaign[] = [];
  leads: Lead[] = [];
  loading = false;
  saving = false;
  error = '';
  success = '';
  editingId: number | undefined;
  formOpen = false;
  draft: MarketingCampaign = this.emptyDraft();

  get activeCount(): number {
    return this.campaigns.filter(campaign => campaign.status === 'ACTIVE').length;
  }

  leadCount(campaignId: number | undefined): number {
    return campaignId ? this.leads.filter(lead => lead.campaignId === campaignId).length : 0;
  }

  get linkedLeadCount(): number {
    return this.leads.filter(lead => lead.campaignId !== undefined && lead.campaignId !== null).length;
  }

  get plannedBudget(): number {
    return this.campaigns
      .filter(campaign => ['PLANNED', 'ACTIVE'].includes(campaign.status))
      .reduce((total, campaign) => total + Number(campaign.budget || 0), 0);
  }

  async ngOnInit(): Promise<void> {
    await this.reload();
  }

  async reload(): Promise<void> {
    this.loading = true;
    this.error = '';
    try {
      const [campaignResult, leadResult] = await Promise.allSettled([
        this.api.list(),
        this.crm.leads()
      ]);
      if (campaignResult.status === 'rejected') {
        throw campaignResult.reason;
      }
      this.campaigns = campaignResult.value;
      this.leads = leadResult.status === 'fulfilled' ? leadResult.value : [];
    } catch {
      this.error = 'Campaigns could not be loaded. Check your access and try again.';
    } finally {
      this.loading = false;
      this.cdr.markForCheck();
    }
  }

  startCreate(): void {
    this.formOpen = true;
    this.editingId = undefined;
    this.draft = this.emptyDraft();
    this.success = '';
    this.error = '';
  }

  edit(campaign: MarketingCampaign): void {
    this.formOpen = true;
    this.editingId = campaign.id;
    this.draft = { ...campaign };
    this.success = '';
    this.error = '';
  }

  cancelEdit(): void {
    this.formOpen = false;
    this.editingId = undefined;
    this.draft = this.emptyDraft();
  }

  async save(): Promise<void> {
    if (this.saving) return;
    this.error = '';
    this.success = '';
    if (!this.draft.name.trim()) {
      this.error = 'Enter a campaign name.';
      return;
    }
    if (this.draft.budget < 0) {
      this.error = 'Budget cannot be negative.';
      return;
    }
    this.saving = true;
    try {
      if (this.editingId) {
        await this.api.update({ ...this.draft, id: this.editingId });
        this.success = 'Campaign updated.';
      } else {
        await this.api.create(this.draft);
        this.success = 'Campaign created.';
      }
      this.cancelEdit();
      await this.reload();
    } catch (error: unknown) {
      this.error = getDialogErrorMessage(error, 'Campaign could not be saved. Check the dates, channel and your access.');
    } finally {
      this.saving = false;
    }
  }

  private emptyDraft(): MarketingCampaign {
    return {
      name: '',
      channel: 'SOCIAL',
      budget: 0,
      status: 'DRAFT',
      goal: ''
    };
  }
}
