import { ComponentFixture, TestBed } from '@angular/core/testing';
import { HrComponent } from './hr.component';
import { HrApi } from './hr-api.service';

describe('HrComponent', () => {
  let fixture: ComponentFixture<HrComponent>;
  let component: HrComponent;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [HrComponent],
      providers: [{ provide: HrApi, useValue: {} }]
    }).compileComponents();
    fixture = TestBed.createComponent(HrComponent);
    component = fixture.componentInstance;
  });

  it('should create', () => expect(component).toBeTruthy());
  it('should expose both organization creation actions', () => {
    component.section = 'organization';
    fixture.detectChanges();
    const buttons = Array.from(fixture.nativeElement.querySelectorAll('button')) as HTMLButtonElement[];
    expect(buttons.some(b => b.textContent?.includes('+ Department'))).toBeTrue();
    expect(buttons.some(b => b.textContent?.includes('+ Designation'))).toBeTrue();
  });
  it('should switch organization views independently', () => {
    component.organizationView = 'departments';
    expect(component.organizationView).toBe('departments');
    component.organizationView = 'designations';
    expect(component.organizationView).toBe('designations');
  });
});
