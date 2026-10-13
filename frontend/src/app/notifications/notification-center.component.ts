import { CommonModule } from '@angular/common';
import { ChangeDetectorRef, Component, OnInit, inject } from '@angular/core';
import { NotificationApi, UserNotification } from './notification-api.service';

@Component({
  selector: 'app-notification-center',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './notification-center.component.html',
  styleUrls: ['./notification-center.component.scss']
})
export class NotificationCenterComponent implements OnInit {
  private readonly api = inject(NotificationApi);
  private readonly cdr = inject(ChangeDetectorRef);

  notifications: UserNotification[] = [];
  isOpen = false;
  loading = false;
  error = '';

  get unreadCount(): number {
    return this.notifications.filter(notification => !notification.readAt).length;
  }

  async ngOnInit(): Promise<void> {
    await this.refresh();
  }

  async toggle(): Promise<void> {
    this.isOpen = !this.isOpen;
    if (this.isOpen) {
      await this.refresh();
    }
  }

  async refresh(): Promise<void> {
    this.loading = true;
    this.error = '';
    this.cdr.markForCheck();
    try {
      this.notifications = await this.api.inbox();
    } catch {
      this.error = 'Notifications could not be loaded. Please try again.';
    } finally {
      this.loading = false;
      this.cdr.markForCheck();
    }
  }

  async markRead(notification: UserNotification): Promise<void> {
    if (notification.readAt) {
      return;
    }
    try {
      const updated = await this.api.markRead(notification.id);
      this.notifications = this.notifications.map(item => item.id === updated.id ? updated : item);
    } catch {
      this.error = 'This notification could not be marked as read.';
    }
    this.cdr.markForCheck();
  }

  async markAllRead(): Promise<void> {
    try {
      await this.api.markAllRead();
      const now = new Date().toISOString();
      this.notifications = this.notifications.map(item => item.readAt ? item : { ...item, readAt: now });
    } catch {
      this.error = 'Notifications could not be marked as read.';
    }
    this.cdr.markForCheck();
  }
}
