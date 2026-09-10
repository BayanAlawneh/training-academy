import { Component, OnInit, inject, signal, computed } from '@angular/core';
import { Router, RouterLink } from '@angular/router';
import { NotificationBell } from '../../shared/notification-bell/notification-bell';
import { AuthService } from '../../core/services/auth.service';
import { PortalService } from '../../core/services/portal.service';
import { MyCourse } from '../../core/models/portal.models';

@Component({
  selector: 'app-my-courses',
  imports: [RouterLink, NotificationBell],
  templateUrl: './my-courses.html',
  styleUrl: './my-courses.css'
})
export class MyCourses implements OnInit {

  readonly auth = inject(AuthService);
  private readonly portal = inject(PortalService);
  private readonly router = inject(Router);

  readonly courses = signal<MyCourse[]>([]);
  readonly loading = signal(false);
  readonly errorMessage = signal<string | null>(null);

  readonly isEmpty = computed(() => !this.loading() && this.courses().length === 0);

  ngOnInit(): void {
    this.load();
  }

  load(): void {
    this.loading.set(true);
    this.errorMessage.set(null);

    this.portal.myCourses().subscribe({
      next: (response) => {
        this.courses.set(response.data);
        this.loading.set(false);
      },
      error: (err) => {
        this.errorMessage.set(err?.error?.message ?? 'تعذّر تحميل الكورسات');
        this.loading.set(false);
      }
    });
  }

  /** 2026-03-14 → 14 / 03 / 2026 */
  formatDate(value: string | null): string {
    if (!value) return '—';
    const parts = value.split('-');
    if (parts.length !== 3) return value;
    return `${parts[2]} / ${parts[1]} / ${parts[0]}`;
  }

  logout(): void {
    this.auth.logout();
    this.router.navigateByUrl('/login');
  }
}
