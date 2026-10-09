import { CommonModule } from '@angular/common';
import { Component, OnInit, inject } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { AuthService } from '../../services/auth.service';
import { Department, Designation, Employee, HrApi } from '../../services/hr-api.service';

@Component({
  selector: 'app-hr-employees',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './employees.component.html',
  styleUrls: ['../hr.component.scss']
})
export class EmployeesComponent implements OnInit {
  private readonly api = inject(HrApi);
  private readonly auth = inject(AuthService);
  employees: Employee[] = [];
  departments: Department[] = [];
  designations: Designation[] = [];
  loadError = '';
  formError = '';
  modalOpen = false;
  editing = false;
  draft: Employee = this.emptyEmployee();

  get availableDesignations(): Designation[] {
    return this.designations.filter(item =>
      item.status !== 'INACTIVE' &&
      (item.departmentId == null || item.departmentId === this.draft.departmentId));
  }

  onDepartmentChange(departmentId: number | undefined): void {
    this.draft.departmentId = departmentId;
    const selected = this.designations.find(item => item.id === this.draft.designationId);
    if (selected && (selected.status === 'INACTIVE' ||
        (selected.departmentId != null && selected.departmentId !== departmentId))) {
      this.draft.designationId = undefined;
      this.draft.designation = undefined;
    }
  }

  get availableRoles(): string[] {
    return this.auth.hasRole('ADMIN')
      ? ['ADMIN', 'HR_ADMIN', 'HR_USER', 'MANAGER', 'EMPLOYEE', 'SALES_MANAGER', 'SALES_USER',
         'INVENTORY_MANAGER', 'INVENTORY_USER', 'WAREHOUSE_MANAGER', 'WAREHOUSE_USER',
         'PROCUREMENT_MANAGER', 'FINANCE_MANAGER', 'FINANCE_USER', 'MARKETING_USER']
      : ['HR_USER', 'MANAGER', 'EMPLOYEE'];
  }

  canManageEmployee(employee: Employee): boolean {
    return this.auth.hasRole('ADMIN')
      || ['HR_USER', 'MANAGER', 'EMPLOYEE'].includes(employee.role || 'EMPLOYEE');
  }

  async ngOnInit(): Promise<void> { await this.reload(); }

  async reload(): Promise<void> {
    this.loadError = '';
    const requests = [
      this.api.employees().then(value => { this.employees = value; })
        .catch(() => this.addLoadError('Employee records could not be loaded.')),
      this.api.departments().then(value => { this.departments = value; })
        .catch(() => this.addLoadError('Departments could not be loaded.')),
      this.api.designations().then(value => { this.designations = value; })
        .catch(() => this.addLoadError('Designations could not be loaded.'))
    ];
    await Promise.all(requests);
  }

  departmentName(id?: number): string {
    if (!id) return '—';
    return this.departments.find(item => item.id === id)?.name ?? `Department #${id}`;
  }

  openNew(): void {
    this.draft = this.emptyEmployee();
    this.editing = false;
    this.formError = '';
    this.modalOpen = true;
  }

  openEdit(employee: Employee): void {
    this.draft = { ...employee, password: '', temporaryPassword: false };
    this.editing = true;
    this.formError = '';
    this.modalOpen = true;
  }

  closeModal(): void { this.modalOpen = false; this.formError = ''; }

  async save(): Promise<void> {
    this.formError = '';
    try {
      if (this.editing && this.draft.id) await this.api.updateEmployee(this.draft.id, this.draft);
      else await this.api.createEmployee(this.draft);
      this.closeModal();
      await this.reload();
    } catch (error: any) {
      this.formError = error?.error?.error ?? error?.message ?? 'Unable to save staff account.';
    }
  }

  private addLoadError(message: string): void {
    this.loadError = this.loadError ? `${this.loadError} ${message}` : message;
  }

  private emptyEmployee(): Employee {
    return {
      employeeNumber: '', firstName: '', lastName: '', email: '', status: 'ACTIVE',
      role: 'EMPLOYEE', accountEnabled: true, temporaryPassword: false, password: ''
    };
  }
}