import { CommonModule } from '@angular/common';
import { Component, OnInit, inject } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { AuthService } from '../../services/auth.service';
import { Department, Designation, Employee, HrApi } from '../../services/hr-api.service';

@Component({
  selector: 'app-hr-organization',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './organization.component.html',
  styleUrls: ['../hr.component.scss']
})
export class OrganizationComponent implements OnInit {
  private readonly api = inject(HrApi);
  private readonly auth = inject(AuthService);
  departments: Department[] = [];
  designations: Designation[] = [];
  employees: Employee[] = [];
  loadError = '';
  formError = '';
  departmentModal = false;
  designationModal = false;
  editingDepartment = false;
  editingDesignation = false;
  departmentDraft: Department = this.emptyDepartment();
  designationDraft: Designation = this.emptyDesignation();

  get canModifyHr(): boolean {
    return ['ADMIN', 'HR_ADMIN', 'HR_USER'].some(role => this.auth.hasRole(role));
  }

  get availableParents(): Department[] {
    return this.departments.filter(item => item.id !== this.departmentDraft.id);
  }

  async ngOnInit(): Promise<void> { await this.reload(); }

  async reload(): Promise<void> {
    this.loadError = '';
    const requests = [
      this.api.departments().then(value => { this.departments = value; })
        .catch(() => this.addLoadError('Departments could not be loaded.')),
      this.api.designations().then(value => { this.designations = value; })
        .catch(() => this.addLoadError('Designations could not be loaded.')),
      this.api.employees().then(value => { this.employees = value; })
        .catch(() => this.addLoadError('Employee counts could not be loaded.'))
    ];
    await Promise.all(requests);
  }

  private addLoadError(message: string): void {
    this.loadError = this.loadError ? `${this.loadError} ${message}` : message;
  }

  departmentName(id?: number): string {
    if (!id) return '—';
    return this.departments.find(item => item.id === id)?.name ?? `Department #${id}`;
  }

  employeesInDepartment(id?: number): number {
    return this.employees.filter(item => item.departmentId === id).length;
  }

  openNewDepartment(): void {
    this.departmentDraft = this.emptyDepartment();
    this.editingDepartment = false;
    this.formError = '';
    this.departmentModal = true;
  }
  openEditDepartment(item: Department): void {
    this.departmentDraft = { ...item };
    this.editingDepartment = true;
    this.formError = '';
    this.departmentModal = true;
  }
  closeDepartmentModal(): void { this.departmentModal = false; this.formError = ''; }

  async saveDepartment(): Promise<void> {
    this.formError = '';
    try {
      if (this.editingDepartment) await this.api.updateDepartment(this.departmentDraft);
      else await this.api.createDepartment(this.departmentDraft);
      this.closeDepartmentModal();
      await this.reload();
    } catch (error: any) {
      this.formError = error?.error?.error ?? error?.message ?? 'Unable to save department.';
    }
  }

  openNewDesignation(): void {
    this.designationDraft = this.emptyDesignation();
    this.editingDesignation = false;
    this.formError = '';
    this.designationModal = true;
  }
  openEditDesignation(item: Designation): void {
    this.designationDraft = { ...item };
    this.editingDesignation = true;
    this.formError = '';
    this.designationModal = true;
  }
  closeDesignationModal(): void { this.designationModal = false; this.formError = ''; }

  async saveDesignation(): Promise<void> {
    this.formError = '';
    try {
      if (this.editingDesignation && this.designationDraft.id) await this.api.updateDesignation(this.designationDraft);
      else await this.api.createDesignation(this.designationDraft);
      this.closeDesignationModal();
      await this.reload();
    } catch (error: any) {
      this.formError = error?.error?.error ?? error?.message ?? 'Unable to save designation.';
    }
  }

  private emptyDepartment(): Department { return { code: '', name: '', status: 'ACTIVE' }; }
  private emptyDesignation(): Designation { return { code: '', name: '', status: 'ACTIVE' }; }
}