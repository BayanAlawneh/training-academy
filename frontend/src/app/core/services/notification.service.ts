import { HttpClient } from '@angular/common/http';
import { Injectable, inject, signal } from '@angular/core';
import { environment } from '../../../environments/environment';
import { ApiResponse } from '../models/auth.models';
import { AppNotification } from '../models/notification.models';

/**
 * حالة الإشعارات مشتركة على مستوى التطبيق، فالجرس في أي صفحة يقرأ نفس
 * الإشارات ولا تُعاد المزامنة عند كل تنقّل.
 */
@Injectable({ providedIn: 'root' })
export class NotificationService {

  private readonly http = inject(HttpClient);
  private readonly baseUrl = `${environment.apiUrl}/notifications`;

  readonly items = signal<AppNotification[]>([]);
  readonly unread = signal(0);
  readonly loading = signal(false);

  refresh(): void {
    this.loading.set(true);

    this.http.get<ApiResponse<AppNotification[]>>(this.baseUrl).subscribe({
      next: (r) => {
        this.items.set(r.data ?? []);
        this.unread.set((r.data ?? []).filter(n => !n.read).length);
        this.loading.set(false);
      },
      error: () => this.loading.set(false)
    });
  }

  markAllRead(): void {
    // تحديث متفائل: الواجهة تستجيب فوراً، والخادم يلحق.
    this.items.set(this.items().map(n => ({ ...n, read: true })));
    this.unread.set(0);

    this.http.put<ApiResponse<null>>(`${this.baseUrl}/read-all`, {}).subscribe({
      error: () => this.refresh()
    });
  }
}
