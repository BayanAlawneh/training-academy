import { Component, OnInit, inject, signal, computed } from '@angular/core';
import { Router, RouterLink } from '@angular/router';
import { AuthService } from '../../core/services/auth.service';
import { PortalService } from '../../core/services/portal.service';
import { ATTENDANCE_LABEL, AttendanceStatus, MyAttendance } from '../../core/models/portal.models';

@Component({
  selector: 'app-my-attendance',
  imports: [RouterLink],
  templateUrl: './my-attendance.html',
  styleUrl: './my-attendance.css'
})
export class MyAttendancePage implements OnInit {

  readonly auth = inject(AuthService);
  private readonly portal = inject(PortalService);
  private readonly router = inject(Router);

  readonly rows = signal<MyAttendance[]>([]);
  readonly loading = signal(false);
  readonly errorMessage = signal<string | null>(null);

  readonly present = computed(() => this.rows().filter(r => r.attendanceStatus === 'PRESENT').length);
  readonly absent  = computed(() => this.rows().filter(r => r.attendanceStatus === 'ABSENT').length);
  readonly late    = computed(() => this.rows().filter(r => r.attendanceStatus === 'LATE').length);

  /** نسبة الحضور تُحتسب من الجلسات المسجَّلة فقط — الجلسات القادمة لا تُحسب غياباً. */
  readonly recorded = computed(() => this.rows().filter(r => r.attendanceStatus !== 'NOT_RECORDED').length);

  readonly rate = computed(() => {
    const total = this.recorded();
    if (total === 0) return null;
    return Math.round(((this.present() + this.late()) / total) * 100);
  });

  readonly isEmpty = computed(() => !this.loading() && this.rows().length === 0);

  ngOnInit(): void {
    this.load();
  }

  load(): void {
    this.loading.set(true);
    this.errorMessage.set(null);

    this.portal.myAttendance().subscribe({
      next: (response) => {
        this.rows.set(response.data);
        this.loading.set(false);
      },
      error: (err) => {
        this.errorMessage.set(err?.error?.message ?? 'تعذّر تحميل سجلّ الحضور');
        this.loading.set(false);
      }
    });
  }

  label(status: AttendanceStatus): string {
    return ATTENDANCE_LABEL[status] ?? status;
  }

  badgeClass(status: AttendanceStatus): string {
    switch (status) {
      case 'PRESENT': return 'b-present';
      case 'ABSENT':  return 'b-absent';
      case 'LATE':    return 'b-late';
      case 'EXCUSED': return 'b-excused';
      default:        return 'b-none';
    }
  }

  formatDate(value: string): string {
    if (!value) return '—';
    const p = value.split('-');
    return p.length === 3 ? `${p[2]} / ${p[1]} / ${p[0]}` : value;
  }

  /** 10:00:00 → 10:00 */
  formatTime(value: string): string {
    if (!value) return '';
    return value.substring(0, 5);
  }

  isUpcoming(row: MyAttendance): boolean {
    return row.attendanceStatus === 'NOT_RECORDED';
  }

  logout(): void {
    this.auth.logout();
    this.router.navigateByUrl('/login');
  }
}
