import { Component, OnInit, inject, signal } from "@angular/core";
import { FormBuilder, ReactiveFormsModule, Validators } from "@angular/forms";
import { Router, RouterLink } from "@angular/router";
import { AuthService } from "../../core/services/auth.service";
import { CourseService } from "../../core/services/course.service";
import { TrainerService } from "../../core/services/trainer.service";
import { ModalService } from "../../core/services/modal.service";
import { Course } from "../../core/models/course.models";
import { Trainer } from "../../core/models/trainer.models";

@Component({
  selector: "app-courses",
  imports: [ReactiveFormsModule, RouterLink],
  templateUrl: "./courses.html",
  styleUrl: "./courses.css",
})
export class Courses implements OnInit {
  private readonly fb = inject(FormBuilder);
  private readonly courseService = inject(CourseService);
  private readonly trainerService = inject(TrainerService);
  private readonly modal = inject(ModalService);
  readonly auth = inject(AuthService);
  private readonly router = inject(Router);

  readonly courses = signal<Course[]>([]);
  readonly trainers = signal<Trainer[]>([]);
  readonly loading = signal(false);
  readonly saving = signal(false);
  readonly errorMessage = signal<string | null>(null);
  readonly editingId = signal<number | null>(null);
  readonly formOpen = signal(false);

  readonly form = this.fb.nonNullable.group({
    title: ["", [Validators.required]],
    capacity: [1, [Validators.required, Validators.min(1)]],
    trainerId: [0, [Validators.required, Validators.min(1)]],
    description: [""],
    durationWeeks: [0],
  });

  ngOnInit(): void {
    this.load();
    this.loadTrainers();
  }

  load(): void {
    this.loading.set(true);
    this.courseService.findAll().subscribe({
      next: (response) => {
        this.courses.set(response.data);
        this.loading.set(false);
      },
      error: (err) => {
        this.errorMessage.set(err?.error?.message ?? "تعذّر تحميل الكورسات");
        this.loading.set(false);
      },
    });
  }

  loadTrainers(): void {
    this.trainerService.findAll().subscribe({
      next: (response) => this.trainers.set(response.data),
      error: () => this.errorMessage.set("تعذّر تحميل قائمة المدربين"),
    });
  }

  openCreate(): void {
    this.editingId.set(null);
    this.errorMessage.set(null);
    this.form.setValue({ title: "", capacity: 1, trainerId: 0, description: "", durationWeeks: 0 });
    this.formOpen.set(true);
  }

  openEdit(course: Course): void {
    this.editingId.set(course.id);
    this.errorMessage.set(null);
    this.form.setValue({
      title: course.title,
      capacity: course.capacity,
      trainerId: course.trainerId,
      description: course.description ?? "",
      durationWeeks: course.durationWeeks ?? 0,
    });
    this.formOpen.set(true);
  }

  cancel(): void {
    this.formOpen.set(false);
    this.errorMessage.set(null);
  }

  submit(): void {
    this.errorMessage.set(null);

    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    this.saving.set(true);
    const value = this.form.getRawValue();
    const id = this.editingId();

    const payload = {
      title: value.title,
      capacity: Number(value.capacity),
      trainerId: Number(value.trainerId),
      description: value.description?.trim() ? value.description.trim() : null,
      // 0 يعني "غير محدّدة" — نرسل null لا صفراً حتى لا يفشل التحقّق @Min(1)
      durationWeeks: Number(value.durationWeeks) > 0 ? Number(value.durationWeeks) : null,
    };

    const request =
      id === null
        ? this.courseService.create(payload)
        : this.courseService.update(id, payload);

    request.subscribe({
      next: () => {
        this.saving.set(false);
        this.formOpen.set(false);
        this.load();
      },
      error: (err) => {
        this.saving.set(false);
        this.errorMessage.set(err?.error?.message ?? "تعذّر حفظ البيانات");
      },
    });
  }

  async remove(course: Course): Promise<void> {
    const ok = await this.modal.confirm(
      "حذف الكورس",
      `حذف الكورس ${course.title}؟`,
      { danger: true, confirmText: "حذف" },
    );
    if (!ok) return;

    this.courseService.delete(course.id).subscribe({
      next: () => this.load(),
      error: (err) =>
        this.errorMessage.set(err?.error?.message ?? "تعذّر الحذف"),
    });
  }

  manageEnrollment(course: Course): void {
    this.router.navigate(["/admin/enrollments"], {
      queryParams: { courseId: course.id },
    });
  }

  back(): void {
    this.router.navigateByUrl("/admin");
  }

  logout(): void {
    this.auth.logout();
    this.router.navigateByUrl("/login");
  }
}
