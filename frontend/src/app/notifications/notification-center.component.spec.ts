import { ComponentFixture, TestBed } from '@angular/core/testing';
import { beforeEach, describe, expect, it, vi } from 'vitest';
import { NotificationApi, UserNotification } from './notification-api.service';
import { NotificationCenterComponent } from './notification-center.component';

describe('NotificationCenterComponent', () => {
  let fixture: ComponentFixture<NotificationCenterComponent>;
  let component: NotificationCenterComponent;

  const unread: UserNotification = {
    id: 3,
    title: 'Welcome',
    message: 'Your account is ready.',
    createdAt: '2026-10-13T10:00:00Z',
    readAt: null
  };

  const api = {
    inbox: vi.fn(async () => [unread]),
    markRead: vi.fn(async () => ({ ...unread, readAt: '2026-10-13T11:00:00Z' })),
    markAllRead: vi.fn(async () => ({ updatedCount: 1 }))
  };

  beforeEach(async () => {
    vi.clearAllMocks();
    api.inbox.mockResolvedValue([unread]);
    await TestBed.configureTestingModule({
      imports: [NotificationCenterComponent],
      providers: [{ provide: NotificationApi, useValue: api }]
    }).compileComponents();

    fixture = TestBed.createComponent(NotificationCenterComponent);
    component = fixture.componentInstance;
  });

  it('counts unread notifications', async () => {
    await component.ngOnInit();
    expect(component.unreadCount).toBe(1);
  });

  it('refreshes the inbox when opened', async () => {
    await component.toggle();
    expect(component.isOpen).toBe(true);
    expect(api.inbox).toHaveBeenCalled();
  });

  it('marks a notification as read', async () => {
    component.notifications = [unread];
    await component.markRead(unread);
    expect(component.unreadCount).toBe(0);
  });
});
