import {Component,OnInit,Input,inject} from '@angular/core';
import {CommonModule} from '@angular/common'; import {FormsModule} from '@angular/forms';
import {HrApi,Employee,Department,Attendance,LeaveRequest,Goal,Payslip,Designation} from './hr-api.service'; import {AuthService} from './auth.service';

@Component({selector:'app-hr',standalone:true,imports:[CommonModule,FormsModule],templateUrl:'./hr.component.html',styles:[`
:host .hr-actions{display:flex;align-items:center;gap:1vw}.form-grid{display:grid;grid-template-columns:1fr 1fr;gap:12px}.hint{font-size:12px;color:#667085;margin:0}.form-error{margin:0;padding:8px 10px;border-radius:6px;background:#fef3f2;color:#b42318}.check{display:flex;align-items:center;gap:8px;font-size:13px}.check input{width:auto}.inactive-pill{background:#f2f4f7;color:#667085}textarea{width:100%;min-height:90px;border:1px solid #d0d5dd;border-radius:8px;padding:11px 12px;font:inherit}
`]})
export class HrComponent implements OnInit {
 @Input() section='employees'; api=inject(HrApi); auth=inject(AuthService);
 employees:Employee[]=[]; departments:Department[]=[]; designations:Designation[]=[]; attendance:Attendance[]=[]; leaves:LeaveRequest[]=[]; goals:Goal[]=[]; payslips:Payslip[]=[];
 employeeModal=false; editingEmployee=false; departmentModal=false; editingDepartment=false; payslipModal=false; editingPayslip=false; designationModal=false; editingDesignation=false; formError='';
 employeeDraft:Employee=this.emptyEmployee(); departmentDraft:Department={code:'',name:'',status:'ACTIVE'}; designationDraft:Designation={code:'',name:'',status:'ACTIVE'}; payslipDraft:Payslip=this.emptyPayslip();
 async ngOnInit(){await this.reload();}
 get canModifyHr(){return this.auth.hasRole('ADMIN')||this.auth.hasRole('HR_ADMIN')||this.auth.hasRole('HR_USER');}
 get isAdmin(){return this.auth.hasRole('ADMIN');}
 get availableRoles(){return this.isAdmin?['ADMIN','HR_ADMIN','HR_USER','MANAGER','EMPLOYEE','SALES_MANAGER','SALES_USER','INVENTORY_MANAGER','INVENTORY_USER','WAREHOUSE_MANAGER','WAREHOUSE_USER','PROCUREMENT_MANAGER','FINANCE_MANAGER','FINANCE_USER','MARKETING_USER']:['HR_USER','MANAGER','EMPLOYEE'];}
 async reload(){[this.employees,this.departments,this.designations,this.attendance,this.leaves,this.goals,this.payslips]=await Promise.all([this.api.employees(),this.api.departments(),this.api.designations(),this.api.attendance(),this.api.leaves(),this.api.goals(),this.api.payslips()]);}
 get pendingLeaves(){return this.leaves.filter(x=>x.status==='PENDING').length;} get activeEmployees(){return this.employees.filter(x=>x.status==='ACTIVE').length;} get sectionTitle(){return this.section[0].toUpperCase()+this.section.slice(1);}
 get availableParents(){const currentId=this.departmentDraft.id;return this.departments.filter(d=>d.id!==currentId);}
 departmentName(id?:number){return id?this.departments.find(x=>x.id===id)?.name||`Department #${id}`:'—';}
 employeeName(id:number){const e=this.employees.find(x=>x.id===id);return e?`${e.firstName} ${e.lastName}`:`Employee #${id}`;}
 employeesInDepartment(id?:number){return this.employees.filter(x=>x.departmentId===id).length;}
 openNewEmployee(){this.employeeDraft=this.emptyEmployee();this.employeeModal=true;this.formError='';}
 openEditEmployee(e:Employee){this.employeeDraft={...e,password:'',temporaryPassword:false};this.editingEmployee=true;this.employeeModal=true;this.formError='';}
 closeEmployeeModal(){this.employeeModal=false;this.editingEmployee=false;this.formError='';}
 async saveEmployee(){this.formError='';try{if(this.editingEmployee&&this.employeeDraft.id){await this.api.updateEmployee(this.employeeDraft.id,this.employeeDraft);}else{await this.api.createEmployee(this.employeeDraft);}this.closeEmployeeModal();await this.reload();}catch(e:any){this.formError=e?.error?.error||e?.message||'Unable to save staff account.';}}
 openNewDepartment(){this.departmentDraft={code:'',name:'',status:'ACTIVE'};this.editingDepartment=false;this.formError='';this.departmentModal=true;}
 openEditDepartment(d:Department){this.departmentDraft={...d};this.editingDepartment=true;this.formError='';this.departmentModal=true;}
 closeDepartmentModal(){this.departmentModal=false;this.editingDepartment=false;this.formError='';}
 async saveDepartment(){this.formError='';try{if(this.editingDepartment){await this.api.updateDepartment(this.departmentDraft);}else{await this.api.createDepartment(this.departmentDraft);}this.closeDepartmentModal();await this.reload();}catch(e:any){this.formError=e?.error?.error||e?.message||'Unable to save department.';}}
 async updateLeave(l:LeaveRequest,status:string){if(l.id){try{await this.api.updateLeave(l.id,status);await this.reload();}catch(e:any){this.formError='Unable to update leave request.';}}}
 openNewDesignation(){this.designationDraft={code:'',name:'',status:'ACTIVE'};this.editingDesignation=false;this.designationModal=true;this.formError='';}
 openEditDesignation(d:Designation){this.designationDraft={...d};this.editingDesignation=true;this.designationModal=true;this.formError='';}
 closeDesignationModal(){this.designationModal=false;this.editingDesignation=false;this.formError='';}
 async saveDesignation(){this.formError='';try{if(this.editingDesignation&&this.designationDraft.id)await this.api.updateDesignation(this.designationDraft);else await this.api.createDesignation(this.designationDraft);this.closeDesignationModal();await this.reload();}catch(e:any){this.formError=e?.error?.error||e?.message||'Unable to save designation.';}}
 openNewPayslip(){this.payslipDraft=this.emptyPayslip();this.payslipModal=true;this.editingPayslip=false;this.formError='';}
 openEditPayslip(p:Payslip){this.payslipDraft={...p};this.payslipModal=true;this.editingPayslip=true;this.formError='';}
 closePayslipModal(){this.payslipModal=false;this.editingPayslip=false;this.formError='';}
 async savePayslip(){this.formError='';try{if(this.editingPayslip&&this.payslipDraft.id)await this.api.updatePayslip(this.payslipDraft.id,this.payslipDraft);else await this.api.createPayslip(this.payslipDraft);this.closePayslipModal();await this.reload();}catch(e:any){this.formError=e?.error?.error||e?.message||'Unable to save payslip.';}}
 private emptyEmployee():Employee{return{employeeNumber:'',firstName:'',lastName:'',email:'',status:'ACTIVE',role:'EMPLOYEE',accountEnabled:true,temporaryPassword:false,password:''};}
 private emptyPayslip():Payslip{return{employeeId:0,periodStart:'',periodEnd:'',grossPay:0,deductions:0,status:'DRAFT'};}
}
