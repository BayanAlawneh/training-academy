import { Component, OnInit, inject, signal } from '@angular/core';
import { Router, RouterLink } from '@angular/router';
import { NotificationBell } from '../../shared/notification-bell/notification-bell';
import { AuthService } from '../../core/services/auth.service';
import { PortalService } from '../../core/services/portal.service';
import { MySummary } from '../../core/models/portal.models';

@Component({
  selector: 'app-trainee-dashboard',
  imports: [RouterLink, NotificationBell],
  templateUrl: './trainee-dashboard.html',
  styleUrl: './trainee-dashboard.css'
})
export class TraineeDashboard implements OnInit {

  readonly auth = inject(AuthService);
  private readonly portal = inject(PortalService);
  private readonly router = inject(Router);

  readonly summary = signal<MySummary | null>(null);
  readonly loading = signal(false);
  readonly errorMessage = signal<string | null>(null);

  ngOnInit(): void {
    this.loading.set(true);
    this.portal.mySummary().subscribe({
      next: (response) => {
        this.summary.set(response.data);
        this.loading.set(false);
      },
      error: (err) => {
        this.errorMessage.set(err?.error?.message ?? 'تعذّر تحميل بياناتك');
        this.loading.set(false);
      }
    });
  }

  formatDate(value: string | null | undefined): string {
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
