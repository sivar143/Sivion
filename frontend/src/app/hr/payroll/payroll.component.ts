import { CommonModule } from '@angular/common';
import { Component, OnInit, inject } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { AuthService } from '../../services/auth.service';
import { Employee, HrApi, Payslip } from '../../services/hr-api.service';

@Component({
  selector: 'app-hr-payroll',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './payroll.component.html',
  styleUrls: ['../hr.component.scss']
})
export class PayrollComponent implements OnInit {
  private readonly api = inject(HrApi);
  private readonly auth = inject(AuthService);
  employees: Employee[] = [];
  payslips: Payslip[] = [];
  loadError = '';
  formError = '';
  modalOpen = false;
  editing = false;
  draft: Payslip = this.emptyPayslip();

  get canModifyHr(): boolean {
    return ['ADMIN', 'HR_ADMIN', 'HR_USER'].some(role => this.auth.hasRole(role));
  }

  async ngOnInit(): Promise<void> { await this.reload(); }

  async reload(): Promise<void> {
    this.loadError = '';
    const results = await Promise.allSettled([this.api.payslips(), this.api.employees()]);
    if (results[0].status === 'fulfilled') this.payslips = results[0].value;
    else this.loadError = 'Payslips could not be loaded.';
    if (results[1].status === 'fulfilled') this.employees = results[1].value;
    else this.loadError = [this.loadError, 'Employee names could not be loaded.'].filter(Boolean).join(' ');
  }

  employeeName(id: number): string {
    const employee = this.employees.find(item => item.id === id);
    return employee ? `${employee.firstName} ${employee.lastName}` : `Employee #${id}`;
  }

  openNew(): void { this.draft = this.emptyPayslip(); this.editing = false; this.formError = ''; this.modalOpen = true; }
  openEdit(item: Payslip): void { this.draft = { ...item }; this.editing = true; this.formError = ''; this.modalOpen = true; }
  closeModal(): void { this.modalOpen = false; this.formError = ''; }

  async save(): Promise<void> {
    this.formError = '';
    try {
      if (this.editing && this.draft.id) await this.api.updatePayslip(this.draft.id, this.draft);
      else await this.api.createPayslip(this.draft);
      this.closeModal();
      await this.reload();
    } catch (error: any) {
      this.formError = error?.error?.error ?? error?.message ?? 'Unable to save payslip.';
    }
  }

  private emptyPayslip(): Payslip {
    return { employeeId: 0, periodStart: '', periodEnd: '', grossPay: 0, deductions: 0, status: 'DRAFT' };
  }
}