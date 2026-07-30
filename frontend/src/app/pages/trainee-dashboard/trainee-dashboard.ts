import { Component, inject } from '@angular/core';
import { Router } from '@angular/router';
import { AuthService } from '../../core/services/auth.service';

@Component({
  selector: 'app-trainee-dashboard',
  imports: [],
  templateUrl: './trainee-dashboard.html',
  styleUrl: './trainee-dashboard.css'
})
export class TraineeDashboard {
  readonly auth = inject(AuthService);
  private readonly router = inject(Router);

  logout(): void {
    this.auth.logout();
    this.router.navigateByUrl('/login');
  }
}