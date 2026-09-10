import { Component, OnInit, inject, signal } from '@angular/core';
import { FormArray, FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { NotificationBell } from '../../shared/notification-bell/notification-bell';
import { AuthService } from '../../core/services/auth.service';
import { ExamService } from '../../core/services/exam.service';
import { ModalService } from '../../core/services/modal.service';
import { Exam, GRADE_STATE_LABEL, GradeRow, GradeState, QUESTION_KIND_LABEL, QuestionKind } from '../../core/models/exam.models';

@Component({
  selector: 'app-trainer-exams',
  imports: [ReactiveFormsModule, RouterLink, NotificationBell],
  templateUrl: './trainer-exams.html',
  styleUrl: './trainer-exams.css'
})
export class TrainerExams implements OnInit {

  readonly auth = inject(AuthService);
  private readonly api = inject(ExamService);
  private readonly fb = inject(FormBuilder);
  private readonly modal = inject(ModalService);
  private readonly router = inject(Router);

  readonly exams = signal<Exam[]>([]);
  readonly loading = signal(false);
  readonly errorMessage = signal<string | null>(null);
  readonly okMessage = signal<string | null>(null);

  readonly showBuilder = signal(false);
  readonly gradesFor = signal<Exam | null>(null);
  readonly grades = signal<GradeRow[]>([]);

  readonly examForm: FormGroup = this.fb.group({
    title: ['', [Validators.required, Validators.maxLength(180)]],
    description: [''],
    opensAt: ['', Validators.required],
    closesAt: ['', Validators.required],
    questions: this.fb.array([])
  });

  get questions(): FormArray {
    return this.examForm.get('questions') as FormArray;
  }

  optionsOf(index: number): FormArray {
    return this.questions.at(index).get('options') as FormArray;
  }

  ngOnInit(): void {
    this.load();
  }

  load(): void {
    this.loading.set(true);
    this.api.list().subscribe({
      next: (r) => { this.exams.set(r.data); this.loading.set(false); },
      error: (err) => {
        this.errorMessage.set(err?.error?.message ?? 'تعذّر تحميل الاختبارات');
        this.loading.set(false);
      }
    });
  }

  openBuilder(): void {
    this.examForm.reset({ title: '', description: '', opensAt: '', closesAt: '' });
    this.questions.clear();
    this.addQuestion();
    this.gradesFor.set(null);
    this.showBuilder.set(true);
  }

  closeBuilder(): void {
    this.showBuilder.set(false);
  }

  readonly kinds: QuestionKind[] = ['MCQ', 'MATCHING'];
  readonly kindLabel = QUESTION_KIND_LABEL;

  addQuestion(): void {
    this.questions.push(this.fb.group({
      text: ['', [Validators.required, Validators.maxLength(1000)]],
      type: ['MCQ' as QuestionKind, Validators.required],
      marks: [5, [Validators.required, Validators.min(1)]],
      correctIndex: [0, Validators.required],
      options: this.fb.array([this.newOption(), this.newOption()])
    }));
  }

  kindOf(index: number): QuestionKind {
    return this.questions.at(index).get('type')!.value as QuestionKind;
  }

  isMatching(index: number): boolean {
    return this.kindOf(index) === 'MATCHING';
  }

  /**
   * تغيير النوع يمسح الخيارات ويعيد بناءها.
   * الحقول تختلف بين النوعين — الاحتفاظ بالقديمة يترك بيانات نصفية
   * كخيار مطابقة بلا طرف أيمن، فيُرفض عند الحفظ برسالة غامضة.
   */
  onKindChange(index: number): void {
    const options = this.optionsOf(index);
    options.clear();
    options.push(this.newOption());
    options.push(this.newOption());
    this.questions.at(index).get('correctIndex')!.setValue(0);
  }

  removeQuestion(index: number): void {
    if (this.questions.length > 1) this.questions.removeAt(index);
  }

  addOption(qIndex: number): void {
    const options = this.optionsOf(qIndex);
    if (options.length < 6) options.push(this.newOption());
  }

  removeOption(qIndex: number, oIndex: number): void {
    const options = this.optionsOf(qIndex);
    if (options.length <= 2) return;

    options.removeAt(oIndex);

    // الإجابة الصحيحة مخزّنة كفهرس، فإزالة خيار قبلها تُزيحه.
    const question = this.questions.at(qIndex);
    const correct = Number(question.get('correctIndex')!.value);
    if (correct === oIndex) question.get('correctIndex')!.setValue(0);
    else if (correct > oIndex) question.get('correctIndex')!.setValue(correct - 1);
  }

  setCorrect(qIndex: number, oIndex: number): void {
    this.questions.at(qIndex).get('correctIndex')!.setValue(oIndex);
  }

  isCorrect(qIndex: number, oIndex: number): boolean {
    return Number(this.questions.at(qIndex).get('correctIndex')!.value) === oIndex;
  }

  totalMarks(): number {
    return this.questions.controls
      .reduce((sum, q) => sum + (Number(q.get('marks')!.value) || 0), 0);
  }

  save(): void {
    if (this.examForm.invalid) { this.examForm.markAllAsTouched(); return; }

    // فحص محلي يعطي رسالة أوضح من ردّ الخادم العام
    for (let i = 0; i < this.questions.length; i++) {
      if (!this.isMatching(i)) continue;
      const options = this.optionsOf(i);
      for (let k = 0; k < options.length; k++) {
        if (!options.at(k).get('matchText')!.value?.trim()) {
          this.errorMessage.set(`السؤال ${i + 1}: الزوج ${k + 1} ناقص الطرف الأيمن`);
          return;
        }
      }
    }

    this.clearMessages();

    const raw = this.examForm.getRawValue();

    const payload = {
      title: raw.title,
      description: raw.description?.trim() || null,
      opensAt: this.withSeconds(raw.opensAt),
      closesAt: this.withSeconds(raw.closesAt),
      questions: raw.questions.map((q: any) => ({
        text: q.text,
        type: q.type as QuestionKind,
        marks: Number(q.marks),
        options: q.options.map((o: any, i: number) => ({
          text: o.text,
          correct: q.type === 'MATCHING' ? false : i === Number(q.correctIndex),
          matchText: q.type === 'MATCHING' ? (o.matchText?.trim() || null) : null
        }))
      }))
    };

    this.api.create(payload).subscribe({
      next: () => {
        this.okMessage.set('تم إنشاء الاختبار. انشريه ليظهر للمتدربين.');
        this.closeBuilder();
        this.load();
      },
      error: (err) => this.errorMessage.set(err?.error?.message ?? 'تعذّر الحفظ')
    });
  }

  togglePublish(exam: Exam): void {
    this.clearMessages();
    this.api.setPublished(exam.id, !exam.published).subscribe({
      next: () => {
        this.okMessage.set(exam.published ? 'تم إخفاء الاختبار' : 'تم نشر الاختبار');
        this.load();
      },
      error: (err) => this.errorMessage.set(err?.error?.message ?? 'تعذّر تغيير حالة النشر')
    });
  }

  async remove(exam: Exam): Promise<void> {
    const ok = await this.modal.confirm(
      'حذف الاختبار', `حذف "${exam.title}"؟`, { danger: true, confirmText: 'حذف' });
    if (!ok) return;

    this.clearMessages();
    this.api.delete(exam.id).subscribe({
      next: () => { this.okMessage.set('تم حذف الاختبار'); this.load(); },
      error: (err) => this.errorMessage.set(err?.error?.message ?? 'تعذّر الحذف')
    });
  }

  openGrades(exam: Exam): void {
    this.clearMessages();
    this.showBuilder.set(false);
    this.gradesFor.set(exam);

    this.api.grades(exam.id).subscribe({
      next: (r) => this.grades.set(r.data),
      error: (err) => {
        this.errorMessage.set(err?.error?.message ?? 'تعذّر تحميل العلامات');
        this.gradesFor.set(null);
      }
    });
  }

  closeGrades(): void {
    this.gradesFor.set(null);
    this.grades.set([]);
  }

  gradeLabel(state: GradeState): string {
    return GRADE_STATE_LABEL[state] ?? state;
  }

  gradeClass(state: GradeState): string {
    switch (state) {
      case 'SUBMITTED': return 'g-done';
      case 'MISSED':    return 'g-missed';
      default:          return 'g-pending';
    }
  }

  formatDateTime(value: string | null): string {
    if (!value) return '—';
    const [date, time] = value.split('T');
    const p = date.split('-');
    const hm = time ? time.substring(0, 5) : '';
    return p.length === 3 ? `${p[2]} / ${p[1]} — ${hm}` : value;
  }

  private newOption(): FormGroup {
    return this.fb.group({
      text: ['', Validators.required],
      matchText: ['']
    });
  }

  /** input[type=datetime-local] يعطي yyyy-MM-ddTHH:mm، وJava يقبل الثواني. */
  private withSeconds(value: string): string {
    return value && value.length === 16 ? `${value}:00` : value;
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
