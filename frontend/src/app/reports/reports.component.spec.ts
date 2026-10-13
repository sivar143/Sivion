import { ComponentFixture, TestBed } from '@angular/core/testing';
import { beforeEach, describe, expect, it, vi } from 'vitest';
import { HttpClient } from '@angular/common/http';
import { AuthService } from '../services/auth.service';
import { CrmApi } from '../services/crm-api.service';
import { InventoryApi } from '../services/inventory-api.service';
import { ReportsComponent } from './reports.component';

describe('ReportsComponent', () => {
  let fixture: ComponentFixture<ReportsComponent>;
  let component: ReportsComponent;

  const crm = {
    leads: vi.fn(async () => [
      { id: 1, status: 'NEW', source: 'CAMPAIGN' },
      { id: 2, status: 'CONVERTED', source: 'WEBSITE' }
    ]),
    opportunities: vi.fn(async () => [
      { id: 1, stage: 'QUALIFICATION', amount: 1000, probability: 50 },
      { id: 2, stage: 'CLOSED_WON', amount: 500, probability: 100 }
    ])
  };

  beforeEach(async () => {
    vi.clearAllMocks();
    await TestBed.configureTestingModule({
      imports: [ReportsComponent],
      providers: [
        { provide: CrmApi, useValue: crm },
        { provide: InventoryApi, useValue: {} },
        { provide: AuthService, useValue: { token: async () => 'token' } },
        { provide: HttpClient, useValue: { get: vi.fn() } }
      ]
    }).compileComponents();
    fixture = TestBed.createComponent(ReportsComponent);
    component = fixture.componentInstance;
  });

  it('calculates sales pipeline metrics from the CRM API', async () => {
    component.workspaceId = 'sales-manager';
    await component.reload();

    expect(component.metrics.find(metric => metric.label === 'Total leads')?.value).toBe(2);
    expect(component.metrics.find(metric => metric.label === 'Open opportunities')?.value).toBe(1);
    expect(component.metrics.find(metric => metric.label === 'Weighted pipeline')?.value).toContain('500');
  });

  it('shows partial data when one CRM report source fails', async () => {
    crm.opportunities.mockRejectedValueOnce(new Error('Unavailable'));
    component.workspaceId = 'sales-manager';
    await component.reload();

    expect(component.metrics.find(metric => metric.label === 'Total leads')?.value).toBe(2);
    expect(component.loadError).toContain('opportunities');
  });
}
