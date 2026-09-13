import {inject,Injectable} from '@angular/core';
import {AuthService} from './auth.service';

export interface SalesOrderItem{materialId:number;quantity:number;unitPrice:number;description?:string;lineTotal?:number;}
export interface SalesOrder{id?:number;orderNumber:string;customerId:number;opportunityId?:number;status:string;totalAmount:number;orderDate:string;deliveryAddress?:string;notes?:string;items:SalesOrderItem[];}

@Injectable({providedIn:'root'})
export class SalesOrderApi{
 private auth=inject(AuthService); private base='/api/v1/sales/orders';
 private headers(){return {'Content-Type':'application/json','Authorization':`Bearer ${this.auth.token()||''}`};}
 async list():Promise<SalesOrder[]>{const r=await fetch(this.base,{headers:this.headers()});if(!r.ok)throw new Error(await r.text());return r.json();}
 async create(request:{customerId:number;opportunityId?:number;deliveryAddress?:string;notes?:string;items:SalesOrderItem[]}):Promise<SalesOrder>{const r=await fetch(this.base,{method:'POST',headers:this.headers(),body:JSON.stringify(request)});if(!r.ok)throw new Error(await r.text());return r.json();}
 async updateStatus(id:number,status:string):Promise<SalesOrder>{const r=await fetch(`${this.base}/${id}/status`,{method:'PATCH',headers:this.headers(),body:JSON.stringify({status})});if(!r.ok)throw new Error(await r.text());return r.json();}
}
