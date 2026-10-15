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

  private errorMessage(error: unknown, fallback: string): string {
    const candidate = error as { error?: { detail?: string; message?: string }; message?: string };
    return candidate?.error?.detail || candidate?.error?.message || candidate?.message || fallback;
  }
}
