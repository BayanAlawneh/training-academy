import { Component, OnInit, inject, signal, computed } from '@angular/core';
import { Router, RouterLink } from '@angular/router';
import { forkJoin } from 'rxjs';
import { AuthService } from '../../core/services/auth.service';
import { TrainerService } from '../../core/services/trainer.service';
import { TraineeService } from '../../core/services/trainee.service';
import { CourseService } from '../../core/services/course.service';
import { NotificationBell } from '../../shared/notification-bell/notification-bell';

@Component({
  selector: 'app-admin-dashboard',
  imports: [RouterLink, NotificationBell],
  templateUrl: './admin-dashboard.html',
  styleUrl: './admin-dashboard.css'
})
export class AdminDashboard implements OnInit {

  readonly auth = inject(AuthService);
  private readonly trainerService = inject(TrainerService);
  private readonly traineeService = inject(TraineeService);
  private readonly courseService = inject(CourseService);
  private readonly router = inject(Router);

  readonly trainers = signal(0);
  readonly trainees = signal(0);
  readonly courses = signal(0);
  readonly enrolled = signal(0);
  readonly capacity = signal(0);
  readonly loading = signal(true);
  readonly errorMessage = signal<string | null>(null);

  /** المدربون بلا كورس — الفئة المتاحة لإسناد كورس جديد أو لاستبدال مدرّب محذوف. */
  readonly freeTrainers = computed(() => Math.max(0, this.trainers() - this.courses()));

  readonly fillRate = computed(() => {
    const cap = this.capacity();
    return cap === 0 ? null : Math.round((this.enrolled() / cap) * 100);
  });

  ngOnInit(): void {
    // ثلاثة طلبات متوازية بدل متسلسلة — تحميل اللوحة يساوي أبطأ طلب لا مجموعها.
    forkJoin({
      trainers: this.trainerService.findAll(),
      trainees: this.traineeService.findAll(),
      courses: this.courseService.findAll(),
    }).subscribe({
      next: (r) => {
        this.trainers.set(r.trainers.data?.length ?? 0);
        this.trainees.set(r.trainees.data?.length ?? 0);

        const list = r.courses.data ?? [];
        this.courses.set(list.length);
        this.enrolled.set(list.reduce((sum, c) => sum + (c.enrolledCount ?? 0), 0));
        this.capacity.set(list.reduce((sum, c) => sum + (c.capacity ?? 0), 0));

        this.loading.set(false);
      },
      error: (err) => {
        this.errorMessage.set(err?.error?.message ?? 'تعذّر تحميل ملخّص النظام');
        this.loading.set(false);
      }
    });
  }

  logout(): void {
    this.auth.logout();
    this.router.navigateByUrl('/login');
  }
}
