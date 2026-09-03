import { Component, OnInit, inject, signal } from "@angular/core";
import { FormBuilder, ReactiveFormsModule, Validators } from "@angular/forms";
import { Router, RouterLink } from "@angular/router";
import { firstValueFrom } from "rxjs";
import { AuthService } from "../../core/services/auth.service";
import { TrainerService } from "../../core/services/trainer.service";
import { CourseService } from "../../core/services/course.service";
import { ModalService } from "../../core/services/modal.service";
import { Trainer } from "../../core/models/trainer.models";

@Component({
  selector: "app-trainers",
  imports: [ReactiveFormsModule, RouterLink],
  templateUrl: "./trainers.html",
  styleUrl: "./trainers.css",
})
export class Trainers implements OnInit {
  private readonly fb = inject(FormBuilder);
  private readonly trainerService = inject(TrainerService);
  private readonly courseService = inject(CourseService);
  private readonly modal = inject(ModalService);
  readonly auth = inject(AuthService);
  private readonly router = inject(Router);

  readonly trainers = signal<Trainer[]>([]);
  readonly loading = signal(false);
  readonly saving = signal(false);
  readonly errorMessage = signal<string | null>(null);
  readonly editingId = signal<number | null>(null);
  readonly formOpen = signal(false);

  readonly form = this.fb.nonNullable.group({
    username: ["", [Validators.required, Validators.minLength(3)]],
    name: ["", [Validators.required]],
    email: ["", [Validators.required, Validators.email]],
    password: [""],
    dateOfBirth: ["", [Validators.required]],
    specialization: [""],
  });

  ngOnInit(): void {
    this.load();
  }

  load(): void {
    this.loading.set(true);
    this.trainerService.findAll().subscribe({
      next: (response) => {
        this.trainers.set(response.data);
        this.loading.set(false);
      },
      error: (err) => {
        this.errorMessage.set(err?.error?.message ?? "تعذّر تحميل المدربين");
        this.loading.set(false);
      },
    });
  }

  openCreate(): void {
    this.editingId.set(null);
    this.errorMessage.set(null);
    this.form.reset();
    this.form.controls.password.setValidators([
      Validators.required,
      Validators.minLength(6),
    ]);
    this.form.controls.password.updateValueAndValidity();
    this.formOpen.set(true);
  }

  openEdit(trainer: Trainer): void {
    this.editingId.set(trainer.id);
    this.errorMessage.set(null);
    this.form.setValue({
      username: trainer.username,
      name: trainer.name,
      email: trainer.email,
      password: "",
      dateOfBirth: trainer.dateOfBirth,
      specialization: trainer.specialization ?? "",
    });
    this.form.controls.password.setValidators([Validators.minLength(6)]);
    this.form.controls.password.updateValueAndValidity();
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
      username: value.username,
      name: value.name,
      email: value.email,
      dateOfBirth: value.dateOfBirth,
      specialization: value.specialization,
      ...(value.password ? { password: value.password } : {}),
    };

    const request =
      id === null
        ? this.trainerService.create(payload)
        : this.trainerService.update(id, payload);

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

  /**
   * تدفّق حذف المدرّب:
   * 1) نسأل الباك اند إذا هذا المدرّب يدرّس كورسات حالياً.
   * 2) إذا ما في كورسات → تأكيد بسيط وحذف مباشر.
   * 3) إذا في كورسات وما في مدربين بدلاء → رسالة "تعذّر الحذف" وتوقّف.
   * 4) إذا في كورسات وفي بدلاء → تأكيد، وبعده لكل كورس نطلب اختيار
   *    مدرّب بديل، ثم ننقل الكورس إليه، وبالنهاية نحذف المدرّب.
   */
  async remove(trainer: Trainer): Promise<void> {
    this.errorMessage.set(null);

    let check;
    try {
      const response = await firstValueFrom(
        this.trainerService.deletionCheck(trainer.id),
      );
      check = response.data;
    } catch (err: any) {
      this.errorMessage.set(
        err?.error?.message ?? "تعذّر التحقّق من بيانات المدرّب",
      );
      return;
    }

    if (check.courses.length === 0) {
      const ok = await this.modal.confirm(
        "حذف المدرّب",
        `هل أنت متأكد من حذف ${trainer.name}؟`,
        { danger: true, confirmText: "حذف" },
      );
      if (!ok) return;
      this.performDelete(trainer.id);
      return;
    }

    if (check.alternativeTrainers.length === 0) {
      const courseNames = check.courses.map((c) => c.title).join("، ");
      await this.modal.alert(
        "تعذّر الحذف",
        `${trainer.name} يدرّس حالياً: ${courseNames}. ولا يوجد مدرّب آخر متاح حالياً لتولّي الكورس.`,
      );
      return;
    }

    const courseNames = check.courses.map((c) => c.title).join("، ");
    const proceed = await this.modal.confirm(
      "المدرّب مستلم كورساً حالياً",
      `${trainer.name} يدرّس حالياً: ${courseNames}. هل أنت متأكد من المتابعة بحذفه؟`,
      { danger: true, confirmText: "متابعة" },
    );
    if (!proceed) return;

    const options = check.alternativeTrainers.map((t) => ({
      value: t.id,
      label: t.name,
    }));

    for (const course of check.courses) {
      const chosen = await this.modal.select(
        "اختيار مدرّب بديل",
        `اختر مدرّباً بديلاً لكورس "${course.title}"`,
        options,
      );
      if (chosen === null) {
        return;
      }
      try {
        await firstValueFrom(
          this.courseService.reassignTrainer(course.id, Number(chosen)),
        );
      } catch (err: any) {
        this.errorMessage.set(
          err?.error?.message ?? "تعذّر نقل الكورس لمدرّب آخر",
        );
        return;
      }
    }

    this.performDelete(trainer.id);
  }

  private performDelete(id: number): void {
    this.trainerService.delete(id).subscribe({
      next: () => this.load(),
      error: (err) =>
        this.errorMessage.set(err?.error?.message ?? "تعذّر الحذف"),
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
