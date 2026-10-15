import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { firstValueFrom } from 'rxjs';
import { AuthService } from './auth.service';

export interface ShipmentEvent {
  id: number;
  previousStatus?: string;
  newStatus: string;
  eventType: string;
  eventTime: string;
  recordedAt: string;
  location?: string;
  remarks?: string;
  sourceType: string;
  actor?: string;
}

export interface Shipment {
  id: number;
  shipmentNumber: string;
  direction: 'INBOUND' | 'OUTBOUND' | 'TRANSFER';
  status: string;
  referenceNumber?: string;
  purchaseOrderId?: number | null;
  goodsReceiptReference?: string;
  salesOrderId?: number | null;
  dispatchId?: number | null;
  supplierName?: string;
  customerName?: string;
  carrierName?: string;
  carrierTrackingNumber?: string;
  originLocation?: string;
  destinationLocation?: string;
  dispatchedAt?: string;
  estimatedArrivalAt?: string;
  deliveredAt?: string;
  notes?: string;
  trackingPath: string;
  createdAt: string;
}

export interface ShipmentDetail {
  shipment: Shipment;
  events: ShipmentEvent[];
}

export interface CreateShipment {
  shipmentNumber: string;
  direction: Shipment['direction'];
  referenceNumber?: string;
  purchaseOrderId?: number | null;
  goodsReceiptReference?: string;
  salesOrderId?: number | null;
  dispatchId?: number | null;
  supplierName?: string;
  customerName?: string;
  carrierName?: string;
  carrierTrackingNumber?: string;
  originLocation?: string;
  destinationLocation?: string;
  estimatedArrivalAt?: string;
  notes?: string;
}

@Injectable({ providedIn: 'root' })
export class SupplyChainApi {
  private readonly http = inject(HttpClient);
  private readonly auth = inject(AuthService);
  private readonly base = '/api/v1/supply-chain';

  private async options() {
    return {
      headers: new HttpHeaders({
        Authorization: `Bearer ${await this.auth.token()}`,
        'X-Tenant-ID': '1'
      })
    };
  }

  async shipments(): Promise<Shipment[]> {
    return firstValueFrom(this.http.get<Shipment[]>(`${this.base}/shipments`, await this.options()));
  }

  async detail(id: number): Promise<ShipmentDetail> {
    return firstValueFrom(this.http.get<ShipmentDetail>(`${this.base}/shipments/${id}`, await this.options()));
  }

  async create(request: CreateShipment): Promise<ShipmentDetail> {
    return firstValueFrom(this.http.post<ShipmentDetail>(`${this.base}/shipments`, request, await this.options()));
  }

  async updateStatus(id: number, status: string, location: string, remarks: string): Promise<ShipmentDetail> {
    return firstValueFrom(this.http.post<ShipmentDetail>(`${this.base}/shipments/${id}/events`, {
      status,
      location: location || undefined,
      remarks: remarks || undefined,
      idempotencyKey: crypto.randomUUID()
    }, await this.options()));
  }

  async receiveMaterialLot(request: MaterialLotRequest): Promise<unknown> {
    return firstValueFrom(this.http.post(`${this.base}/material-lots`, request, await this.options()));
  }

  async recordProductionLot(request: ProductionLotRequest): Promise<unknown> {
    return firstValueFrom(this.http.post(`${this.base}/production-lots`, request, await this.options()));
  }

  async recordConsumption(request: LotConsumptionRequest): Promise<unknown> {
    return firstValueFrom(this.http.post(`${this.base}/lot-consumptions`, request, await this.options()));
  }

  async allocateProductionLot(lotCode: string, request: ProductionLotShipmentRequest): Promise<unknown> {
    return firstValueFrom(this.http.post(`${this.base}/production-lots/${encodeURIComponent(lotCode)}/shipments`, request, await this.options()));
  }

  async genealogy(lotCode: string): Promise<unknown> {
    return firstValueFrom(this.http.get(`${this.base}/lots/${encodeURIComponent(lotCode)}/genealogy`, await this.options()));
  }

  async createManufacturingOrder(request: ManufacturingOrderRequest): Promise<unknown> {
    return firstValueFrom(this.http.post(`${this.base}/manufacturing-orders`, request, await this.options()));
  }

  async refreshCarrierTracking(id: number): Promise<ShipmentDetail> {
    return firstValueFrom(this.http.post<ShipmentDetail>(`${this.base}/shipments/${id}/carrier-refresh`, {}, await this.options()));
  }

  async inspectLot(lotCode: string, request: QualityInspectionRequest): Promise<unknown> {
    return firstValueFrom(this.http.post(`${this.base}/lots/${encodeURIComponent(lotCode)}/inspection`, request, await this.options()));
  }

  async recordReturn(shipmentId: number, request: ShipmentReturnRequest): Promise<unknown> {
    return firstValueFrom(this.http.post(`${this.base}/shipments/${shipmentId}/returns`, request, await this.options()));
  }
}

export interface QualityInspectionRequest {
  acceptedQuantity: number;
  rejectedQuantity: number;
  notes?: string;
}

export interface ShipmentReturnRequest {
  returnReference: string;
  productId: number;
  warehouseId: number;
  quantity: number;
  disposition: 'RESTOCK' | 'QUARANTINE' | 'SCRAP';
  reason: string;
}

export interface MaterialLotRequest {
  lotCode: string;
  materialId: number;
  supplierId?: number | null;
  supplierName?: string;
  supplierLotCode?: string;
  shipmentId?: number | null;
  purchaseOrderId?: number | null;
  goodsReceiptReference?: string;
  warehouseId?: number | null;
  receivedQuantity: number;
  acceptedQuantity?: number;
  rejectedQuantity?: number;
  manufacturedAt?: string;
  expiresAt?: string;
}

export interface ProductionLotRequest {
  lotCode: string;
  productId: number;
  manufacturingOrderId: number;
  producedQuantity: number;
  rejectedQuantity?: number;
  warehouseId?: number | null;
  producedAt?: string;
}

export interface LotConsumptionRequest {
  materialLotId: number;
  productionLotId: number;
  manufacturingOrderId: number;
  quantityConsumed: number;
  unit: string;
  remarks?: string;
}

export interface ManufacturingOrderRequest {
  orderNumber: string;
  productId: number;
  plannedQuantity: number;
}

export interface ProductionLotShipmentRequest {
  shipmentId: number;
  quantity: number;
}
