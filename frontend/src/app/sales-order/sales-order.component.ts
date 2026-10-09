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
  SalesOrderApi, SalesOrder, SalesOrderItem
}
from '../services/sales-order-api.service';
import  {
  CrmApi, Customer, Opportunity
}
from '../services/crm-api.service';
import  {
  InventoryApi, Material
}
from '../services/inventory-api.service';
@Component( {
  selector:'app-sales-orders',standalone:true,imports:[CommonModule,FormsModule],templateUrl:'./sales-order.component.html'
})
export class SalesOrderComponent implements OnInit  {
  api=inject(SalesOrderApi);
  crm=inject(CrmApi);
  inventory=inject(InventoryApi);
  orders:SalesOrder[]=[];
  customers:Customer[]=[];
  opportunities:Opportunity[]=[];
  materials:Material[]=[];
  loadError='';
  newOrder=false;
  statuses=['DRAFT','CONFIRMED','RESERVED','PARTIALLY_DISPATCHED','DISPATCHED','CANCELLED'];
  draft: {
    customerId:number;
    opportunityId?:number;
    deliveryAddress:string;
    notes:string;
    items:SalesOrderItem[]
  }
  = {
    customerId:0,deliveryAddress:'',notes:'',items:[ {
      materialId:0,quantity:1,unitPrice:0
    }]
  };
  async ngOnInit() {
    await this.reload();
  }
  async reload() {
    const results = await Promise.allSettled([
      this.api.list(),
      this.crm.customers(''),
      this.crm.opportunities(),
      this.inventory.materials()
    ]);
    const failures: string[] = [];

    if (results[0].status === 'fulfilled') this.orders = results[0].value;
    else failures.push('sales orders');
    if (results[1].status === 'fulfilled') this.customers = results[1].value;
    else failures.push('customers');
    if (results[2].status === 'fulfilled') this.opportunities = results[2].value;
    else failures.push('opportunities');
    if (results[3].status === 'fulfilled') this.materials = results[3].value;
    else failures.push('materials');

    this.loadError = failures.length
      ? `Some sales-order data could not be loaded: ${failures.join(', ')}. Other available data is still shown.`
      : '';
  }
  get total() {
    return this.orders.reduce((a,o)=>a+Number(o.totalAmount||0),0);
  }
  count(s:string) {
    return this.orders.filter(o=>o.status===s).length;
  }
  customerName(id:number) {
    return this.customers.find(c=>c.id===id)?.name||`Customer #${id}`;
  }
  opportunityName(id?:number) {
    return id?this.opportunities.find(o=>o.id===id)?.name||`Opportunity #${id}`:'—';
  }
  addLine() {
    this.draft.items.push( {
      materialId:0,quantity:1,unitPrice:0
    });
  }
  removeLine(i:number) {
    if(this.draft.items.length>1)this.draft.items.splice(i,1);
  }
  async save() {
    await this.api.create(this.draft);
    this.newOrder=false;
    this.draft= {
      customerId:0,deliveryAddress:'',notes:'',items:[ {
        materialId:0,quantity:1,unitPrice:0
      }]
    };
    await this.reload();
  }
  async changeStatus(o:SalesOrder,status:string) {
    try {
      await this.api.updateStatus(o.id!,status);
      await this.reload();
    }
    catch(e) {
      alert(e instanceof Error?e.message:'Unable to update order');
      await this.reload();
    }
  }
}
