import { CommonModule } from '@angular/common';
import { ChangeDetectorRef, Component, Input, OnInit, inject } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { HrCalendarApi, HrCalendarEntry, HrCalendarType } from './hr-calendar-api.service';

@Component({
  selector: 'app-hr-calendar',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './hr-calendar.component.html',
  styleUrls: ['../hr.component.scss', './hr-calendar.component.scss']
})
export class HrCalendarComponent implements OnInit {
  @Input() section: 'holidays' | 'meetings' = 'holidays';

  private readonly api = inject(HrCalendarApi);
  private readonly cdr = inject(ChangeDetectorRef);

  entries: HrCalendarEntry[] = [];
  loading = false;
  error = '';
  editingId: number | undefined;
  formOpen = false;
  draft: HrCalendarEntry = this.emptyDraft();

  get type(): HrCalendarType {
    return this.section === 'holidays' ? 'HOLIDAY' : 'MEETING';
  }

  get heading(): string {
    return this.section === 'holidays' ? 'Holiday calendar' : 'Organization meetings';
  }

  get description(): string {
    return this.section === 'holidays'
      ? 'Maintain upcoming organization holidays and closure dates.'
      : 'Schedule organization meetings and share their purpose with employees.';
  }

  async ngOnInit(): Promise<void> {
    await this.reload();
  }

  async reload(): Promise<void> {
    this.loading = true;
    this.error = '';
    try {
      this.entries = await this.api.list(this.type);
    } catch {
      this.error = `${this.heading} could not be loaded. Check your access and try again.`;
    } finally {
      this.loading = false;
      this.cdr.markForCheck();
    }
  }

  startCreate(): void {
    this.editingId = undefined;
    this.draft = this.emptyDraft();
    this.formOpen = true;
    this.error = '';
  }

  edit(entry: HrCalendarEntry): void {
    this.editingId = entry.id;
    this.draft = { ...entry };
    this.formOpen = true;
    this.error = '';
  }

  cancel(): void {
    this.formOpen = false;
    this.editingId = undefined;
    this.draft = this.emptyDraft();
  }

  async save(): Promise<void> {
    this.error = '';
    if (!this.draft.title.trim()) {
      this.error = 'Enter a title.';
      return;
    }
    if (!this.draft.startsOn || !this.draft.endsOn || this.draft.endsOn < this.draft.startsOn) {
      this.error = 'Choose a valid date range.';
      return;
    }
    try {
      const entry = { ...this.draft, type: this.type };
      if (this.editingId) await this.api.update({ ...entry, id: this.editingId });
      else await this.api.create(entry);
      this.cancel();
      await this.reload();
    } catch {
      this.error = `${this.heading} entry could not be saved.`;
      this.cdr.markForCheck();
    }
  }

  async toggleStatus(entry: HrCalendarEntry): Promise<void> {
    try {
      await this.api.update({
        ...entry,
        status: entry.status === 'ACTIVE' ? 'CANCELLED' : 'ACTIVE'
      });
      await this.reload();
    } catch {
      this.error = 'The entry status could not be updated.';
      this.cdr.markForCheck();
    }
  }

  private emptyDraft(): HrCalendarEntry {
    return {
      type: this.type,
      title: '',
      description: '',
      startsOn: '',
      endsOn: '',
      status: 'ACTIVE'
    };
  }
}
