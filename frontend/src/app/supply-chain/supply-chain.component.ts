import { ChangeDetectorRef, Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { CreateShipment, Shipment, ShipmentDetail, SupplyChainApi } from '../services/supply-chain-api.service';

@Component({
  selector: 'app-supply-chain',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './supply-chain.component.html',
  styleUrl: './supply-chain.component.scss'
})
export class SupplyChainComponent implements OnInit {
  private readonly api = inject(SupplyChainApi);
  private readonly changeDetector = inject(ChangeDetectorRef);

  shipments: Shipment[] = [];
  selected?: ShipmentDetail;
  loading = false;
  saving = false;
  error = '';
  success = '';
  showCreate = false;
  statusDraft = '';
  locationDraft = '';
  remarksDraft = '';
  inspectionDraft = { lotCode: '', acceptedQuantity: 0, rejectedQuantity: 0, notes: '' };
  returnDraft = { returnReference: '', productId: 0, warehouseId: 0, quantity: 0, disposition: 'QUARANTINE' as 'RESTOCK' | 'QUARANTINE' | 'SCRAP', reason: '' };
  materialLotDraft = { lotCode: '', materialId: 0, supplierId: null as number | null, supplierName: '', supplierLotCode: '', shipmentId: null as number | null, purchaseOrderId: null as number | null, goodsReceiptReference: '', warehouseId: null as number | null, receivedQuantity: 0, acceptedQuantity: 0, rejectedQuantity: 0 };
  productionLotDraft = { lotCode: '', productId: 0, manufacturingOrderId: 0, producedQuantity: 0, rejectedQuantity: 0, warehouseId: null as number | null };
  consumptionDraft = { materialLotId: 0, productionLotId: 0, manufacturingOrderId: 0, quantityConsumed: 0, unit: 'EA', remarks: '' };
  manufacturingOrderDraft = { orderNumber: '', productId: 0, plannedQuantity: 0 };
  lotShipmentDraft = { lotCode: '', shipmentId: 0, quantity: 0 };
  lotShipmentAllocations: unknown[] | null = null;
  genealogyLotCode = '';
  genealogyResult: unknown = null;

  draft: CreateShipment = {
    shipmentNumber: '',
    direction: 'INBOUND',
    referenceNumber: '',
    supplierName: '',
    customerName: '',
    carrierName: '',
    carrierTrackingNumber: '',
    originLocation: '',
    destinationLocation: '',
    estimatedArrivalAt: '',
    notes: ''
  };

  async ngOnInit(): Promise<void> {
    await this.reload();
  }

  async reload(): Promise<void> {
    this.loading = true;
    this.error = '';
    try {
      this.shipments = await this.api.shipments();
      if (this.selected) {
        this.selected = await this.api.detail(this.selected.shipment.id);
      }
    } catch (error) {
      this.error = this.errorMessage(error, 'Unable to load shipments.');
    } finally {
      this.loading = false;
      this.changeDetector.markForCheck();
    }
  }

  async openShipment(shipment: Shipment): Promise<void> {
    this.error = '';
    try {
      this.selected = await this.api.detail(shipment.id);
      this.statusDraft = shipment.status;
    } catch (error) {
      this.error = this.errorMessage(error, 'Unable to load shipment details.');
    }
  }

  async createShipment(): Promise<void> {
    this.saving = true;
    this.error = '';
    this.success = '';
    try {
      const created = await this.api.create({
        ...this.draft,
        shipmentNumber: this.draft.shipmentNumber.trim()
      });
      this.selected = created;
      this.showCreate = false;
      this.draft = {
        shipmentNumber: '',
        direction: 'INBOUND',
        referenceNumber: '',
        supplierName: '',
        customerName: '',
        carrierName: '',
        carrierTrackingNumber: '',
        originLocation: '',
        destinationLocation: '',
        estimatedArrivalAt: '',
        notes: ''
      };
      this.success = 'Shipment created successfully.';
      await this.reload();
    } catch (error) {
      this.error = this.errorMessage(error, 'Unable to create shipment.');
    } finally {
      this.saving = false;
      this.changeDetector.markForCheck();
    }
  }

  async updateStatus(): Promise<void> {
    if (!this.selected) return;
    this.saving = true;
    this.error = '';
    this.success = '';
    try {
      this.selected = await this.api.updateStatus(
        this.selected.shipment.id,
        this.statusDraft,
        this.locationDraft,
        this.remarksDraft
      );
      this.success = 'Shipment status updated.';
      this.locationDraft = '';
      this.remarksDraft = '';
      await this.reload();
    } catch (error) {
      this.error = this.errorMessage(error, 'Unable to update shipment status.');
    } finally {
      this.saving = false;
      this.changeDetector.markForCheck();
    }
  }

  async receiveMaterialLot(): Promise<void> {
    this.saving = true;
    this.error = '';
    this.success = '';
    try {
      await this.api.receiveMaterialLot({
        ...this.materialLotDraft,
        lotCode: this.materialLotDraft.lotCode.trim(),
        materialId: Number(this.materialLotDraft.materialId),
        receivedQuantity: Number(this.materialLotDraft.receivedQuantity),
        acceptedQuantity: Number(this.materialLotDraft.acceptedQuantity),
        rejectedQuantity: Number(this.materialLotDraft.rejectedQuantity)
      });
      this.success = 'Raw-material lot recorded and linked to the validated goods receipt.';
      this.materialLotDraft = { lotCode: '', materialId: 0, supplierId: null, supplierName: '', supplierLotCode: '', shipmentId: null, purchaseOrderId: null, goodsReceiptReference: '', warehouseId: null, receivedQuantity: 0, acceptedQuantity: 0, rejectedQuantity: 0 };
    } catch (error) {
      this.error = this.errorMessage(error, 'Unable to receive raw-material lot.');
    } finally {
      this.saving = false;
      this.changeDetector.markForCheck();
    }
  }

  async createManufacturingOrder(): Promise<void> {
    this.saving = true;
    this.error = '';
    this.success = '';
    try {
      await this.api.createManufacturingOrder({
        ...this.manufacturingOrderDraft,
        orderNumber: this.manufacturingOrderDraft.orderNumber.trim(),
        productId: Number(this.manufacturingOrderDraft.productId),
        plannedQuantity: Number(this.manufacturingOrderDraft.plannedQuantity)
      });
      this.success = 'Manufacturing order created.';
      this.manufacturingOrderDraft = { orderNumber: '', productId: 0, plannedQuantity: 0 };
    } catch (error) {
      this.error = this.errorMessage(error, 'Unable to create manufacturing order.');
    } finally {
      this.saving = false;
      this.changeDetector.markForCheck();
    }
  }

  async recordProductionLot(): Promise<void> {
    this.saving = true;
    this.error = '';
    this.success = '';
    try {
      await this.api.recordProductionLot({
        ...this.productionLotDraft,
        lotCode: this.productionLotDraft.lotCode.trim(),
        productId: Number(this.productionLotDraft.productId),
        manufacturingOrderId: Number(this.productionLotDraft.manufacturingOrderId),
        producedQuantity: Number(this.productionLotDraft.producedQuantity),
        rejectedQuantity: Number(this.productionLotDraft.rejectedQuantity)
      });
      this.success = 'Finished-product lot recorded against the manufacturing order.';
      this.productionLotDraft = { lotCode: '', productId: 0, manufacturingOrderId: 0, producedQuantity: 0, rejectedQuantity: 0, warehouseId: null };
    } catch (error) {
      this.error = this.errorMessage(error, 'Unable to record finished-product lot.');
    } finally {
      this.saving = false;
      this.changeDetector.markForCheck();
    }
  }

  async recordConsumption(): Promise<void> {
    this.saving = true;
    this.error = '';
    this.success = '';
    try {
      await this.api.recordConsumption({
        ...this.consumptionDraft,
        quantityConsumed: Number(this.consumptionDraft.quantityConsumed),
        unit: this.consumptionDraft.unit.trim(),
        remarks: this.consumptionDraft.remarks || undefined
      });
      this.success = 'Material consumption linked to the finished-product lot.';
      this.consumptionDraft = { materialLotId: 0, productionLotId: 0, manufacturingOrderId: 0, quantityConsumed: 0, unit: 'EA', remarks: '' };
    } catch (error) {
      this.error = this.errorMessage(error, 'Unable to record material consumption.');
    } finally {
      this.saving = false;
      this.changeDetector.markForCheck();
    }
  }

  async allocateProductionLot(): Promise<void> {
    this.saving = true;
    this.error = '';
    this.success = '';
    try {
      const lotCode = this.lotShipmentDraft.lotCode.trim();
      await this.api.allocateProductionLot(lotCode, {
        shipmentId: Number(this.lotShipmentDraft.shipmentId),
        quantity: Number(this.lotShipmentDraft.quantity)
      });
      this.lotShipmentAllocations = await this.api.lotShipmentAllocations(lotCode);
      this.success = 'Finished-product lot quantity allocated to the outbound shipment.';
      this.lotShipmentDraft = { lotCode: '', shipmentId: 0, quantity: 0 };
    } catch (error) {
      this.error = this.errorMessage(error, 'Unable to allocate finished-product lot to shipment.');
    } finally {
      this.saving = false;
      this.changeDetector.markForCheck();
    }
  }

  async loadLotShipmentAllocations(): Promise<void> {
    this.error = '';
    this.lotShipmentAllocations = null;
    try {
      this.lotShipmentAllocations = await this.api.lotShipmentAllocations(this.lotShipmentDraft.lotCode.trim());
    } catch (error) {
      this.error = this.errorMessage(error, 'Unable to load finished-product lot shipment allocations.');
    }
  }

  async lookupGenealogy(): Promise<void> {
    this.error = '';
    this.success = '';
    this.genealogyResult = null;
    try {
      this.genealogyResult = await this.api.genealogy(this.genealogyLotCode.trim());
    } catch (error) {
      this.error = this.errorMessage(error, 'Unable to load lot genealogy.');
    }
  }

  async refreshCarrierTracking(): Promise<void> {
    if (!this.selected) return;
    this.saving = true;
    this.error = '';
    this.success = '';
    try {
      this.selected = await this.api.refreshCarrierTracking(this.selected.shipment.id);
      this.success = 'Live carrier tracking refreshed.';
      await this.reload();
    } catch (error) {
      this.error = this.errorMessage(error, 'Unable to refresh live carrier tracking.');
    } finally {
      this.saving = false;
      this.changeDetector.markForCheck();
    }
  }

  async inspectLot(): Promise<void> {
    this.saving = true;
    this.error = '';
    this.success = '';
    try {
      await this.api.inspectLot(this.inspectionDraft.lotCode.trim(), {
        acceptedQuantity: Number(this.inspectionDraft.acceptedQuantity),
        rejectedQuantity: Number(this.inspectionDraft.rejectedQuantity),
        notes: this.inspectionDraft.notes || undefined
      });
      this.success = 'Quality inspection recorded and accepted quantity reconciled to inventory.';
      this.inspectionDraft = { lotCode: '', acceptedQuantity: 0, rejectedQuantity: 0, notes: '' };
    } catch (error) {
      this.error = this.errorMessage(error, 'Unable to record quality inspection.');
    } finally {
      this.saving = false;
      this.changeDetector.markForCheck();
    }
  }

  async recordReturn(): Promise<void> {
    if (!this.selected) return;
    this.saving = true;
    this.error = '';
    this.success = '';
    try {
      await this.api.recordReturn(this.selected.shipment.id, {
        ...this.returnDraft,
        returnReference: this.returnDraft.returnReference.trim(),
        reason: this.returnDraft.reason.trim(),
        productId: Number(this.returnDraft.productId),
        warehouseId: Number(this.returnDraft.warehouseId),
        quantity: Number(this.returnDraft.quantity)
      });
      this.success = 'Shipment return recorded.';
      this.returnDraft = { returnReference: '', productId: 0, warehouseId: 0, quantity: 0, disposition: 'QUARANTINE', reason: '' };
    } catch (error) {
      this.error = this.errorMessage(error, 'Unable to record shipment return.');
    } finally {
      this.saving = false;
      this.changeDetector.markForCheck();
    }
  }

  private errorMessage(error: unknown, fallback: string): string {
    const candidate = error as { error?: { detail?: string; message?: string }; message?: string };
    return candidate?.error?.detail || candidate?.error?.message || candidate?.message || fallback;
  }
}
