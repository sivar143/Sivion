import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { firstValueFrom } from 'rxjs';
import { AuthService } from './auth.service';
export interface Warehouse {id:number;code:string;name:string;status:string}
export interface Material {id?:number;sku:string;name:string;description?:string;unit:string;initialQuantity?:number;reorderLevel?:number;status?:string;quantity?:number}
export interface DispatchItem {productId:number;warehouseId:number;quantity:number}
export interface Dispatch {id?:number;dispatchNumber?:string;customerId:number;referenceNumber:string;dispatchDate?:string;deliveryAddress?:string;notes?:string;status?:string;items:DispatchItem[]}
@Injectable({providedIn:'root'}) export class InventoryApi {
 private http=inject(HttpClient); private auth=inject(AuthService); private base='/api/v1/inventory';
 private async options(){return {headers:new HttpHeaders({'Authorization':`Bearer ${await this.auth.token()}`,'X-Tenant-ID':'1'})};}
 async warehouses(){return firstValueFrom(this.http.get<Warehouse[]>(`${this.base}/warehouses`,await this.options()));}
 async materials(){return firstValueFrom(this.http.get<Material[]>(`${this.base}/materials`,await this.options()));}
 async createMaterial(x:Material){return firstValueFrom(this.http.post<Material>(`${this.base}/materials`,x,await this.options()));}
 async stockIn(productId:number,warehouseId:number,quantity:number,reference:string){return firstValueFrom(this.http.post<void>(`${this.base}/stock-in`,{productId,warehouseId,quantity,reference},await this.options()));}
 async adjustment(productId:number,warehouseId:number,quantity:number,reference:string){return firstValueFrom(this.http.post<void>(`${this.base}/adjustments`,{productId,warehouseId,quantity,reference},await this.options()));}
 async dispatch(x:Dispatch){return firstValueFrom(this.http.post<Dispatch>(`${this.base}/dispatches`,x,await this.options()));}
 async history(){return firstValueFrom(this.http.get<Dispatch[]>(`${this.base}/dispatches`,await this.options()));}
}
