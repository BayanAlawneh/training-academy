import { Component, OnInit, OnDestroy, inject, signal, computed } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { TraineeExamService } from '../../core/services/trainee-exam.service';
import { ModalService } from '../../core/services/modal.service';
import { AnswerSubmission, ExamPaper, ExamQuestion, SubmissionResult } from '../../core/models/exam.models';

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

  /** MCQ: questionId → selectedOptionId */
  readonly choices = signal<Record<number, number>>({});

  /**
   * MATCHING: questionId → { optionId → matchIndex }
   * نخزّن الفهرس لا النص، لأنه ما يفهمه الخادم ولأن النصوص قد تتكرّر.
   */
  readonly pairs = signal<Record<number, Record<number, number>>>({});

  /** الطرف الأيمن المسحوب حالياً — للأجهزة التي تدعم السحب وللنقر المتتابع. */
  readonly dragging = signal<{ questionId: number; index: number } | null>(null);

  readonly secondsLeft = signal<number | null>(null);

  private timer: ReturnType<typeof setInterval> | null = null;

  /** سؤال المطابقة يُعدّ مُجاباً حين تُوصَل كل أطرافه لا بعضها. */
  readonly answeredCount = computed(() => {
    const p = this.paper();
    if (!p) return 0;

    return p.questions.filter(q => {
      if (q.type === 'MATCHING') {
        const got = this.pairs()[q.id] ?? {};
        return Object.keys(got).length === q.options.length;
      }
      return this.choices()[q.id] !== undefined;
    }).length;
  });

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

  // ---------------- المطابقة ----------------

  /** يبدأ سحب طرف أيمن، أو يحدّده بالنقر على الأجهزة التي لا تسحب. */
  startDrag(questionId: number, index: number): void {
    this.dragging.set({ questionId, index });
  }

  endDrag(): void {
    this.dragging.set(null);
  }

  /** يُفلت الطرف المسحوب على عنصر أيسر. */
  dropOn(questionId: number, optionId: number): void {
    const held = this.dragging();
    if (!held || held.questionId !== questionId) return;

    this.assign(questionId, optionId, held.index);
    this.dragging.set(null);
  }

  /**
   * يربط طرفاً أيمن بعنصر أيسر.
   * الطرف الأيمن يُستخدم مرّة واحدة: إن كان موصولاً بعنصر آخر، يُفَكّ منه
   * أولاً — وإلا أمكن وصل طرف واحد بعدّة عناصر وهو ما لا تسمح به المطابقة.
   */
  private assign(questionId: number, optionId: number, index: number): void {
    const all = { ...this.pairs() };
    const forQuestion = { ...(all[questionId] ?? {}) };

    for (const key of Object.keys(forQuestion)) {
      if (forQuestion[Number(key)] === index) delete forQuestion[Number(key)];
    }

    forQuestion[optionId] = index;
    all[questionId] = forQuestion;
    this.pairs.set(all);
  }

  /** يفكّ الوصل عن عنصر أيسر. */
  clearPair(questionId: number, optionId: number): void {
    const all = { ...this.pairs() };
    const forQuestion = { ...(all[questionId] ?? {}) };
    delete forQuestion[optionId];
    all[questionId] = forQuestion;
    this.pairs.set(all);
  }

  /** نصّ الطرف الموصول بعنصر أيسر، أو null. */
  matchedText(q: ExamQuestion, optionId: number): string | null {
    const index = this.pairs()[q.id]?.[optionId];
    if (index === undefined || !q.matches) return null;
    return q.matches.find(m => m.index === index)?.text ?? null;
  }

  /** هل هذا الطرف الأيمن مستخدَم بالفعل؟ يُخفَى من العمود حتى لا يُكرَّر. */
  isMatchUsed(questionId: number, index: number): boolean {
    const forQuestion = this.pairs()[questionId] ?? {};
    return Object.values(forQuestion).includes(index);
  }

  isDragging(questionId: number, index: number): boolean {
    const held = this.dragging();
    return held !== null && held.questionId === questionId && held.index === index;
  }

  /** عنصر أيسر جاهز لاستقبال الإفلات. */
  isDropTarget(questionId: number): boolean {
    const held = this.dragging();
    return held !== null && held.questionId === questionId;
  }

  matchedCount(q: ExamQuestion): number {
    return Object.keys(this.pairs()[q.id] ?? {}).length;
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
        ? `تركتِ ${missing} سؤالاً بلا إجابة أو ناقص الوصل. تسليم الآن؟`
        : 'سيتم تسليم إجاباتك نهائياً ولا يمكن التعديل بعدها. متأكدة؟';

      const ok = await this.modal.confirm('تسليم الاختبار', message, { confirmText: 'تسليم' });
      if (!ok) return;
    }

    this.submitting.set(true);
    this.errorMessage.set(null);

    const answers: AnswerSubmission[] = p.questions.map(q => {
      if (q.type === 'MATCHING') {
        const forQuestion = this.pairs()[q.id] ?? {};
        return {
          questionId: q.id,
          selectedOptionId: null,
          pairs: q.options.map(o => ({
            optionId: o.id,
            matchIndex: forQuestion[o.id] ?? null
          }))
        };
      }
      return {
        questionId: q.id,
        selectedOptionId: this.choices()[q.id] ?? null,
        pairs: null
      };
    });

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
