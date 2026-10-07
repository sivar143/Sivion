import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ProcurementComponent } from './procurement.component';
import { HttpClient } from '@angular/common/http';
import { AuthService } from './auth.service';

describe('ProcurementComponent', () => {
  let fixture: ComponentFixture<ProcurementComponent>;
  let component: ProcurementComponent;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ProcurementComponent],
      providers: [
        { provide: HttpClient, useValue: {} },
        { provide: AuthService, useValue: { token: async () => 'test-token' } }
      ]
    }).compileComponents();
    fixture = TestBed.createComponent(ProcurementComponent);
    component = fixture.componentInstance;
  });

  it('should create', () => expect(component).toBeTruthy());
  it('should expose only receivable purchase orders', () => {
    component.orders = [
      { status: 'ISSUED' }, { status: 'PARTIALLY_RECEIVED' },
      { status: 'RECEIVED' }, { status: 'CANCELLED' }
    ];
    expect(component.receivableOrders.length).toBe(2);
  });
  it('should add and remove purchase order lines', () => {
    const initial = component.order.items.length;
    component.addOrderItem();
    expect(component.order.items.length).toBe(initial + 1);
    component.removeOrderItem(component.order.items.length - 1);
    expect(component.order.items.length).toBe(initial);
  });
});
