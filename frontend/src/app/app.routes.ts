import { Routes } from '@angular/router';
import { authGuard, roleGuard } from './core/guards/auth.guard';
import { Login } from './pages/login/login';
import { Signup } from './pages/signup/signup';
import { AdminDashboard } from './pages/admin-dashboard/admin-dashboard';
import { TrainerDashboard } from './pages/trainer-dashboard/trainer-dashboard';
import { TraineeDashboard } from './pages/trainee-dashboard/trainee-dashboard';
import { MyCourses } from './pages/my-courses/my-courses';
import { MyAttendancePage } from './pages/my-attendance/my-attendance';
import { TrainerSessions } from './pages/trainer-sessions/trainer-sessions';
import { MyExams } from './pages/my-exams/my-exams';
import { TakeExam } from './pages/take-exam/take-exam';
import { TrainerExams } from './pages/trainer-exams/trainer-exams';
import { Trainers } from './pages/trainers/trainers';
import { Trainees } from './pages/trainees/trainees';
import { Courses } from './pages/courses/courses';
import { Enrollments } from './pages/enrollments/enrollments';

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
    path: 'admin/trainers',
    component: Trainers,
    canActivate: [authGuard, roleGuard],
    data: { roles: ['ADMIN'] }
  },
  {
    path: 'admin/trainees',
    component: Trainees,
    canActivate: [authGuard, roleGuard],
    data: { roles: ['ADMIN'] }
  },
  {
    path: 'admin/courses',
    component: Courses,
    canActivate: [authGuard, roleGuard],
    data: { roles: ['ADMIN'] }
  },
  {
    path: 'admin/enrollments',
    component: Enrollments,
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
  {
    path: 'trainee/courses',
    component: MyCourses,
    canActivate: [authGuard, roleGuard],
    data: { roles: ['TRAINEE'] }
  },
  {
    path: 'trainee/attendance',
    component: MyAttendancePage,
    canActivate: [authGuard, roleGuard],
    data: { roles: ['TRAINEE'] }
  },
  {
    path: 'trainer/sessions',
    component: TrainerSessions,
    canActivate: [authGuard, roleGuard],
    data: { roles: ['TRAINER'] }
  },
  {
    path: 'trainer/exams',
    component: TrainerExams,
    canActivate: [authGuard, roleGuard],
    data: { roles: ['TRAINER'] }
  },
  {
    path: 'trainee/exams',
    component: MyExams,
    canActivate: [authGuard, roleGuard],
    data: { roles: ['TRAINEE'] }
  },
  {
    path: 'trainee/exams/:id',
    component: TakeExam,
    canActivate: [authGuard, roleGuard],
    data: { roles: ['TRAINEE'] }
  },

  { path: '', redirectTo: 'login', pathMatch: 'full' },
  { path: '**', redirectTo: 'login' }
];
