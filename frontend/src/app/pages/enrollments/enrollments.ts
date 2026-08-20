import { Component, OnInit, inject, signal } from "@angular/core";
import { ActivatedRoute, Router } from "@angular/router";
import { AuthService } from "../../core/services/auth.service";
import { CourseService } from "../../core/services/course.service";
import { TraineeService } from "../../core/services/trainee.service";
import { EnrollmentService } from "../../core/services/enrollment.service";
import { ModalService } from "../../core/services/modal.service";
import { Course } from "../../core/models/course.models";
import { Trainee } from "../../core/models/trainee.models";
import { Enrollment } from "../../core/models/enrollment.models";

@Component({
  selector: "app-enrollments",
  imports: [],
  templateUrl: "./enrollments.html",
  styleUrl: "./enrollments.css",
})
export class Enrollments implements OnInit {
  private readonly courseService = inject(CourseService);
  private readonly traineeService = inject(TraineeService);
  private readonly enrollmentService = inject(EnrollmentService);
  private readonly modal = inject(ModalService);
  readonly auth = inject(AuthService);
  private readonly router = inject(Router);
  private readonly route = inject(ActivatedRoute);

  readonly courses = signal<Course[]>([]);
  readonly trainees = signal<Trainee[]>([]);
  readonly enrollments = signal<Enrollment[]>([]);

  readonly selectedCourseId = signal<number | null>(null);
  readonly selectedTraineeId = signal<number | null>(null);

  readonly loading = signal(false);
  readonly saving = signal(false);
  readonly errorMessage = signal<string | null>(null);
  readonly successMessage = signal<string | null>(null);

  ngOnInit(): void {
    this.loadCourses();
    this.loadTrainees();

    const fromQuery = this.route.snapshot.queryParamMap.get("courseId");
    if (fromQuery) {
      this.selectedCourseId.set(Number(fromQuery));
      this.loadEnrollments();
    }
  }

  loadCourses(): void {
    this.courseService.findAll().subscribe({
      next: (response) => this.courses.set(response.data),
      error: (err) =>
        this.errorMessage.set(err?.error?.message ?? "تعذّر تحميل الكورسات"),
    });
  }

  loadTrainees(): void {
    this.traineeService.findAll().subscribe({
      next: (response) => this.trainees.set(response.data),
      error: (err) =>
        this.errorMessage.set(err?.error?.message ?? "تعذّر تحميل المتدربين"),
    });
  }

  onCourseChange(value: string): void {
    const id = value ? Number(value) : null;
    this.selectedCourseId.set(id);
    this.errorMessage.set(null);
    this.successMessage.set(null);
    this.enrollments.set([]);

    if (id !== null) {
      this.loadEnrollments();
    }
  }

  onTraineeChange(value: string): void {
    this.selectedTraineeId.set(value ? Number(value) : null);
  }

  loadEnrollments(): void {
    const courseId = this.selectedCourseId();
    if (courseId === null) return;

    this.loading.set(true);
    this.enrollmentService.findByCourse(courseId).subscribe({
      next: (response) => {
        this.enrollments.set(response.data);
        this.loading.set(false);
      },
      error: (err) => {
        this.errorMessage.set(err?.error?.message ?? "تعذّر تحميل التسجيلات");
        this.loading.set(false);
      },
    });
  }

  selectedCourse(): Course | undefined {
    const id = this.selectedCourseId();
    return id === null ? undefined : this.courses().find((c) => c.id === id);
  }

  enrol(): void {
    this.errorMessage.set(null);
    this.successMessage.set(null);

    const courseId = this.selectedCourseId();
    const traineeId = this.selectedTraineeId();

    if (courseId === null || traineeId === null) {
      this.errorMessage.set("اختر الكورس والمتدرّب أولاً");
      return;
    }

    this.saving.set(true);
    this.enrollmentService.enrol({ courseId, traineeId }).subscribe({
      next: (response) => {
        this.saving.set(false);
        this.successMessage.set(`تم تسجيل ${response.data.traineeName}`);
        this.selectedTraineeId.set(null);
        this.loadEnrollments();
        this.loadCourses();
      },
      error: (err) => {
        this.saving.set(false);
        this.errorMessage.set(err?.error?.message ?? "تعذّر التسجيل");
      },
    });
  }

  async remove(enrollment: Enrollment): Promise<void> {
    const ok = await this.modal.confirm(
      "إلغاء التسجيل",
      `إلغاء تسجيل ${enrollment.traineeName}؟`,
      { danger: true, confirmText: "إلغاء التسجيل" },
    );
    if (!ok) return;

    this.enrollmentService.remove(enrollment.id).subscribe({
      next: () => {
        this.successMessage.set(null);
        this.loadEnrollments();
        this.loadCourses();
      },
      error: (err) =>
        this.errorMessage.set(err?.error?.message ?? "تعذّر إلغاء التسجيل"),
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
