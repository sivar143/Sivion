import { ComponentFixture, TestBed } from '@angular/core/testing';
import { beforeEach, describe, expect, it, vi } from 'vitest';
import { AuditApi, AuditRecord } from './audit-api.service';
import { AuditComponent } from './audit.component';

describe('AuditComponent', () => {
  let fixture: ComponentFixture<AuditComponent>;
  let component: AuditComponent;

  const api = {
    records: vi.fn(async (): Promise<AuditRecord[]> => [])
  };

  beforeEach(async () => {
    api.records.mockReset();
    api.records.mockResolvedValue([]);

    await TestBed.configureTestingModule({
      imports: [AuditComponent],
      providers: [{ provide: AuditApi, useValue: api }]
    }).compileComponents();

    fixture = TestBed.createComponent(AuditComponent);
    component = fixture.componentInstance;
  });

  it('loads audit records on initialization', async () => {
    api.records.mockResolvedValue([{
      id: 1,
      actor: 'admin',
      method: 'POST',
      endpoint: '/api/v1/hr/leaves',
      statusCode: 201,
      occurredAt: '2026-10-13T08:00:00Z',
      clientAddress: '127.0.0.1'
    }]);

    await component.ngOnInit();

    expect(api.records).toHaveBeenCalledOnce();
    expect(component.records).toHaveLength(1);
    expect(component.records[0].actor).toBe('admin');
    expect(component.loading).toBe(false);
  });

  it('shows a safe error message when audit history cannot be loaded', async () => {
    api.records.mockRejectedValue(new Error('Forbidden'));
    await component.reload();

    expect(component.loadError).toContain('could not be loaded');
    expect(component.records).toEqual([]);
  });

  it('classifies HTTP result statuses for quick review', () => {
    expect(component.statusClass(201)).toBe('status-success');
    expect(component.statusClass(403)).toBe('status-warning');
    expect(component.statusClass(500)).toBe('status-error');
  });
});
