import { Component, OnInit, inject, signal } from '@angular/core';
import { Router } from '@angular/router';
import { NotificationService } from '../../core/services/notification.service';
import { AppNotification } from '../../core/models/notification.models';

/**
 * جرس الإشعارات — مكوّن مشترك يُدرَج في شريط كل صفحة.
 */
@Component({
  selector: 'app-notification-bell',
  imports: [],
  templateUrl: './notification-bell.html',
  styleUrl: './notification-bell.css'
})
export class NotificationBell implements OnInit {

  readonly service = inject(NotificationService);
  private readonly router = inject(Router);

  readonly open = signal(false);

  ngOnInit(): void {
    this.service.refresh();
  }

  toggle(): void {
    const next = !this.open();
    this.open.set(next);
    if (next) this.service.refresh();
  }

  close(): void {
    this.open.set(false);
  }

  markAllRead(): void {
    this.service.markAllRead();
  }

  go(item: AppNotification): void {
    this.close();
    if (item.link) this.router.navigateByUrl(item.link);
  }

  icon(type: string): string {
    switch (type) {
      // للمتدرّب
      case 'EXAM_PUBLISHED':      return '؟';
      case 'SESSION_SCHEDULED':   return '◷';
      case 'ATTENDANCE_RECORDED': return '✓';
      case 'ENROLLED':            return '★';
      // للمدرّب
      case 'EXAM_SUBMITTED':      return '✎';
      case 'TRAINEE_ENROLLED':    return '+';
      case 'TRAINEE_REMOVED':     return '−';
      case 'COURSE_ASSIGNED':     return '◆';
      default:                    return '•';
    }
  }

  /** منذ كم — أوضح من تاريخ كامل في قائمة قصيرة. */
  ago(value: string): string {
    const diff = Date.now() - new Date(value).getTime();
    const mins = Math.floor(diff / 60000);

    if (mins < 1) return 'الآن';
    if (mins < 60) return `قبل ${mins} د`;

    const hours = Math.floor(mins / 60);
    if (hours < 24) return `قبل ${hours} س`;

    const days = Math.floor(hours / 24);
    if (days < 30) return `قبل ${days} ي`;

    const [date] = value.split('T');
    const p = date.split('-');
    return p.length === 3 ? `${p[2]}/${p[1]}` : value;
  }
}
