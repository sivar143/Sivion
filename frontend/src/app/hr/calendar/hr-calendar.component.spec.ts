import { ComponentFixture, TestBed } from '@angular/core/testing';
import { beforeEach, describe, expect, it, vi } from 'vitest';
import { HrCalendarApi, HrCalendarEntry } from './hr-calendar-api.service';
import { HrCalendarComponent } from './hr-calendar.component';

describe('HrCalendarComponent', () => {
  let fixture: ComponentFixture<HrCalendarComponent>;
  let component: HrCalendarComponent;

  const api = {
    list: vi.fn(async () => []),
    create: vi.fn(async (entry: HrCalendarEntry) => entry),
    update: vi.fn(async (entry: HrCalendarEntry) => entry)
  };

  beforeEach(async () => {
    vi.clearAllMocks();
    api.list.mockResolvedValue([]);
    await TestBed.configureTestingModule({
      imports: [HrCalendarComponent],
      providers: [{ provide: HrCalendarApi, useValue: api }]
    }).compileComponents();
    fixture = TestBed.createComponent(HrCalendarComponent);
    component = fixture.componentInstance;
  });

  it('selects the right API type for holiday and meeting screens', () => {
    component.section = 'holidays';
    expect(component.type).toBe('HOLIDAY');
    component.section = 'meetings';
    expect(component.type).toBe('MEETING');
  });

  it('rejects a reversed date range before calling the API', async () => {
    component.draft.title = 'Planning';
    component.draft.startsOn = '2026-11-12';
    component.draft.endsOn = '2026-11-10';
    await component.save();
    expect(component.error).toContain('valid date range');
    expect(api.create).not.toHaveBeenCalled();
  });

  it('loads the selected calendar type', async () => {
    component.section = 'meetings';
    await component.reload();
    expect(api.list).toHaveBeenCalledWith('MEETING');
  });
});
