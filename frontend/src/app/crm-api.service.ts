import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { firstValueFrom } from 'rxjs';
import { AuthService } from './auth.service';
export interface Customer { id?:number; code:string; name:string; email?:string; phone?:string; status:string; }
export interface Contact { id?:number; customerId:number; firstName:string; lastName?:string; jobTitle?:string; email?:string; phone?:string; mobile?:string; primaryContact:boolean; status:string; }
export interface Lead { id?:number; leadNumber?:string; name:string; email?:string; phone?:string; companyName?:string; source:string; status:string; rating:string; estimatedValue?:number; assignedTo?:string; expectedCloseDate?:string; }
export interface Opportunity { id?:number; opportunityNumber?:string; name:string; customerId?:number; leadId?:number; stage:string; amount?:number; probability?:number; expectedCloseDate?:string; assignedTo?:string; description?:string; }
export interface Activity { id?:number; customerId?:number; leadId?:number; opportunityId?:number; type:string; subject:string; description?:string; dueAt?:string; completedAt?:string; assignedTo?:string; }
@Injectable({providedIn:'root'}) export class CrmApi {
 private http=inject(HttpClient); private auth=inject(AuthService); private base='/api/v1/crm';
 private async options(){return {headers:new HttpHeaders({'Authorization':`Bearer ${await this.auth.token()}`,'X-Tenant-ID':'1'})};}
 async customers(q=''){const o=await this.options(); return firstValueFrom(this.http.get<Customer[]>(`${this.base}/customers`,{...o,params:q?{q}:{}}));}
 async createCustomer(x:Customer){return firstValueFrom(this.http.post<Customer>(`${this.base}/customers`,x,await this.options()));}
 async updateCustomer(id:number,x:Customer){return firstValueFrom(this.http.put<Customer>(`${this.base}/customers/${id}`,x,await this.options()));}
 async contacts(customerId?:number){return firstValueFrom(this.http.get<Contact[]>(`${this.base}/contacts`,{...(await this.options()),params:customerId?{customerId}:{}}));}
 async createContact(x:Contact){return firstValueFrom(this.http.post<Contact>(`${this.base}/contacts`,x,await this.options()));}
 async leads(status=''){return firstValueFrom(this.http.get<Lead[]>(`${this.base}/leads`,{...(await this.options()),params:status?{status}:{}}));}
 async createLead(x:Lead){return firstValueFrom(this.http.post<Lead>(`${this.base}/leads`,x,await this.options()));}
 async opportunities(stage=''){return firstValueFrom(this.http.get<Opportunity[]>(`${this.base}/opportunities`,{...(await this.options()),params:stage?{stage}:{}}));}
 async createOpportunity(x:Opportunity){return firstValueFrom(this.http.post<Opportunity>(`${this.base}/opportunities`,x,await this.options()));}
 async activities(customerId?:number){return firstValueFrom(this.http.get<Activity[]>(`${this.base}/activities`,{...(await this.options()),params:customerId?{customerId}:{}}));}
 async createActivity(x:Activity){return firstValueFrom(this.http.post<Activity>(`${this.base}/activities`,x,await this.options()));}
}
