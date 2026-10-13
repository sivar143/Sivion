import { CommonModule } from '@angular/common';
import { ChangeDetectorRef, Component, OnInit, inject } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { MarketingCampaign, MarketingCampaignsApi } from './marketing-campaigns-api.service';

@Component({
  selector: 'app-marketing-campaigns',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './marketing-campaigns.component.html',
  styleUrls: ['./marketing-campaigns.component.scss']
})
export class MarketingCampaignsComponent implements OnInit {
  private readonly api = inject(MarketingCampaignsApi);
  private readonly cdr = inject(ChangeDetectorRef);

  campaigns: MarketingCampaign[] = [];
  loading = false;
  error = '';
  success = '';
  editingId: number | undefined;
  formOpen = false;
  draft: MarketingCampaign = this.emptyDraft();

  get activeCount(): number {
    return this.campaigns.filter(campaign => campaign.status === 'ACTIVE').length;
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
      this.campaigns = await this.api.list();
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
    } catch {
      this.error = 'Campaign could not be saved. Check the dates, channel and your access.';
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
