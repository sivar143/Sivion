import { ComponentFixture, TestBed } from '@angular/core/testing';
import { beforeEach, describe, expect, it } from 'vitest';
import { InventoryComponent } from './inventory.component';
import { InventoryApi } from '../services/inventory-api.service';
import { CrmApi } from '../services/crm-api.service';

describe('InventoryComponent', () => {
  let fixture: ComponentFixture<InventoryComponent>;
  let component: InventoryComponent;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [InventoryComponent],
      providers: [
        { provide: InventoryApi, useValue: {
          warehouses: async () => [],
          materials: async () => [],
          history: async () => [],
          transactions: async () => []
        } },
        { provide: CrmApi, useValue: { customers: async () => [] } }
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(InventoryComponent);
    component = fixture.componentInstance;
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('shows a meaningful placeholder instead of a blank warehouse selector', () => {
    fixture.detectChanges();

    const selector: HTMLSelectElement = fixture.nativeElement.querySelector('.warehouse');
    expect(selector).toBeTruthy();
    expect(selector.options[0].textContent.trim()).toBe('Loading warehouses…');
  });

  it('shows an empty-state message and disables stock-in when no warehouse exists', async () => {
    component.warehousesLoaded = true;
    fixture.detectChanges();

    const selector: HTMLSelectElement = fixture.nativeElement.querySelector('.warehouse');
    const stockInButton: HTMLButtonElement = fixture.nativeElement.querySelector('.stock-in-button');
    expect(selector.options[0].textContent.trim()).toBe('No warehouses available — add a warehouse');
    expect(stockInButton.disabled).toBe(true);
  });

  it('loads the warehouse selector without waiting for customer data', async () => {
    component.api = {
      warehouses: async () => [
        { id: 7, code: 'MAIN', name: 'Main Warehouse', status: 'ACTIVE' }
      ],
      materials: async () => [],
      history: async () => [],
      transactions: async () => []
    } as any;
    component.crm = {
      customers: () => new Promise(() => undefined)
    } as any;

    const initialization = component.ngOnInit();
    await new Promise(resolve => setTimeout(resolve, 0));

    expect(component.warehousesLoaded).toBe(true);
    expect(component.warehouseId).toBe(7);
    expect(component.warehouseName).toBe('MAIN — Main Warehouse');

    // The CRM promise intentionally remains pending; don't wait for it in this test.
    void initialization;
  });

  it('should calculate total and low stock counts', () => {
    component.materials = [
      { quantity: 10, reorderLevel: 5 } as any,
      { quantity: 2, reorderLevel: 5 } as any,
      { quantity: 0, reorderLevel: 0 } as any
    ];

    expect(component.totalStock).toBe(12);
    expect(component.lowStock).toBe(2);
  });

  it('should add and remove dispatch lines', () => {
    const initial = component.dispatchLines.length;
    component.addLine();
    expect(component.dispatchLines.length).toBe(initial + 1);

    component.removeLine(component.dispatchLines.length - 1);
    expect(component.dispatchLines.length).toBe(initial);
  });
});
