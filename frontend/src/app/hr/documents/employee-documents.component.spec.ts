import { ComponentFixture, TestBed } from '@angular/core/testing';
import { beforeEach, describe, expect, it, vi } from 'vitest';
import { AuthService } from '../../services/auth.service';
import { HrApi } from '../../services/hr-api.service';
import { EmployeeDocumentsApi } from './employee-documents-api.service';
import { EmployeeDocumentsComponent } from './employee-documents.component';

describe('EmployeeDocumentsComponent', () => {
  let fixture: ComponentFixture<EmployeeDocumentsComponent>;
  let component: EmployeeDocumentsComponent;

  const api = {
    list: vi.fn(async () => []),
    upload: vi.fn(async () => ({})),
    download: vi.fn(async () => new Blob())
  };

  beforeEach(async () => {
    vi.clearAllMocks();
    api.list.mockResolvedValue([]);
    await TestBed.configureTestingModule({
      imports: [EmployeeDocumentsComponent],
      providers: [
        { provide: EmployeeDocumentsApi, useValue: api },
        { provide: HrApi, useValue: { employees: async () => [{ id: 5, employeeNumber: 'EMP-5', firstName: 'Alex', lastName: 'Kim' }] } },
        { provide: AuthService, useValue: { roles: ['HR_USER'] } }
      ]
    }).compileComponents();
    fixture = TestBed.createComponent(EmployeeDocumentsComponent);
    component = fixture.componentInstance;
  });

  it('allows document uploads only for HR and admin roles', () => {
    expect(component.canUpload).toBe(true);
  });

  it('requires an employee and file before uploading', async () => {
    await component.upload();
    expect(component.error).toContain('Select an employee');
    expect(api.upload).not.toHaveBeenCalled();
  });

  it('rejects files above the client-side size limit', async () => {
    component.selectedEmployeeId = 5;
    component.selectedFile = new File([new Uint8Array(10 * 1024 * 1024 + 1)], 'large.pdf', { type: 'application/pdf' });
    await component.upload();
    expect(component.error).toContain('10 MB or smaller');
    expect(api.upload).not.toHaveBeenCalled();
  });
});
