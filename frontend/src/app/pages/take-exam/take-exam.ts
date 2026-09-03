import { Component, OnInit, OnDestroy, inject, signal, computed } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { TraineeExamService } from '../../core/services/trainee-exam.service';
import { ModalService } from '../../core/services/modal.service';
import { ExamPaper, SubmissionResult } from '../../core/models/exam.models';

@Component({
  selector: 'app-take-exam',
  imports: [],
  templateUrl: './take-exam.html',
  styleUrl: './take-exam.css'
})
export class TakeExam implements OnInit, OnDestroy {

  private readonly api = inject(TraineeExamService);
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly modal = inject(ModalService);

  readonly paper = signal<ExamPaper | null>(null);
  readonly result = signal<SubmissionResult | null>(null);
  readonly loading = signal(true);
  readonly submitting = signal(false);
  readonly errorMessage = signal<string | null>(null);

  /** questionId → selectedOptionId */
  readonly choices = signal<Record<number, number>>({});
  readonly secondsLeft = signal<number | null>(null);

  private timer: ReturnType<typeof setInterval> | null = null;

  readonly answeredCount = computed(() => Object.keys(this.choices()).length);

  readonly allAnswered = computed(() => {
    const p = this.paper();
    return p ? this.answeredCount() === p.questions.length : false;
  });

  readonly timeLabel = computed(() => {
    const s = this.secondsLeft();
    if (s === null) return '';
    if (s <= 0) return '00:00';

    const h = Math.floor(s / 3600);
    const m = Math.floor((s % 3600) / 60);
    const sec = s % 60;
    const pad = (n: number) => String(n).padStart(2, '0');
    return h > 0 ? `${pad(h)}:${pad(m)}:${pad(sec)}` : `${pad(m)}:${pad(sec)}`;
  });

  readonly urgent = computed(() => {
    const s = this.secondsLeft();
    return s !== null && s > 0 && s <= 300;
  });

  ngOnInit(): void {
    const id = Number(this.route.snapshot.paramMap.get('id'));
    if (!id) { this.router.navigateByUrl('/trainee/exams'); return; }

    this.api.paper(id).subscribe({
      next: (r) => {
        this.paper.set(r.data);
        this.loading.set(false);
        this.startTimer(r.data.closesAt);
      },
      error: (err) => {
        this.errorMessage.set(err?.error?.message ?? 'تعذّر فتح الاختبار');
        this.loading.set(false);
      }
    });
  }

  ngOnDestroy(): void {
    this.stopTimer();
  }

  /**
   * العدّاد يعتمد على closesAt القادم من الخادم لا على مؤقّت محلي،
   * فتغيير ساعة الجهاز لا يمنح وقتاً إضافياً. والخادم يرفض التسليم
   * المتأخر على أي حال — العدّاد للراحة لا للحماية.
   */
  private startTimer(closesAt: string): void {
    const tick = () => {
      const remaining = Math.floor((new Date(closesAt).getTime() - Date.now()) / 1000);
      this.secondsLeft.set(remaining);

      if (remaining <= 0) {
        this.stopTimer();
        if (!this.result() && !this.submitting()) this.submit(true);
      }
    };

    tick();
    this.timer = setInterval(tick, 1000);
  }

  private stopTimer(): void {
    if (this.timer) { clearInterval(this.timer); this.timer = null; }
  }

  choose(questionId: number, optionId: number): void {
    this.choices.set({ ...this.choices(), [questionId]: optionId });
  }

  isChosen(questionId: number, optionId: number): boolean {
    return this.choices()[questionId] === optionId;
  }

  isAnswered(questionId: number): boolean {
    return this.choices()[questionId] !== undefined;
  }

  async submit(auto = false): Promise<void> {
    const p = this.paper();
    if (!p || this.submitting()) return;

    if (!auto) {
      const missing = p.questions.length - this.answeredCount();
      const message = missing > 0
        ? `تركتِ ${missing} سؤالاً بلا إجابة وستُحتسب صفراً. تسليم الآن؟`
        : 'سيتم تسليم إجاباتك نهائياً ولا يمكن التعديل بعدها. متأكدة؟';

      const ok = await this.modal.confirm('تسليم الاختبار', message, { confirmText: 'تسليم' });
      if (!ok) return;
    }

    this.submitting.set(true);
    this.errorMessage.set(null);

    const answers = p.questions.map(q => ({
      questionId: q.id,
      selectedOptionId: this.choices()[q.id] ?? null
    }));

    this.api.submit(p.examId, answers).subscribe({
      next: (r) => {
        this.stopTimer();
        this.result.set(r.data);
        this.submitting.set(false);
      },
      error: (err) => {
        this.errorMessage.set(err?.error?.message ?? 'تعذّر تسليم الاختبار');
        this.submitting.set(false);
      }
    });
  }

  percent(): number {
    const r = this.result();
    if (!r || r.totalMarks === 0) return 0;
    return Math.round((r.score / r.totalMarks) * 100);
  }

  back(): void {
    this.router.navigateByUrl('/trainee/exams');
  }
}
