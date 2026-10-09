import  {
  Component, OnInit, inject
}
from '@angular/core';
import  {
  CommonModule
}
from '@angular/common';
import  {
  FormsModule
}
from '@angular/forms';
import  {
  HttpClient, HttpHeaders
}
from '@angular/common/http';
import  {
  firstValueFrom
}
from 'rxjs';
import  {
  AuthService
}
from '../services/auth.service';
@Component( {
  selector:'app-procurement',standalone:true,imports:[CommonModule,FormsModule],templateUrl:'./procurement.component.html',styles:[`.sub{margin-top:28px}.line{display:grid;grid-template-columns:1.4fr 1.2fr .7fr .9fr auto;gap:8px;align-items:center;margin:8px 0}textarea{width:100%;min-height:80px;padding:11px}select{max-width:260px}`]
})
export class ProcurementComponent implements OnInit  {
  http=inject(HttpClient);
  auth=inject(AuthService);
  base='/api/v1/procurement';
  inventoryBase='/api/v1/inventory';
  suppliers:any[]=[];
  requests:any[]=[];
  orders:any[]=[];
  receipts:any[]=[];
  materials:any[]=[];
  warehouses:any[]=[];
  loadError='';
  mode='';
  supplier:any= {
    code:'',name:'',email:'',phone:'',address:''
  };
  request:any= {
    description:'',estimatedAmount:0,requestedBy:'user'
  };
  order:any= {
    supplierId:0,items:[this.newOrderItem()]
  };
  receipt:any= {
    purchaseOrderId:0,productId:0,warehouseId:0,quantity:0
  };
  async ngOnInit() {
    await this.reload()
  }
  async headers() {
    return {
      headers:new HttpHeaders( {
        'Authorization':`Bearer ${await this.auth.token()}`,'X-Tenant-ID':'1'
      })
    }
  }
  async reload() {
    let h: { headers: HttpHeaders };
    try {
      h = await this.headers();
    } catch {
      this.loadError = 'Unable to authenticate to load procurement data.';
      return;
    }

    const results = await Promise.allSettled([
      firstValueFrom(this.http.get<any[]>(this.base + '/suppliers', h)),
      firstValueFrom(this.http.get<any[]>(this.base + '/requests', h)),
      firstValueFrom(this.http.get<any[]>(this.base + '/orders', h)),
      firstValueFrom(this.http.get<any[]>(this.base + '/goods-received', h)),
      firstValueFrom(this.http.get<any[]>(this.inventoryBase + '/warehouses', h))
    ]);
    const failures: string[] = [];

    if (results[0].status === 'fulfilled') this.suppliers = results[0].value;
    else failures.push('suppliers');
    if (results[1].status === 'fulfilled') this.requests = results[1].value;
    else failures.push('purchase requests');
    if (results[2].status === 'fulfilled') this.orders = results[2].value;
    else failures.push('purchase orders');
    if (results[3].status === 'fulfilled') this.receipts = results[3].value;
    else failures.push('goods receipts');
    if (results[4].status === 'fulfilled') this.warehouses = results[4].value;
    else failures.push('warehouses');

    const warehouse = this.warehouses.find(w => w.status === 'ACTIVE') ?? this.warehouses[0];
    if (warehouse) {
      try {
        this.materials = await firstValueFrom(this.http.get<any[]>(
          this.inventoryBase + '/materials',
          { ...h, params: { warehouseId: warehouse.id } }
        ));
      } catch {
        failures.push('materials');
      }
    } else {
      this.materials = [];
      if (results[4].status === 'fulfilled') failures.push('materials (no warehouse available)');
    }

    this.loadError = failures.length
      ? `Unable to load ${failures.join(', ')}. Other available procurement data is still shown.`
      : '';
  }
  get receivableOrders() {
    return this.orders.filter(x=>x.status==='ISSUED'||x.status==='PARTIALLY_RECEIVED');
  }
  get selectedOrder() {
    return this.orders.find(x=>x.id===this.receipt.purchaseOrderId);
  }
  newOrderItem() {
    return {
      productId:0,warehouseId:0,quantity:1,unitPrice:0
    }
  }
  addOrderItem() {
    this.order.items.push(this.newOrderItem())
  }
  removeOrderItem(i:number) {
    if(this.order.items.length>1)this.order.items.splice(i,1)
  }
  supplierName(id:number) {
    return this.suppliers.find(x=>x.id===id)?.name||`Supplier #${id}`;
  }
  orderName(id:number) {
    return this.orders.find(x=>x.id===id)?.orderNumber||`PO #${id}`;
  }
  materialName(id:number) {
    return this.materials.find(x=>x.id===id)?.name||`Material #${id}`;
  }
  warehouseName(id:number) {
    return this.warehouses.find(x=>x.id===id)?.name||`Warehouse #${id}`;
  }
  syncReceiptLine() {
    this.receipt.productId=0;
    this.receipt.warehouseId=0;
    this.receipt.quantity=0
  }
  syncReceiptWarehouse() {
    const line=this.selectedOrder?.items?.find((i:any)=>i.productId===this.receipt.productId);
    if(line)this.receipt.warehouseId=line.warehouseId
  }
  async updateRequest(id:number,status:string) {
    try {
      const h=await this.headers();
      await firstValueFrom(this.http.patch(this.base+`/requests/${id}`, {
      }, {
        ...h,params: {
          status
        }
      }));
      await this.reload();
    }
    catch(e:any) {
      alert(e?.error?.message||'Unable to update request')
    }
  }
  async updateOrder(id:number,status:string) {
    try {
      const h=await this.headers();
      await firstValueFrom(this.http.patch(this.base+`/orders/${id}`, {
      }, {
        ...h,params: {
          status
        }
      }));
      await this.reload();
    }
    catch(e:any) {
      alert(e?.error?.message||'Unable to update order')
    }
  }
  async submit() {
    try {
      const h=await this.headers();
      let path='';
      let body:any;
      if(this.mode==='supplier') {
        path='suppliers';
        body=this.supplier;
      }
      if(this.mode==='request') {
        path='requests';
        body=this.request;
      }
      if(this.mode==='order') {
        path='orders';
        body=this.order;
      }
      if(this.mode==='receipt') {
        path='goods-received';
        body=this.receipt;
      }
      await firstValueFrom(this.http.post(this.base+'/'+path,body,h));
      this.mode='';
      await this.reload();
    }
    catch(e:any) {
      alert(e?.error?.message||'Request failed')
    }
  }
}
