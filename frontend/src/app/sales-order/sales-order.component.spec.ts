import { ComponentFixture, TestBed } from '@angular/core/testing';
import { beforeEach, describe, expect, it, vi } from 'vitest';
import { SalesOrderComponent } from './sales-order.component';
import { SalesOrderApi } from '../services/sales-order-api.service';
import { CrmApi } from '../services/crm-api.service';
import { InventoryApi } from '../services/inventory-api.service';

describe('SalesOrderComponent', () => {
  let fixture: ComponentFixture<SalesOrderComponent>;
  let component: SalesOrderComponent;

  const salesApi = {
    list: vi.fn(async () => []),
    create: vi.fn(async () => ({})),
    updateStatus: vi.fn(async () => ({}))
  };
  const crmApi = {
    customers: vi.fn(async () => []),
    opportunities: vi.fn(async () => [])
  };
  const inventoryApi = { materials: vi.fn(async () => []) };

  beforeEach(async () => {
    vi.clearAllMocks();
    salesApi.list.mockResolvedValue([]);
    salesApi.create.mockResolvedValue({});
    await TestBed.configureTestingModule({
      imports: [SalesOrderComponent],
      providers: [
        { provide: SalesOrderApi, useValue: salesApi },
        { provide: CrmApi, useValue: crmApi },
        { provide: InventoryApi, useValue: inventoryApi }
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(SalesOrderComponent);
    component = fixture.componentInstance;
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('keeps the dialog open and displays the server error when creation fails', async () => {
    component.newOrder = true;
    salesApi.create.mockRejectedValue({ error: { message: 'A duplicate order was detected.' } });

    await component.save();

    expect(component.newOrder).toBe(true);
    expect(component.dialogError).toBe('A duplicate order was detected.');
    expect(component.submitting).toBe(false);
  });

  it('closes the dialog only after a successful create action', async () => {
    component.newOrder = true;

    await component.save();

    expect(component.newOrder).toBe(false);
    expect(component.dialogError).toBe('');
    expect(component.submitting).toBe(false);
  });

  it('should calculate order totals and status counts', () => {
    component.orders = [
      { status: 'DRAFT', totalAmount: 100 } as any,
      { status: 'CONFIRMED', totalAmount: 250 } as any,
      { status: 'DRAFT', totalAmount: 50 } as any
    ];

    expect(component.total).toBe(400);
    expect(component.count('DRAFT')).toBe(2);
    expect(component.count('CONFIRMED')).toBe(1);
  });

  it('should add and remove order lines', () => {
    const initial = component.draft.items.length;
    component.addLine();
    expect(component.draft.items.length).toBe(initial + 1);

    component.removeLine(component.draft.items.length - 1);
    expect(component.draft.items.length).toBe(initial);
  });
});
