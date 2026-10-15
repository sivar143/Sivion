import { ComponentFixture, TestBed } from '@angular/core/testing';
import { SupplyChainComponent } from './supply-chain.component';
import { SupplyChainApi } from '../services/supply-chain-api.service';

describe('SupplyChainComponent', () => {
  let fixture: ComponentFixture<SupplyChainComponent>;
  const api = {
    shipments: jasmine.createSpy('shipments').and.resolveTo([]),
    detail: jasmine.createSpy('detail'),
    create: jasmine.createSpy('create'),
    updateStatus: jasmine.createSpy('updateStatus')
  };

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [SupplyChainComponent],
      providers: [{ provide: SupplyChainApi, useValue: api }]
    }).compileComponents();
    fixture = TestBed.createComponent(SupplyChainComponent);
    await fixture.whenStable();
  });

  it('creates the tracking workspace and loads shipments', () => {
    expect(fixture.componentInstance).toBeTruthy();
    expect(api.shipments).toHaveBeenCalled();
  });

  it('shows an actionable message when shipment loading fails', async () => {
    api.shipments.and.rejectWith(new Error('Network unavailable'));
    await fixture.componentInstance.reload();
    expect(fixture.componentInstance.error).toContain('Network unavailable');
  });
});
