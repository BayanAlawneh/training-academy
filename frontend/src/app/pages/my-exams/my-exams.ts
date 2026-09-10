import { Component, OnInit, inject, signal, computed } from '@angular/core';
import { Router, RouterLink } from '@angular/router';
import { NotificationBell } from '../../shared/notification-bell/notification-bell';
import { AuthService } from '../../core/services/auth.service';
import { TraineeExamService } from '../../core/services/trainee-exam.service';
import { EXAM_STATE_LABEL, ExamState, MyExam } from '../../core/models/exam.models';

@Component({
  selector: 'app-my-exams',
  imports: [RouterLink, NotificationBell],
  templateUrl: './my-exams.html',
  styleUrl: './my-exams.css'
})
export class MyExams implements OnInit {

  readonly auth = inject(AuthService);
  private readonly api = inject(TraineeExamService);
  private readonly router = inject(Router);

  readonly exams = signal<MyExam[]>([]);
  readonly loading = signal(false);
  readonly errorMessage = signal<string | null>(null);

  readonly submitted = computed(() => this.exams().filter(e => e.state === 'SUBMITTED'));

  /** المعدّل من المصحَّح والفائت — الاختبارات القادمة لا تُحسب. */
  readonly average = computed(() => {
    const graded = this.exams().filter(e => e.score !== null);
    if (graded.length === 0) return null;

    const earned = graded.reduce((sum, e) => sum + (e.score ?? 0), 0);
    const total = graded.reduce((sum, e) => sum + e.totalMarks, 0);
    return total === 0 ? null : Math.round((earned / total) * 100);
  });

  ngOnInit(): void {
    this.load();
  }

  load(): void {
    this.loading.set(true);
    this.errorMessage.set(null);

    this.api.list().subscribe({
      next: (r) => { this.exams.set(r.data); this.loading.set(false); },
      error: (err) => {
        this.errorMessage.set(err?.error?.message ?? 'تعذّر تحميل الاختبارات');
        this.loading.set(false);
      }
    });
  }

  start(exam: MyExam): void {
    this.router.navigate(['/trainee/exams', exam.examId]);
  }

  label(state: ExamState): string {
    return EXAM_STATE_LABEL[state] ?? state;
  }

  badgeClass(state: ExamState): string {
    switch (state) {
      case 'OPEN':      return 'b-open';
      case 'SUBMITTED': return 'b-done';
      case 'MISSED':    return 'b-missed';
      default:          return 'b-upcoming';
    }
  }

  /** 2026-09-10T10:00:00 → 10 / 09 / 2026 — 10:00 */
  formatDateTime(value: string | null): string {
    if (!value) return '—';
    const [date, time] = value.split('T');
    const p = date.split('-');
    const hm = time ? time.substring(0, 5) : '';
    return p.length === 3 ? `${p[2]} / ${p[1]} / ${p[0]} — ${hm}` : value;
  }

  percent(exam: MyExam): number | null {
    if (exam.score === null || exam.totalMarks === 0) return null;
    return Math.round((exam.score / exam.totalMarks) * 100);
  }

  logout(): void {
    this.auth.logout();
    this.router.navigateByUrl('/login');
  }
}
