import { ComponentFixture, TestBed } from '@angular/core/testing';
import { beforeEach, describe, expect, it, vi } from 'vitest';
import { MarketingCampaignsApi } from './marketing-campaigns-api.service';
import { CrmApi } from '../services/crm-api.service';
import { MarketingCampaignsComponent } from './marketing-campaigns.component';

describe('MarketingCampaignsComponent', () => {
  let fixture: ComponentFixture<MarketingCampaignsComponent>;
  let component: MarketingCampaignsComponent;

  const api = {
    list: vi.fn(async () => []),
    create: vi.fn(async campaign => campaign),
    update: vi.fn(async campaign => campaign)
  };

  beforeEach(async () => {
    vi.clearAllMocks();
    api.list.mockResolvedValue([]);
    await TestBed.configureTestingModule({
      imports: [MarketingCampaignsComponent],
      providers: [
        { provide: MarketingCampaignsApi, useValue: api },
        { provide: CrmApi, useValue: { leads: vi.fn(async () => []) } }
      ]
    }).compileComponents();
    fixture = TestBed.createComponent(MarketingCampaignsComponent);
    component = fixture.componentInstance;
  });

  it('renders the campaign editor as a modal dialog when opened', () => {
    component.startCreate();
    fixture.detectChanges();

    const dialog = fixture.nativeElement.querySelector('.campaign-modal [role="dialog"]');
    expect(dialog).not.toBeNull();
    expect(dialog.getAttribute('aria-modal')).toBe('true');
    expect(dialog.querySelector('input[name="name"]')).not.toBeNull();
  });

  it('validates a required campaign name', async () => {
    component.draft.name = ' ';
    await component.save();
    expect(component.error).toContain('campaign name');
    expect(api.create).not.toHaveBeenCalled();
  });

  it('counts leads explicitly linked to a campaign', () => {
    component.leads = [
      { name: 'Lead A', source: 'CAMPAIGN', campaignId: 1, status: 'NEW', rating: 'WARM' },
      { name: 'Lead B', source: 'CAMPAIGN', campaignId: 1, status: 'NEW', rating: 'WARM' },
      { name: 'Lead C', source: 'CAMPAIGN', status: 'NEW', rating: 'WARM' }
    ];
    expect(component.leadCount(1)).toBe(2);
    expect(component.linkedLeadCount).toBe(2);
  });

  it('calculates the active campaign count and planned budget', () => {
    component.campaigns = [
      { id: 1, name: 'A', channel: 'EMAIL', budget: 100, status: 'ACTIVE' },
      { id: 2, name: 'B', channel: 'SOCIAL', budget: 250, status: 'PLANNED' },
      { id: 3, name: 'C', channel: 'EVENT', budget: 80, status: 'CANCELLED' }
    ];
    expect(component.activeCount).toBe(1);
    expect(component.plannedBudget).toBe(350);
  });
});
