import { CommonModule } from '@angular/common';
import { ChangeDetectorRef, Component, Input, OnInit, inject } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { getDialogErrorMessage } from '../../shared/dialog-error.util';
import { HrApi, Employee } from '../../services/hr-api.service';
import { AuthService } from '../../services/auth.service';
import { HrWorkspaceRecord, HrWorkspaceRecordType, HrWorkspaceRecordsApi } from './hr-workspace-records-api.service';

@Component({
  selector: 'app-hr-workspace-records',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './hr-workspace-records.component.html',
  styleUrls: ['../hr.component.scss', './hr-workspace-records.component.scss']
})
export class HrWorkspaceRecordsComponent implements OnInit {
  @Input() type: HrWorkspaceRecordType = 'PERFORMANCE';

  private readonly api = inject(HrWorkspaceRecordsApi);
  private readonly hrApi = inject(HrApi);
  private readonly auth = inject(AuthService);
  private readonly cdr = inject(ChangeDetectorRef);

  records: HrWorkspaceRecord[] = [];
  employees: Employee[] = [];
  loading = false;
  error = '';
  saving = false;
  formOpen = false;
  editingId: number | undefined;
  draft: HrWorkspaceRecord = this.emptyDraft();

  get heading(): string {
    return this.type === 'PERFORMANCE' ? 'Performance reviews' : 'Learning and development';
  }

  get description(): string {
    return this.type === 'PERFORMANCE'
      ? 'Record employee review outcomes and ratings.'
      : 'Share learning resources and track development activities.';
  }

  get canManage(): boolean {
    return this.auth.roles.some(role => ['ADMIN', 'HR_ADMIN', 'HR_USER', 'MANAGER'].includes(role));
  }

  async ngOnInit(): Promise<void> {
    await this.reload();
    if (this.type === 'PERFORMANCE') {
      try {
        this.employees = await this.hrApi.employees();
      } catch {
        // Keep existing reviews visible even if the employee picker cannot be loaded.
      }
    }
  }

  async reload(): Promise<void> {
    this.loading = true;
    this.error = '';
    try {
      this.records = await this.api.list(this.type);
    } catch {
      this.error = `${this.heading} could not be loaded. Check your access and try again.`;
    } finally {
      this.loading = false;
      this.cdr.markForCheck();
    }
  }

  startCreate(): void {
    this.editingId = undefined;
    this.draft = this.emptyDraft();
    this.formOpen = true;
    this.error = '';
  }

  edit(record: HrWorkspaceRecord): void {
    this.editingId = record.id;
    this.draft = { ...record };
    this.formOpen = true;
    this.error = '';
  }

  cancel(): void {
    this.formOpen = false;
    this.editingId = undefined;
    this.draft = this.emptyDraft();
  }

  employeeName(id: number | undefined): string {
    const employee = this.employees.find(item => item.id === id);
    return employee ? `${employee.firstName} ${employee.lastName}` : `Employee #${id ?? '—'}`;
  }

  async save(): Promise<void> {
    if (this.saving) return;
    this.error = '';
    if (!this.draft.title.trim()) {
      this.error = 'Enter a title.';
      return;
    }
    if (this.type === 'PERFORMANCE' && (!this.draft.employeeId || !this.draft.score || this.draft.score < 1 || this.draft.score > 5)) {
      this.error = 'Select an employee and a score from 1 to 5.';
      return;
    }
    if (this.draft.resourceUrl && !(this.draft.resourceUrl.startsWith('https://') || this.draft.resourceUrl.startsWith('/'))) {
      this.error = 'Resource links must use HTTPS or an application-relative path.';
      return;
    }
    this.saving = true;
    try {
      const record = { ...this.draft, type: this.type };
      if (this.editingId) await this.api.update({ ...record, id: this.editingId });
      else await this.api.create(record);
      this.cancel();
      await this.reload();
    } catch (error: unknown) {
      this.error = getDialogErrorMessage(error, `${this.heading} record could not be saved. Please review the details and try again.`);
      this.cdr.markForCheck();
    } finally {
      this.saving = false;
    }
  }

  private emptyDraft(): HrWorkspaceRecord {
    return {
      type: this.type,
      title: '',
      description: '',
      score: this.type === 'PERFORMANCE' ? 3 : undefined,
      status: this.type === 'PERFORMANCE' ? 'DRAFT' : 'ACTIVE'
    };
  }
}
