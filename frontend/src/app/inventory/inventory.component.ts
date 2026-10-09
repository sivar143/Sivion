import  {
  Component, inject, OnInit
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
  InventoryApi, Material, Warehouse, InventoryTransaction
}
from '../services/inventory-api.service';
import  {
  CrmApi, Customer
}
from '../services/crm-api.service';
interface DispatchLine  {
  productId:number;
  warehouseId:number;
  quantity:number
}
@Component( {
  selector:'app-inventory',standalone:true,imports:[CommonModule,FormsModule],templateUrl:'./inventory.component.html',styles:[`.sub{margin-top:28px}.inventory-cards{margin-bottom:10px}.muted{font-size:12px;color:#667085}.warehouse{padding:9px;border:1px solid #d0d5dd;border-radius:8px}textarea{width:100%;min-height:80px;padding:11px;font:inherit}.line{display:grid;grid-template-columns:1fr 130px auto;gap:8px;margin-bottom:8px}.negative{font-weight:700}`]
})
export class InventoryComponent implements OnInit  {
  api=inject(InventoryApi);
  crm=inject(CrmApi);
  materials:Material[]=[];
  warehouses:Warehouse[]=[];
  customers:Customer[]=[];
  dispatches:any[]=[];
  transactions:InventoryTransaction[]=[];
  loadError='';
  private readonly loadFailures = new Set<string>();
  mode='';
  selectedId=0;
  warehouseId=0;
  quantity=0;
  reference='';
  customerId=0;
  address='';
  notes='';
  canManage=true;
  warehouseDraft= {
    code:'',name:''
  };
  draft:Material= {
    sku:'',name:'',unit:'PCS',initialQuantity:0,reorderLevel:0
  };
  dispatchLines:DispatchLine[]=[ {
    productId:0,warehouseId:0,quantity:1
  }];
  async ngOnInit() {
    const [warehouses, customers] = await Promise.allSettled([
      this.api.warehouses(),
      this.crm.customers('')
    ]);
    if (warehouses.status === 'fulfilled') {
      this.warehouses = warehouses.value;
      this.warehouseId = this.warehouses.find(w => w.status === 'ACTIVE')?.id || 0;
    }
    this.setLoadFailure('warehouses', warehouses.status === 'rejected');

    if (customers.status === 'fulfilled') this.customers = customers.value;
    this.setLoadFailure('customers', customers.status === 'rejected');

    this.draft.warehouseId = this.warehouseId;
    await this.reload();
  }
  private setLoadFailure(source: string, failed: boolean): void {
    if (failed) this.loadFailures.add(source);
    else this.loadFailures.delete(source);
    this.loadError = this.loadFailures.size
      ? `Unable to load ${Array.from(this.loadFailures).join(', ')}. Other available data is still shown.`
      : '';
  }

  get warehouseName() {
    return this.warehouses.find(w=>w.id===this.warehouseId)?.name||'Not configured';
  }
  get totalStock() {
    return this.materials.reduce((a,m)=>a+(m.quantity||0),0);
  }
  get lowStock() {
    return this.materials.filter(m=>(m.quantity||0)<=(m.reorderLevel||0)).length;
  }
  customerName(id:number) {
    return this.customers.find(c=>c.id===id)?.name||`Customer #${id}`;
  }
  materialName(id:number) {
    return this.materials.find(m=>m.id===id)?.name||`Material #${id}`;
  }
  warehouseLabel(id:number) {
    return this.warehouses.find(w=>w.id===id)?.code||`Warehouse #${id}`;
  }
  async reload() {
    this.draft.warehouseId = this.warehouseId || undefined;
    const results = await Promise.allSettled([
      this.warehouseId ? this.api.materials(this.warehouseId) : Promise.resolve([] as Material[]),
      this.api.history(),
      this.api.transactions()
    ]);
    if (results[0].status === 'fulfilled') this.materials = results[0].value;
    this.setLoadFailure('materials and stock balances', results[0].status === 'rejected');
    if (results[1].status === 'fulfilled') this.dispatches = results[1].value;
    this.setLoadFailure('dispatch history', results[1].status === 'rejected');
    if (results[2].status === 'fulfilled') this.transactions = results[2].value;
    this.setLoadFailure('stock transactions', results[2].status === 'rejected');

    this.setLoadFailure(
      'no warehouse is configured',
      !this.warehouseId && this.warehouses.length === 0 && !this.loadFailures.has('warehouses')
    );
  }
  startDispatch() {
    this.dispatchLines=[ {
      productId:0,warehouseId:this.warehouseId,quantity:1
    }];
    this.mode='dispatch';
  }
  addLine() {
    this.dispatchLines.push( {
      productId:0,warehouseId:this.warehouseId,quantity:1
    });
  }
  removeLine(i:number) {
    this.dispatchLines.splice(i,1);
  }
  async submit() {
    try {
      if(this.mode==='warehouse') {
        const w=await this.api.createWarehouse(this.warehouseDraft.code,this.warehouseDraft.name);
        this.warehouses=[...this.warehouses,w];
        this.warehouseId=w.id;
      }
      if(this.mode==='material')await this.api.createMaterial( {
        ...this.draft,warehouseId:this.warehouseId
      });
      if(this.mode==='stock')await this.api.stockIn(this.selectedId,this.warehouseId,this.quantity,this.reference);
      if(this.mode==='adjust')await this.api.adjustment(this.selectedId,this.warehouseId,this.quantity,this.reference);
      if(this.mode==='dispatch') {
        if(!this.customerId||this.dispatchLines.some(x=>!x.productId||x.quantity<=0))throw new Error('Customer and all dispatch lines are required');
        await this.api.dispatch( {
          customerId:this.customerId,referenceNumber:this.reference,deliveryAddress:this.address,notes:this.notes,items:this.dispatchLines.map(x=>( {
            ...x,warehouseId:this.warehouseId
          }))
        });
      }
      this.mode='';
      this.draft= {
        sku:'',name:'',unit:'PCS',initialQuantity:0,reorderLevel:0,warehouseId:this.warehouseId
      };
      this.warehouseDraft= {
        code:'',name:''
      };
      this.quantity=0;
      this.reference='';
      this.customerId=0;
      this.address='';
      this.notes='';
      await this.reload();
    }
    catch(e:any) {
      alert(e?.error?.message||e?.message||'Request failed')
    }
  }
}
