import { ComponentFixture, TestBed } from '@angular/core/testing';
import { SalesOrderComponent } from './sales-order.component';
import { SalesOrderApi } from './sales-order-api.service';
import { CrmApi } from './crm-api.service';
import { InventoryApi } from './inventory-api.service';

describe('SalesOrderComponent', () => {
  let fixture: ComponentFixture<SalesOrderComponent>;
  let component: SalesOrderComponent;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [SalesOrderComponent],
      providers: [
        { provide: SalesOrderApi, useValue: {} },
        { provide: CrmApi, useValue: {} },
        { provide: InventoryApi, useValue: {} }
      ]
    }).compileComponents();
    fixture = TestBed.createComponent(SalesOrderComponent);
    component = fixture.componentInstance;
  });

  it('should create', () => expect(component).toBeTruthy());
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
