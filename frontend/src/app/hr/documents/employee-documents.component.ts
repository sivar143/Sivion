import { CommonModule } from '@angular/common';
import { ChangeDetectorRef, Component, OnInit, inject } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { AuthService } from '../../services/auth.service';
import { Employee, HrApi } from '../../services/hr-api.service';
import { EmployeeDocumentRecord, EmployeeDocumentsApi } from './employee-documents-api.service';

@Component({
  selector: 'app-employee-documents',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './employee-documents.component.html',
  styleUrls: ['../hr.component.scss', './employee-documents.component.scss']
})
export class EmployeeDocumentsComponent implements OnInit {
  private readonly api = inject(EmployeeDocumentsApi);
  private readonly hrApi = inject(HrApi);
  private readonly auth = inject(AuthService);
  private readonly cdr = inject(ChangeDetectorRef);

  records: EmployeeDocumentRecord[] = [];
  employees: Employee[] = [];
  selectedEmployeeId: number | undefined;
  description = '';
  selectedFile: File | undefined;
  loading = false;
  error = '';
  success = '';

  get canUpload(): boolean {
    return this.auth.roles.some(role => ['ADMIN', 'HR_ADMIN', 'HR_USER'].includes(role));
  }

  async ngOnInit(): Promise<void> {
    await Promise.all([this.reload(), this.loadEmployees()]);
  }

  async reload(): Promise<void> {
    this.loading = true;
    this.error = '';
    try {
      this.records = await this.api.list();
    } catch {
      this.error = 'Employee documents could not be loaded. Check your access and try again.';
    } finally {
      this.loading = false;
      this.cdr.markForCheck();
    }
  }

  private async loadEmployees(): Promise<void> {
    try {
      this.employees = await this.hrApi.employees();
      this.selectedEmployeeId = this.employees[0]?.id;
    } catch {
      this.error = 'Employee list could not be loaded.';
      this.cdr.markForCheck();
    }
  }

  onFileSelected(event: Event): void {
    const input = event.target as HTMLInputElement;
    this.selectedFile = input.files?.[0];
  }

  employeeName(id: number): string {
    const employee = this.employees.find(item => item.id === id);
    return employee ? `${employee.firstName} ${employee.lastName}` : `Employee #${id}`;
  }

  async upload(): Promise<void> {
    this.error = '';
    this.success = '';
    if (!this.selectedEmployeeId || !this.selectedFile) {
      this.error = 'Select an employee and a document file.';
      return;
    }
    if (this.selectedFile.size > 10 * 1024 * 1024) {
      this.error = 'Documents must be 10 MB or smaller.';
      return;
    }
    try {
      await this.api.upload(this.selectedEmployeeId, this.description, this.selectedFile);
      this.description = '';
      this.selectedFile = undefined;
      this.success = 'Document uploaded.';
      await this.reload();
    } catch {
      this.error = 'Document upload failed. Use PDF, JPG, PNG, DOC, DOCX or XLSX files up to 10 MB.';
    }
    this.cdr.markForCheck();
  }

  async download(record: EmployeeDocumentRecord): Promise<void> {
    try {
      const blob = await this.api.download(record.id);
      const url = URL.createObjectURL(blob);
      const anchor = document.createElement('a');
      anchor.href = url;
      anchor.download = record.originalName;
      anchor.click();
      URL.revokeObjectURL(url);
    } catch {
      this.error = 'Document could not be downloaded. You may not have access to this employee record.';
      this.cdr.markForCheck();
    }
  }
}
