import { CommonModule } from '@angular/common';
import { ChangeDetectorRef, Component, OnInit, inject } from '@angular/core';
import { AuditApi, AuditRecord } from './audit-api.service';

@Component({
  selector: 'app-audit',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './audit.component.html',
  styleUrls: ['./audit.component.scss']
})
export class AuditComponent implements OnInit {
  private readonly api = inject(AuditApi);
  private readonly changeDetector = inject(ChangeDetectorRef);

  records: AuditRecord[] = [];
  loading = false;
  loadError = '';

  async ngOnInit(): Promise<void> {
    await this.reload();
  }

  async reload(): Promise<void> {
    this.loading = true;
    this.loadError = '';
    this.changeDetector.markForCheck();

    try {
      this.records = await this.api.records();
    } catch {
      this.loadError = 'Audit history could not be loaded. Check your access and try again.';
    } finally {
      this.loading = false;
      // The project uses async promise-based APIs; explicitly schedule the view refresh.
      this.changeDetector.markForCheck();
    }
  }

  statusClass(statusCode: number): string {
    if (statusCode >= 500) {
      return 'status-error';
    }
    if (statusCode >= 400) {
      return 'status-warning';
    }
    return 'status-success';
  }
}
