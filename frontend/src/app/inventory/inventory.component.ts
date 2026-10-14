import  {
  ChangeDetectorRef, Component, inject, OnInit
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
import { getDialogErrorMessage } from '../shared/dialog-error.util';
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
  selector:'app-inventory',standalone:true,imports:[CommonModule,FormsModule],templateUrl:'./inventory.component.html',styles: [`
    .sub {
      margin-top: 28px;
    }

    .inventory-cards {
      margin-bottom: 10px;
    }

    .muted {
      font-size: 12px;
      color: #667085;
    }

    .stock-toolbar {
      align-items: center;
      gap: 16px;
    }

    .stock-toolbar-heading {
      min-width: 0;
    }

    .warehouse-actions {
      display: flex;
      align-items: center;
      justify-content: flex-end;
      gap: 12px;
      min-width: 0;
      flex: 0 1 auto;
    }

    .warehouse {
      width: auto;
      min-width: 220px;
      max-width: min(32vw, 360px);
      padding: 9px 12px;
      border: 1px solid #d0d5dd;
      border-radius: 8px;
      color: #344054;
      background: #ffffff;
    }

    .stock-in-button {
      flex: 0 0 auto;
      white-space: nowrap;
    }

    textarea {
      width: 100%;
      min-height: 80px;
      padding: 11px;
      font: inherit;
    }

    .line {
      display: grid;
      grid-template-columns: 1fr 130px auto;
      gap: 8px;
      margin-bottom: 8px;
    }

    .negative {
      font-weight: 700;
    }

    @media (max-width: 680px) {
      .stock-toolbar {
        align-items: stretch;
      }

      .warehouse-actions {
        width: 100%;
        justify-content: flex-start;
        flex-wrap: wrap;
      }

      .warehouse {
        flex: 1 1 220px;
        min-width: 0;
        max-width: 100%;
      }
    }
  `]
})
export class InventoryComponent implements OnInit  {
  api=inject(InventoryApi);
  crm=inject(CrmApi);
  private readonly changeDetector = inject(ChangeDetectorRef);
  materials:Material[]=[];
  warehouses:Warehouse[]=[];
  customers:Customer[]=[];
  dispatches:any[]=[];
  transactions:InventoryTransaction[]=[];
  loadError='';
  dialogError = '';
  submitting = false;
  private readonly loadFailures = new Set<string>();
  mode='';
  selectedId=0;
  warehouseId=0;
  warehousesLoaded = false;
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
  async ngOnInit(): Promise<void> {
    // Load independent resources separately so slow CRM/customer data cannot
    // delay the warehouse selector or the first stock refresh.
    const warehouseLoad = this.api.warehouses()
      .then(async warehouses => {
        this.warehouses = warehouses;
        this.warehouseId = this.warehouses.find(w => w.status === 'ACTIVE')?.id
          || this.warehouses[0]?.id
          || 0;
        this.warehousesLoaded = true;
        this.setLoadFailure('warehouses', false);
        this.draft.warehouseId = this.warehouseId;

        // Start stock requests as soon as the selected warehouse is known.
        await this.reload();
      })
      .catch(() => {
        this.warehousesLoaded = true;
        this.setLoadFailure('warehouses', true);
      });

    const customerLoad = this.crm.customers('')
      .then(customers => {
        this.customers = customers;
        this.setLoadFailure('customers', false);
      })
      .catch(() => this.setLoadFailure('customers', true));

    await Promise.all([warehouseLoad, customerLoad]);
  }
  private setLoadFailure(source: string, failed: boolean): void {
    if (failed) this.loadFailures.add(source);
    else this.loadFailures.delete(source);
    this.loadError = this.loadFailures.size
      ? `Unable to load ${Array.from(this.loadFailures).join(', ')}. Other available data is still shown.`
      : '';

    // Angular's zoneless change detection does not automatically refresh the
    // view for ordinary property changes made in Promise callbacks.
    this.changeDetector.markForCheck();
  }

  get warehouseName() {
    const selectedWarehouse = this.warehouses.find(w => w.id === this.warehouseId);
    return selectedWarehouse
      ? `${selectedWarehouse.code} — ${selectedWarehouse.name}`
      : this.warehousesLoaded
        ? 'No warehouse selected'
        : 'Loading warehouse…';
  }

  get warehousePlaceholder(): string {
    if (!this.warehousesLoaded) return 'Loading warehouses…';
    if (this.loadFailures.has('warehouses')) return 'Unable to load warehouses';
    return 'No warehouses available — add a warehouse';
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
    if (this.submitting) return;
    this.dialogError = '';
    this.submitting = true;
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
    catch(e: unknown) {
      this.dialogError = getDialogErrorMessage(e, 'The action could not be completed. Please review the details and try again.');
    } finally {
      this.submitting = false;
    }
  }
}
