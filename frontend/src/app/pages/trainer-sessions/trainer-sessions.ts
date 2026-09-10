import { Component, OnInit, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { NotificationBell } from '../../shared/notification-bell/notification-bell';
import { AuthService } from '../../core/services/auth.service';
import { TrainerPortalService } from '../../core/services/trainer-portal.service';
import { ModalService } from '../../core/services/modal.service';
import {
  ATTENDANCE_LABEL, AttendanceRow, AttendanceStatus,
  TrainerSummary, TrainingSession, WEEKDAYS
} from '../../core/models/portal.models';

@Component({
  selector: 'app-trainer-sessions',
  imports: [ReactiveFormsModule, RouterLink, NotificationBell],
  templateUrl: './trainer-sessions.html',
  styleUrl: './trainer-sessions.css'
})
export class TrainerSessions implements OnInit {

  readonly auth = inject(AuthService);
  private readonly api = inject(TrainerPortalService);
  private readonly fb = inject(FormBuilder);
  private readonly modal = inject(ModalService);
  private readonly router = inject(Router);

  readonly weekdays = WEEKDAYS;

  readonly summary = signal<TrainerSummary | null>(null);
  readonly sessions = signal<TrainingSession[]>([]);
  readonly loading = signal(false);
  readonly errorMessage = signal<string | null>(null);
  readonly okMessage = signal<string | null>(null);

  readonly showSingle = signal(false);
  readonly showSchedule = signal(false);
  readonly editingId = signal<number | null>(null);

  /** الجلسة المفتوحة لتسجيل الحضور، وقائمة متدربيها. */
  readonly rosterFor = signal<TrainingSession | null>(null);
  readonly roster = signal<AttendanceRow[]>([]);
  readonly rosterLoading = signal(false);

  readonly sessionForm = this.fb.group({
    title: ['', [Validators.required, Validators.maxLength(150)]],
    sessionDate: ['', Validators.required],
    startTime: ['10:00', Validators.required],
    endTime: ['12:00', Validators.required],
    meetingLink: ['']
  });

  readonly scheduleForm = this.fb.group({
    startDate: ['', Validators.required],
    weeks: [4, [Validators.required, Validators.min(1), Validators.max(26)]],
    startTime: ['10:00', Validators.required],
    endTime: ['12:00', Validators.required],
    titlePrefix: ['لقاء']
  });

  readonly selectedDays = signal<number[]>([7, 2]);

  ngOnInit(): void {
    this.loadAll();
  }

  loadAll(): void {
    this.loading.set(true);
    this.errorMessage.set(null);

    this.api.summary().subscribe({
      next: (r) => this.summary.set(r.data),
      error: (err) => this.errorMessage.set(err?.error?.message ?? 'تعذّر تحميل بياناتك')
    });

    this.api.sessions().subscribe({
      next: (r) => { this.sessions.set(r.data); this.loading.set(false); },
      error: (err) => {
        this.errorMessage.set(err?.error?.message ?? 'تعذّر تحميل الجلسات');
        this.loading.set(false);
      }
    });
  }

  toggleDay(value: number): void {
    const current = this.selectedDays();
    this.selectedDays.set(
      current.includes(value) ? current.filter(d => d !== value) : [...current, value]
    );
  }

  isDaySelected(value: number): boolean {
    return this.selectedDays().includes(value);
  }

  openSingle(): void {
    this.editingId.set(null);
    this.sessionForm.reset({ title: '', sessionDate: '', startTime: '10:00', endTime: '12:00', meetingLink: '' });
    this.showSchedule.set(false);
    this.showSingle.set(true);
  }

  openSchedule(): void {
    this.showSingle.set(false);
    this.showSchedule.set(true);
  }

  edit(session: TrainingSession): void {
    this.editingId.set(session.id);
    this.sessionForm.patchValue({
      title: session.title,
      sessionDate: session.sessionDate,
      startTime: session.startTime?.substring(0, 5),
      endTime: session.endTime?.substring(0, 5),
      meetingLink: session.meetingLink ?? ''
    });
    this.showSchedule.set(false);
    this.showSingle.set(true);
  }

  cancelForms(): void {
    this.showSingle.set(false);
    this.showSchedule.set(false);
    this.editingId.set(null);
  }

  saveSession(): void {
    if (this.sessionForm.invalid) { this.sessionForm.markAllAsTouched(); return; }
    this.clearMessages();

    const raw = this.sessionForm.getRawValue();
    const payload = {
      title: raw.title!,
      sessionDate: raw.sessionDate!,
      startTime: this.withSeconds(raw.startTime!),
      endTime: this.withSeconds(raw.endTime!),
      meetingLink: raw.meetingLink?.trim() ? raw.meetingLink.trim() : null
    };

    const id = this.editingId();
    const request$ = id ? this.api.updateSession(id, payload) : this.api.createSession(payload);

    request$.subscribe({
      next: () => {
        this.okMessage.set(id ? 'تم تحديث الجلسة' : 'تمت إضافة الجلسة');
        this.cancelForms();
        this.loadAll();
      },
      error: (err) => this.errorMessage.set(err?.error?.message ?? 'تعذّر الحفظ')
    });
  }

  generate(): void {
    if (this.scheduleForm.invalid) { this.scheduleForm.markAllAsTouched(); return; }
    if (this.selectedDays().length === 0) {
      this.errorMessage.set('اختاري يوماً واحداً على الأقل');
      return;
    }
    this.clearMessages();

    const raw = this.scheduleForm.getRawValue();
    this.api.generateSchedule({
      startDate: raw.startDate!,
      weeks: Number(raw.weeks),
      weekdays: this.selectedDays(),
      startTime: this.withSeconds(raw.startTime!),
      endTime: this.withSeconds(raw.endTime!),
      titlePrefix: raw.titlePrefix?.trim() || null
    }).subscribe({
      next: (r) => {
        this.okMessage.set(`تم توليد ${r.data.length} جلسة`);
        this.cancelForms();
        this.loadAll();
      },
      error: (err) => this.errorMessage.set(err?.error?.message ?? 'تعذّر توليد الجدول')
    });
  }

  async remove(session: TrainingSession): Promise<void> {
    const ok = await this.modal.confirm(
      'حذف الجلسة',
      `حذف "${session.title}"؟ سيُحذف معها سجلّ الحضور المرتبط بها.`,
      { danger: true, confirmText: 'حذف' }
    );
    if (!ok) return;

    this.clearMessages();
    this.api.deleteSession(session.id).subscribe({
      next: () => { this.okMessage.set('تم حذف الجلسة'); this.loadAll(); },
      error: (err) => this.errorMessage.set(err?.error?.message ?? 'تعذّر الحذف')
    });
  }

  openRoster(session: TrainingSession): void {
    this.clearMessages();
    this.rosterFor.set(session);
    this.rosterLoading.set(true);

    this.api.roster(session.id).subscribe({
      next: (r) => { this.roster.set(r.data); this.rosterLoading.set(false); },
      error: (err) => {
        this.errorMessage.set(err?.error?.message ?? 'تعذّر تحميل قائمة المتدربين');
        this.rosterLoading.set(false);
        this.rosterFor.set(null);
      }
    });
  }

  closeRoster(): void {
    this.rosterFor.set(null);
    this.roster.set([]);
  }

  setStatus(traineeId: number, status: AttendanceStatus): void {
    this.roster.set(this.roster().map(r =>
      r.traineeId === traineeId ? { ...r, status } : r
    ));
  }

  markAll(status: AttendanceStatus): void {
    this.roster.set(this.roster().map(r => ({ ...r, status })));
  }

  saveRoster(): void {
    const session = this.rosterFor();
    if (!session) return;
    this.clearMessages();

    this.api.markAttendance(session.id, {
      entries: this.roster().map(r => ({
        traineeId: r.traineeId,
        status: r.status,
        note: r.note
      }))
    }).subscribe({
      next: () => {
        this.okMessage.set('تم حفظ الحضور');
        this.closeRoster();
        this.loadAll();
      },
      error: (err) => this.errorMessage.set(err?.error?.message ?? 'تعذّر حفظ الحضور')
    });
  }

  label(status: AttendanceStatus): string {
    return ATTENDANCE_LABEL[status] ?? status;
  }

  formatDate(value: string): string {
    if (!value) return '—';
    const p = value.split('-');
    return p.length === 3 ? `${p[2]} / ${p[1]} / ${p[0]}` : value;
  }

  formatTime(value: string): string {
    return value ? value.substring(0, 5) : '';
  }

  /** input[type=time] يعطي HH:mm، وJava LocalTime يقبل HH:mm:ss. */
  private withSeconds(value: string): string {
    return value.length === 5 ? `${value}:00` : value;
  }

  private clearMessages(): void {
    this.errorMessage.set(null);
    this.okMessage.set(null);
  }

  logout(): void {
    this.auth.logout();
    this.router.navigateByUrl('/login');
  }
}
