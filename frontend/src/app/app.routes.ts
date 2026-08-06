import { Routes } from '@angular/router';
import { authGuard, roleGuard } from './core/guards/auth.guard';
import { Login } from './pages/login/login';
import { Signup } from './pages/signup/signup';
import { AdminDashboard } from './pages/admin-dashboard/admin-dashboard';
import { TrainerDashboard } from './pages/trainer-dashboard/trainer-dashboard';
import { TraineeDashboard } from './pages/trainee-dashboard/trainee-dashboard';

export const routes: Routes = [
  { path: 'login', component: Login },
  { path: 'signup', component: Signup },

  {
    path: 'admin',
    component: AdminDashboard,
    canActivate: [authGuard, roleGuard],
    data: { roles: ['ADMIN'] }
  },
  {
    path: 'trainer',
    component: TrainerDashboard,
    canActivate: [authGuard, roleGuard],
    data: { roles: ['TRAINER'] }
  },
  {
    path: 'trainee',
    component: TraineeDashboard,
    canActivate: [authGuard, roleGuard],
    data: { roles: ['TRAINEE'] }
  },

  { path: '', redirectTo: 'login', pathMatch: 'full' },
  { path: '**', redirectTo: 'login' }
];