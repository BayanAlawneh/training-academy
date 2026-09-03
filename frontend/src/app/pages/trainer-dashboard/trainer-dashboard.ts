import { Component, OnInit, inject, signal } from '@angular/core';
import { Router, RouterLink } from '@angular/router';
import { AuthService } from '../../core/services/auth.service';
import { TrainerPortalService } from '../../core/services/trainer-portal.service';
import { TrainerSummary } from '../../core/models/portal.models';

@Component({
  selector: 'app-trainer-dashboard',
  imports: [RouterLink],
  templateUrl: './trainer-dashboard.html',
  styleUrl: './trainer-dashboard.css'
})
export class TrainerDashboard implements OnInit {

  readonly auth = inject(AuthService);
  private readonly api = inject(TrainerPortalService);
  private readonly router = inject(Router);

  readonly summary = signal<TrainerSummary | null>(null);
  readonly errorMessage = signal<string | null>(null);

  ngOnInit(): void {
    this.api.summary().subscribe({
      next: (r) => this.summary.set(r.data),
      error: (err) => this.errorMessage.set(err?.error?.message ?? 'تعذّر تحميل بياناتك')
    });
  }

  logout(): void {
    this.auth.logout();
    this.router.navigateByUrl('/login');
  }
}
