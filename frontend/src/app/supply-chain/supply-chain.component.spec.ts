import { ComponentFixture, TestBed } from '@angular/core/testing';
import { beforeEach, describe, expect, it, vi } from 'vitest';
import { SupplyChainComponent } from './supply-chain.component';
import { SupplyChainApi } from '../services/supply-chain-api.service';

describe('SupplyChainComponent', () => {
  let fixture: ComponentFixture<SupplyChainComponent>;
  const api = {
    shipments: vi.fn().mockResolvedValue([]),
    detail: vi.fn(),
    create: vi.fn(),
    updateStatus: vi.fn(),
    allocateProductionLot: vi.fn().mockResolvedValue({}),
    lotShipmentAllocations: vi.fn().mockResolvedValue([]),
    inspectLot: vi.fn().mockResolvedValue({}),
    refreshCarrierTracking: vi.fn().mockResolvedValue({ shipment: { id: 1 }, events: [] }),
    receiveInboundShipment: vi.fn().mockResolvedValue({}),
    inboundShipmentReceipts: vi.fn().mockResolvedValue([])
  };

  beforeEach(async () => {
    vi.clearAllMocks();
    api.shipments.mockResolvedValue([]);
    api.lotShipmentAllocations.mockResolvedValue([]);
    api.inboundShipmentReceipts.mockResolvedValue([]);
    await TestBed.configureTestingModule({
      imports: [SupplyChainComponent],
      providers: [{ provide: SupplyChainApi, useValue: api }]
    }).compileComponents();
    fixture = TestBed.createComponent(SupplyChainComponent);
    fixture.detectChanges();
    await fixture.whenStable();
  });

  it('creates the tracking workspace and loads shipments', () => {
    expect(fixture.componentInstance).toBeTruthy();
    expect(api.shipments).toHaveBeenCalled();
  });

  it('opens the new shipment form and switches back to the shipment workflow', () => {
    const component = fixture.componentInstance;
    component.selectSection('traceability');
    component.openCreateShipment();
    fixture.detectChanges();

    expect(component.activeSection).toBe('shipments');
    expect(component.showCreate).toBe(true);
    expect(fixture.nativeElement.querySelector('[role="dialog"] #create-shipment-title')?.textContent).toContain('Create shipment');
    expect(fixture.nativeElement.querySelector('.workflow-panel form.create-form')).toBeNull();
  });

  it('closes the create shipment dialog without affecting the tracking workspace', () => {
    const component = fixture.componentInstance;
    component.openCreateShipment();
    fixture.detectChanges();
    expect(fixture.nativeElement.querySelector('[role="dialog"]')).toBeTruthy();
    component.closeCreateShipment();
    fixture.detectChanges();
    expect(fixture.nativeElement.querySelector('[role="dialog"]')).toBeNull();
    expect(fixture.nativeElement.querySelector('.tracking-layout')).toBeTruthy();
  });

  it('keeps workflow forms separated instead of rendering every form together', () => {
    const component = fixture.componentInstance;
    component.selectSection('production');
    fixture.detectChanges();

    expect(fixture.nativeElement.querySelector('.workflow-panel .workflow-action strong')?.textContent).toContain('Manufacturing order');
    expect(fixture.nativeElement.querySelectorAll('.workflow-panel').length).toBe(1);
  });



  it('opens each workflow action in its own named dialog without embedding forms in the page', () => {
    const component = fixture.componentInstance;
    const cases = [
      ['inspection', 'Record inspection'],
      ['material-lot', 'Receive raw-material lot'],
      ['manufacturing-order', 'Manufacturing order'],
      ['production-lot', 'Record finished-product lot'],
      ['consumption', 'Link material consumption to production'],
      ['allocation', 'Allocate finished-product lot to shipment'],
      ['genealogy', 'Lot genealogy']
    ] as const;
    for (const [dialog, title] of cases) {
      component.openWorkflowDialog(dialog);
      fixture.detectChanges();
      expect(fixture.nativeElement.querySelector('[role="dialog"] h3')?.textContent).toContain(title);
      expect(fixture.nativeElement.querySelectorAll('.workflow-panel form').length).toBe(0);
      component.closeWorkflowDialog();
      fixture.detectChanges();
    }
  });

  it('records a partial inbound receipt against the selected purchase-order shipment', async () => {
    fixture.componentInstance.selected = {
      shipment: { id: 17, direction: 'INBOUND', purchaseOrderId: 22 },
      events: []
    } as any;
    fixture.componentInstance.inboundReceiptDraft = {
      receiptReference: 'SUP-DELIVERY-01', lotCode: 'RAW-LOT-01', productId: 31,
      warehouseId: 4, quantity: 5, supplierName: 'Supplier', supplierLotCode: 'S-LOT-1',
      finalReceipt: false
    };

    await fixture.componentInstance.receiveInboundShipment();

    expect(api.receiveInboundShipment).toHaveBeenCalledWith(17, expect.objectContaining({
      receiptReference: 'SUP-DELIVERY-01', lotCode: 'RAW-LOT-01', productId: 31,
      warehouseId: 4, quantity: 5, finalReceipt: false
    }));
    expect(api.inboundShipmentReceipts).toHaveBeenCalledWith(17);
    expect(fixture.componentInstance.success).toContain('quality inspection');
  });

  it('allocates a finished-product lot quantity and reloads its shipment allocation history', async () => {
    fixture.componentInstance.lotShipmentDraft = { lotCode: 'FG-LOT-10', shipmentId: 42, quantity: 3 };
    await fixture.componentInstance.allocateProductionLot();

    expect(api.allocateProductionLot).toHaveBeenCalledWith('FG-LOT-10', { shipmentId: 42, quantity: 3 });
    expect(api.lotShipmentAllocations).toHaveBeenCalledWith('FG-LOT-10');
    expect(fixture.componentInstance.success).toContain('allocated');
    expect(fixture.componentInstance.error).toBe('');
  });

  it('keeps the quality inspection form actionable when inventory reconciliation fails', async () => {
    api.inspectLot.mockRejectedValueOnce(new Error('Insufficient available stock'));
    fixture.componentInstance.inspectionDraft = {
      lotCode: 'RAW-LOT-7', acceptedQuantity: 2, rejectedQuantity: 1, notes: 'inspection'
    };
    await fixture.componentInstance.inspectLot();

    expect(fixture.componentInstance.error).toContain('Insufficient available stock');
    expect(fixture.componentInstance.saving).toBe(false);
  });

  it('shows an actionable message when shipment loading fails', async () => {
    api.shipments.mockRejectedValueOnce(new Error('Network unavailable'));
    await fixture.componentInstance.reload();
    expect(fixture.componentInstance.error).toContain('Network unavailable');
  });
});
