import { ComponentFixture, TestBed } from '@angular/core/testing';
import { beforeEach, describe, expect, it, vi } from 'vitest';
import { MarketingCampaignsApi } from './marketing-campaigns-api.service';
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
      providers: [{ provide: MarketingCampaignsApi, useValue: api }]
    }).compileComponents();
    fixture = TestBed.createComponent(MarketingCampaignsComponent);
    component = fixture.componentInstance;
  });

  it('validates a required campaign name', async () => {
    component.draft.name = ' ';
    await component.save();
    expect(component.error).toContain('campaign name');
    expect(api.create).not.toHaveBeenCalled();
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
