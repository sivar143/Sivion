import { CommonModule } from '@angular/common';
import { ChangeDetectorRef, Component, EventEmitter, OnInit, Output, inject } from '@angular/core';
import { HrApi, Employee } from '../services/hr-api.service';

interface RoleSummary {
  role: string;
  purpose: string;
}

@Component({
  selector: 'app-administration',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './administration.component.html',
  styleUrls: ['./administration.component.scss']
})
export class AdministrationComponent implements OnInit {
  @Output() manageStaff = new EventEmitter<void>();

  private readonly api = inject(HrApi);
  private readonly cdr = inject(ChangeDetectorRef);

  employees: Employee[] = [];
  loading = false;
  error = '';

  readonly roles: RoleSummary[] = [
    { role: 'ADMIN', purpose: 'Platform-wide administration and access to all workspaces' },
    { role: 'HR_ADMIN', purpose: 'HR operations, organization and staff account management' },
    { role: 'HR_USER', purpose: 'HR operations and employee lifecycle workflows' },
    { role: 'MANAGER', purpose: 'Team-level HR workflows and employee oversight' },
    { role: 'EMPLOYEE', purpose: 'Personal employee workspace and records' },
    { role: 'SALES_MANAGER', purpose: 'Sales team and pipeline management' },
    { role: 'SALES_USER', purpose: 'Assigned CRM and sales workflows' },
    { role: 'INVENTORY_MANAGER', purpose: 'Inventory and warehouse management' },
    { role: 'INVENTORY_USER', purpose: 'Inventory operations' },
    { role: 'PROCUREMENT_MANAGER', purpose: 'Supplier, purchase order and receiving workflows' },
    { role: 'FINANCE_MANAGER', purpose: 'Finance and payroll oversight' },
    { role: 'FINANCE_USER', purpose: 'Finance operations and reporting' },
    { role: 'MARKETING_USER', purpose: 'Campaign planning and marketing reporting' }
  ];

  get enabledAccounts(): number {
    return this.employees.filter(employee => employee.accountEnabled !== false && employee.status !== 'INACTIVE').length;
  }

  get roleCounts(): Array<{ role: string; count: number }> {
    const counts = new Map<string, number>();
    this.employees.forEach(employee => {
      const role = employee.role || 'UNASSIGNED';
      counts.set(role, (counts.get(role) || 0) + 1);
    });
    return Array.from(counts, ([role, count]) => ({ role, count }))
      .sort((a, b) => a.role.localeCompare(b.role));
  }

  async ngOnInit(): Promise<void> {
    await this.reload();
  }

  async reload(): Promise<void> {
    this.loading = true;
    this.error = '';
    this.cdr.markForCheck();
    try {
      this.employees = await this.api.employees();
    } catch {
      this.error = 'Staff account information could not be loaded. Check administrator access and try again.';
    } finally {
      this.loading = false;
      this.cdr.markForCheck();
    }
  }

  trackEmployee(_index: number, employee: Employee): number | string {
    return employee.id ?? employee.employeeNumber;
  }
}
