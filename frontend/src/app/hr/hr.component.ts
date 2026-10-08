import { Component, OnInit, Input, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { HrApi, Employee, Department, Designation, Attendance, LeaveRequest, Goal } from '../services/hr-api.service';

@Component({selector:'app-hr',standalone:true,imports:[CommonModule,FormsModule],templateUrl:'./hr.component.html',styles:[`
:host .hr-actions{display:flex;align-items:center;gap:1vw}.form-grid{display:grid;grid-template-columns:1fr 1fr;gap:12px}.hint{font-size:12px;color:#667085;margin:0}.form-error{margin:0;padding:8px 10px;border-radius:6px;background:#fef3f2;color:#b42318}.check{display:flex;align-items:center;gap:8px;font-size:13px}.check input{width:auto}.inactive-pill{background:#f2f4f7;color:#667085}textarea{width:100%;min-height:90px;border:1px solid #d0d5dd;border-radius:8px;padding:11px 12px;font:inherit}
`]})
export class HrComponent implements OnInit {
 @Input() section='employees'; api=inject(HrApi); employees:Employee[]=[]; departments:Department[]=[]; designations:Designation[]=[]; attendance:Attendance[]=[]; leaves:LeaveRequest[]=[]; goals:Goal[]=[];
 newEmployee=false; departmentModal=false; editingDepartment=false; designationModal=false; editingDesignation=false; organizationView:'departments'|'designations'='departments'; formError=''; departmentDraft:Department={code:'',name:'',status:'ACTIVE'}; designationDraft:Designation={code:'',name:'',status:'ACTIVE'}; draft:Employee={employeeNumber:'',firstName:'',lastName:'',email:'',status:'ACTIVE'};
 async ngOnInit(){await this.reload();}
 async reload(){[this.employees,this.departments,this.designations,this.attendance,this.leaves,this.goals]=await Promise.all([this.api.employees(),this.api.departments(),this.api.designations(),this.api.attendance(),this.api.leaves(),this.api.goals()]);}
 get pendingLeaves(){return this.leaves.filter(x=>x.status==='PENDING').length;} get activeEmployees(){return this.employees.filter(x=>x.status==='ACTIVE').length;} get sectionTitle(){return this.section[0].toUpperCase()+this.section.slice(1);}
 get availableParents(){const currentId=this.departmentDraft.id;return this.departments.filter(d=>d.id!==currentId);}
 departmentName(id?:number){return id?this.departments.find(x=>x.id===id)?.name||`Department #${id}`:'—';}
 employeeName(id:number){const e=this.employees.find(x=>x.id===id);return e?`${e.firstName} ${e.lastName}`:`Employee #${id}`;}
 employeesInDepartment(id?:number){return this.employees.filter(x=>x.departmentId===id).length;}
 openNewEmployee(){this.draft={employeeNumber:'',firstName:'',lastName:'',email:'',status:'ACTIVE'};this.newEmployee=true;}
 openNewDepartment(){this.departmentDraft={code:'',name:'',status:'ACTIVE'};this.editingDepartment=false;this.formError='';this.departmentModal=true;}
 openEditDepartment(d:Department){this.departmentDraft={...d};this.editingDepartment=true;this.formError='';this.departmentModal=true;} openNewDesignation(){this.designationDraft={code:'',name:'',status:'ACTIVE'};this.editingDesignation=false;this.formError='';this.designationModal=true;} openEditDesignation(d:Designation){this.designationDraft={...d};this.editingDesignation=true;this.formError='';this.designationModal=true;} closeDesignationModal(){this.designationModal=false;this.editingDesignation=false;this.formError='';}
 closeDepartmentModal(){this.departmentModal=false;this.editingDepartment=false;this.formError='';}
 async saveEmployee(){if(this.draft.designationId)this.draft.designation=this.designations.find(x=>x.id===this.draft.designationId)?.name;await this.api.createEmployee(this.draft);this.newEmployee=false;await this.reload();}
 async saveDesignation(){this.formError='';try{if(this.editingDesignation){await this.api.updateDesignation(this.designationDraft);}else{await this.api.createDesignation(this.designationDraft);}this.closeDesignationModal();await this.reload();}catch(error:any){this.formError=error?.error?.error||error?.message||'Unable to save designation.';}} async saveDepartment(){this.formError='';try{if(this.editingDepartment){await this.api.updateDepartment(this.departmentDraft);}else{await this.api.createDepartment(this.departmentDraft);}this.closeDepartmentModal();await this.reload();}catch(error:any){this.formError=error?.error?.error||error?.message||'Unable to save department.';}}
 async approve(l:LeaveRequest){if(l.id){await this.api.updateLeave(l.id,'APPROVED');await this.reload();}}
}