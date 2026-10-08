import  {
  CommonModule
}
from '@angular/common';
import  {
  Component, Input, OnInit, inject
}
from '@angular/core';
import  {
  FormsModule
}
from '@angular/forms';
import  {
  Attendance,
  Department,
  Designation,
  Employee,
  Goal,
  HrApi,
  LeaveRequest,
  Payslip
}
from '../services/hr-api.service';
import  {
  AuthService
}
from '../services/auth.service';
@Component( {
  selector: 'app-hr',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './hr.component.html',
  styles: [`
    :host .hr-actions {
      display: flex;
      align-items: center;
      gap: 1vw;
    }

    .form-grid {
      display: grid;
      grid-template-columns: 1fr 1fr;
      gap: 12px;
    }

    .hint {
      font-size: 12px;
      color: #667085;
      margin: 0;
    }

    .form-error {
      margin: 0;
      padding: 8px 10px;
      border-radius: 6px;
      background: #fef3f2;
      color: #b42318;
    }

    .check {
      display: flex;
      align-items: center;
      gap: 8px;
      font-size: 13px;
    }

    .check input {
      width: auto;
    }

    .inactive-pill {
      background: #f2f4f7;
      color: #667085;
    }

    textarea {
      width: 100%;
      min-height: 90px;
      border: 1px solid #d0d5dd;
      border-radius: 8px;
      padding: 11px 12px;
      font: inherit;
    }
  `]
})
export class HrComponent implements OnInit  {
  @Input() section = 'employees';
  api = inject(HrApi);
  auth = inject(AuthService);
  employees: Employee[] = [];
  departments: Department[] = [];
  designations: Designation[] = [];
  attendance: Attendance[] = [];
  leaves: LeaveRequest[] = [];
  goals: Goal[] = [];
  payslips: Payslip[] = [];
  employeeModal = false;
  editingEmployee = false;
  departmentModal = false;
  editingDepartment = false;
  payslipModal = false;
  editingPayslip = false;
  designationModal = false;
  editingDesignation = false;
  formError = '';
  employeeDraft: Employee = this.emptyEmployee();
  departmentDraft: Department =  {
    code: '',
    name: '',
    status: 'ACTIVE'
  };
  designationDraft: Designation =  {
    code: '',
    name: '',
    status: 'ACTIVE'
  };
  payslipDraft: Payslip = this.emptyPayslip();
  async ngOnInit(): Promise<void>  {
    await this.reload();
  }
  get canModifyHr(): boolean  {
    return (
    this.auth.hasRole('ADMIN') ||
    this.auth.hasRole('HR_ADMIN') ||
    this.auth.hasRole('HR_USER')
    );
  }
  get isAdmin(): boolean  {
    return this.auth.hasRole('ADMIN');
  }
  get availableRoles(): string[]  {
    if (this.isAdmin)  {
      return [
      'ADMIN',
      'HR_ADMIN',
      'HR_USER',
      'MANAGER',
      'EMPLOYEE',
      'SALES_MANAGER',
      'SALES_USER',
      'INVENTORY_MANAGER',
      'INVENTORY_USER',
      'WAREHOUSE_MANAGER',
      'WAREHOUSE_USER',
      'PROCUREMENT_MANAGER',
      'FINANCE_MANAGER',
      'FINANCE_USER',
      'MARKETING_USER'
      ];
    }
    return ['HR_USER', 'MANAGER', 'EMPLOYEE'];
  }
  async reload(): Promise<void>  {
    [
    this.employees,
    this.departments,
    this.designations,
    this.attendance,
    this.leaves,
    this.goals,
    this.payslips
    ] = await Promise.all([
    this.api.employees(),
    this.api.departments(),
    this.api.designations(),
    this.api.attendance(),
    this.api.leaves(),
    this.api.goals(),
    this.api.payslips()
    ]);
  }
  get pendingLeaves(): number  {
    return this.leaves.filter((leave) => leave.status === 'PENDING').length;
  }
  get activeEmployees(): number  {
    return this.employees.filter((employee) => employee.status === 'ACTIVE').length;
  }
  get sectionTitle(): string  {
    return this.section[0].toUpperCase() + this.section.slice(1);
  }
  get availableParents(): Department[]  {
    const currentId = this.departmentDraft.id;
    return this.departments.filter((department) => department.id !== currentId);
  }
  departmentName(id?: number): string  {
    if (!id)  {
      return '—';
    }
    const department = this.departments.find((item) => item.id === id);
    return department?.name ?? `Department #${id}`;
  }
  employeeName(id: number): string  {
    const employee = this.employees.find((item) => item.id === id);
    return employee
    ? `${employee.firstName} ${employee.lastName}`
    : `Employee #${id}`;
  }
  employeesInDepartment(id?: number): number  {
    return this.employees.filter((employee) => employee.departmentId === id).length;
  }
  openNewEmployee(): void  {
    this.employeeDraft = this.emptyEmployee();
    this.employeeModal = true;
    this.formError = '';
  }
  openEditEmployee(employee: Employee): void  {
    this.employeeDraft =  {
      ...employee,
      password: '',
      temporaryPassword: false
    };
    this.editingEmployee = true;
    this.employeeModal = true;
    this.formError = '';
  }
  closeEmployeeModal(): void  {
    this.employeeModal = false;
    this.editingEmployee = false;
    this.formError = '';
  }
  async saveEmployee(): Promise<void>  {
    this.formError = '';
    try  {
      if (this.editingEmployee && this.employeeDraft.id)  {
        await this.api.updateEmployee(this.employeeDraft.id, this.employeeDraft);
      }
      else  {
        await this.api.createEmployee(this.employeeDraft);
      }
      this.closeEmployeeModal();
      await this.reload();
    }
    catch (error: any)  {
      this.formError =
      error?.error?.error ??
      error?.message ??
      'Unable to save staff account.';
    }
  }
  openNewDepartment(): void  {
    this.departmentDraft =  {
      code: '',
      name: '',
      status: 'ACTIVE'
    };
    this.editingDepartment = false;
    this.formError = '';
    this.departmentModal = true;
  }
  openEditDepartment(department: Department): void  {
    this.departmentDraft =  {
      ...department
    };
    this.editingDepartment = true;
    this.formError = '';
    this.departmentModal = true;
  }
  closeDepartmentModal(): void  {
    this.departmentModal = false;
    this.editingDepartment = false;
    this.formError = '';
  }
  async saveDepartment(): Promise<void>  {
    this.formError = '';
    try  {
      if (this.editingDepartment)  {
        await this.api.updateDepartment(this.departmentDraft);
      }
      else  {
        await this.api.createDepartment(this.departmentDraft);
      }
      this.closeDepartmentModal();
      await this.reload();
    }
    catch (error: any)  {
      this.formError =
      error?.error?.error ??
      error?.message ??
      'Unable to save department.';
    }
  }
  async updateLeave(leave: LeaveRequest, status: string): Promise<void>  {
    if (!leave.id)  {
      return;
    }
    try  {
      await this.api.updateLeave(leave.id, status);
      await this.reload();
    }
    catch (error: any)  {
      this.formError = 'Unable to update leave request.';
    }
  }
  openNewDesignation(): void  {
    this.designationDraft =  {
      code: '',
      name: '',
      status: 'ACTIVE'
    };
    this.editingDesignation = false;
    this.designationModal = true;
    this.formError = '';
  }
  openEditDesignation(designation: Designation): void  {
    this.designationDraft =  {
      ...designation
    };
    this.editingDesignation = true;
    this.designationModal = true;
    this.formError = '';
  }
  closeDesignationModal(): void  {
    this.designationModal = false;
    this.editingDesignation = false;
    this.formError = '';
  }
  async saveDesignation(): Promise<void>  {
    this.formError = '';
    try  {
      if (this.editingDesignation && this.designationDraft.id)  {
        await this.api.updateDesignation(this.designationDraft);
      }
      else  {
        await this.api.createDesignation(this.designationDraft);
      }
      this.closeDesignationModal();
      await this.reload();
    }
    catch (error: any)  {
      this.formError =
      error?.error?.error ??
      error?.message ??
      'Unable to save designation.';
    }
  }
  openNewPayslip(): void  {
    this.payslipDraft = this.emptyPayslip();
    this.payslipModal = true;
    this.editingPayslip = false;
    this.formError = '';
  }
  openEditPayslip(payslip: Payslip): void  {
    this.payslipDraft =  {
      ...payslip
    };
    this.payslipModal = true;
    this.editingPayslip = true;
    this.formError = '';
  }
  closePayslipModal(): void  {
    this.payslipModal = false;
    this.editingPayslip = false;
    this.formError = '';
  }
  async savePayslip(): Promise<void>  {
    this.formError = '';
    try  {
      if (this.editingPayslip && this.payslipDraft.id)  {
        await this.api.updatePayslip(this.payslipDraft.id, this.payslipDraft);
      }
      else  {
        await this.api.createPayslip(this.payslipDraft);
      }
      this.closePayslipModal();
      await this.reload();
    }
    catch (error: any)  {
      this.formError =
      error?.error?.error ??
      error?.message ??
      'Unable to save payslip.';
    }
  }
  private emptyEmployee(): Employee  {
    return  {
      employeeNumber: '',
      firstName: '',
      lastName: '',
      email: '',
      status: 'ACTIVE',
      role: 'EMPLOYEE',
      accountEnabled: true,
      temporaryPassword: false,
      password: ''
    };
  }
  private emptyPayslip(): Payslip  {
    return  {
      employeeId: 0,
      periodStart: '',
      periodEnd: '',
      grossPay: 0,
      deductions: 0,
      status: 'DRAFT'
    };
  }
}
